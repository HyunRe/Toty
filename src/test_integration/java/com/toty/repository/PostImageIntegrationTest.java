package com.toty.repository;

import com.toty.common.image.application.ImageUploadService;
import com.toty.common.image.domain.component.ImageValidator;
import com.toty.common.image.domain.component.S3KeyGenerator;
import com.toty.common.image.domain.model.Image;
import com.toty.common.image.domain.model.ImageType;
import com.toty.common.image.domain.repository.ImageRepository;
import com.toty.common.image.infrastructure.S3StorageService;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.user.domain.model.Role;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import com.toty.TestJpaApplication;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 게시글 이미지 통합 테스트
 * 목적: Summernote 에디터 이미지 업로드부터 게시글 삭제까지 전체 라이프사이클 검증
 * S3는 Mock 처리 (실제 S3 통신은 S3StorageServiceTest에서 단위 테스트)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ImageUploadService.class, ImageValidator.class, S3KeyGenerator.class})
@ContextConfiguration(classes = TestJpaApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("게시글 이미지 전체 라이프사이클 통합 테스트")
class PostImageIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private S3StorageService s3StorageService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("테스터")
                .username("테스트유저")
                .phoneNumber("010-1234-5678")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("게시글 생성 시 이미지를 연결할 수 있다")
    void createPostWithImages() {
        // given - Summernote 에디터에서 이미지 업로드 시뮬레이션
        String url1 = "https://s3.amazonaws.com/bucket/images/post/1/image1.jpg";
        String url2 = "https://s3.amazonaws.com/bucket/images/post/1/image2.jpg";

        // postId가 null인 이미지 생성 (에디터에서 업로드된 상태)
        Image image1 = new Image("images/post/1/image1.jpg", url1, testUser.getId(), null, ImageType.POST_CONTENT, null);
        Image image2 = new Image("images/post/1/image2.jpg", url2, testUser.getId(), null, ImageType.POST_CONTENT, null);
        imageRepository.save(image1);
        imageRepository.save(image2);

        // 게시글 생성
        String content = String.format("<p>내용</p><img src='%s'/><img src='%s'/>", url1, url2);
        Post post = Post.builder()
                .title("이미지가 포함된 게시글")
                .content(content)
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        entityManager.flush();
        entityManager.clear();

        // when - 이미지를 게시글에 연결
        List<String> imageUrls = Arrays.asList(url1, url2);
        imageUploadService.linkImagesToPost(post.getId(), testUser.getId(), imageUrls);

        entityManager.flush();
        entityManager.clear();

        // then - 이미지가 게시글에 연결되었는지 확인
        Long savedPostId = post.getId();
        List<Image> linkedImages = imageRepository.findByPostId(savedPostId);
        assertThat(linkedImages).hasSize(2);
        assertThat(linkedImages).extracting(Image::getUrl).containsExactlyInAnyOrder(url1, url2);
        assertThat(linkedImages).allMatch(img -> img.getPostId().equals(savedPostId));
    }

    @Test
    @DisplayName("게시글 삭제 시 연결된 이미지도 함께 삭제된다")
    void deletePostWithImages() {
        // given - 이미지가 포함된 게시글 생성
        String url1 = "https://s3.amazonaws.com/bucket/images/post/1/image1.jpg";
        String url2 = "https://s3.amazonaws.com/bucket/images/post/1/image2.jpg";

        Post post = Post.builder()
                .title("이미지 게시글")
                .content(String.format("<img src='%s'/><img src='%s'/>", url1, url2))
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        Image image1 = new Image("images/post/1/image1.jpg", url1, testUser.getId(), post.getId(), ImageType.POST_CONTENT, null);
        Image image2 = new Image("images/post/1/image2.jpg", url2, testUser.getId(), post.getId(), ImageType.POST_CONTENT, null);
        imageRepository.save(image1);
        imageRepository.save(image2);

        Long postId = post.getId();
        Long image1Id = image1.getId();
        Long image2Id = image2.getId();

        entityManager.flush();
        entityManager.clear();

        // when - 게시글 삭제 (이미지도 함께 삭제되어야 함)
        imageUploadService.deletePostImages(postId);
        postRepository.deleteById(postId);

        entityManager.flush();
        entityManager.clear();

        // then - 게시글과 이미지 모두 삭제됨
        assertThat(postRepository.findById(postId)).isEmpty();
        assertThat(imageRepository.findById(image1Id)).isEmpty();
        assertThat(imageRepository.findById(image2Id)).isEmpty();
        assertThat(imageRepository.findByPostId(postId)).isEmpty();
    }

    @Test
    @DisplayName("게시글에 연결되지 않은 임시 이미지는 게시글 생성 전까지 postId가 null이다")
    void temporaryImagesHaveNullPostId() {
        // given - Summernote 에디터에서 이미지만 업로드하고 게시글은 아직 저장 안함
        String url = "https://s3.amazonaws.com/bucket/images/post/1/temp.jpg";
        Image tempImage = new Image("images/post/1/temp.jpg", url, testUser.getId(), null, ImageType.POST_CONTENT, null);
        tempImage = imageRepository.save(tempImage);

        entityManager.flush();
        entityManager.clear();

        // when - 이미지 조회
        Image foundImage = imageRepository.findById(tempImage.getId()).orElseThrow();

        // then - postId가 null
        assertThat(foundImage.getPostId()).isNull();
        assertThat(foundImage.getUserId()).isEqualTo(testUser.getId());
        assertThat(foundImage.getType()).isEqualTo(ImageType.POST_CONTENT);
    }

    @Test
    @DisplayName("여러 게시글이 각각 다른 이미지를 가질 수 있다")
    void multiplePostsWithDifferentImages() {
        // given
        // 게시글 1 생성
        Post post1 = Post.builder()
                .title("게시글 1")
                .content("내용 1")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post1 = postRepository.save(post1);

        Image image1 = new Image("key1", "url1", testUser.getId(), post1.getId(), ImageType.POST_CONTENT, null);
        imageRepository.save(image1);

        // 게시글 2 생성
        Post post2 = Post.builder()
                .title("게시글 2")
                .content("내용 2")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post2 = postRepository.save(post2);

        Image image2 = new Image("key2", "url2", testUser.getId(), post2.getId(), ImageType.POST_CONTENT, null);
        Image image3 = new Image("key3", "url3", testUser.getId(), post2.getId(), ImageType.POST_CONTENT, null);
        imageRepository.save(image2);
        imageRepository.save(image3);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Image> post1Images = imageRepository.findByPostId(post1.getId());
        List<Image> post2Images = imageRepository.findByPostId(post2.getId());

        // then
        assertThat(post1Images).hasSize(1);
        assertThat(post2Images).hasSize(2);
        assertThat(post1Images.get(0).getUrl()).isEqualTo("url1");
        assertThat(post2Images).extracting(Image::getUrl).containsExactlyInAnyOrder("url2", "url3");
    }

    @Test
    @DisplayName("게시글 없이 업로드된 고아 이미지를 조회할 수 있다")
    void findOrphanImages() {
        // given - postId가 null인 이미지들 (게시글 작성 중 업로드만 하고 저장 안한 경우)
        Image orphan1 = new Image("key1", "url1", testUser.getId(), null, ImageType.POST_CONTENT, null);
        Image orphan2 = new Image("key2", "url2", testUser.getId(), null, ImageType.POST_CONTENT, null);
        imageRepository.save(orphan1);
        imageRepository.save(orphan2);

        // 정상 게시글에 연결된 이미지
        Post post = Post.builder()
                .title("게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        Image linkedImage = new Image("key3", "url3", testUser.getId(), post.getId(), ImageType.POST_CONTENT, null);
        imageRepository.save(linkedImage);

        entityManager.flush();
        entityManager.clear();

        // when - userId로 모든 이미지 조회하고 postId가 null인 것만 필터링
        List<Image> allUserImages = imageRepository.findByUserId(testUser.getId());
        List<Image> orphanImages = allUserImages.stream()
                .filter(img -> img.getPostId() == null && img.getType() == ImageType.POST_CONTENT)
                .toList();

        // then - 고아 이미지 2개 발견
        assertThat(orphanImages).hasSize(2);
        assertThat(orphanImages).extracting(Image::getUrl).containsExactlyInAnyOrder("url1", "url2");
    }

    @Test
    @DisplayName("게시글 이미지와 프로필 이미지는 독립적으로 관리된다")
    void postImagesAndProfileImagesAreIndependent() {
        // given
        // 프로필 이미지 생성
        Image profileImage = new Image("profile/key", "profile/url", testUser.getId(), null, ImageType.PROFILE, null);
        imageRepository.save(profileImage);

        // 게시글과 게시글 이미지 생성
        Post post = Post.builder()
                .title("게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        Image postImage = new Image("post/key", "post/url", testUser.getId(), post.getId(), ImageType.POST_CONTENT, null);
        imageRepository.save(postImage);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Image> profileImages = imageRepository.findByUserIdAndType(testUser.getId(), ImageType.PROFILE);
        List<Image> postImages = imageRepository.findByPostId(post.getId());

        // then - 각각 독립적으로 관리됨
        assertThat(profileImages).hasSize(1);
        assertThat(postImages).hasSize(1);
        assertThat(profileImages.get(0).getType()).isEqualTo(ImageType.PROFILE);
        assertThat(postImages.get(0).getType()).isEqualTo(ImageType.POST_CONTENT);
    }

    @Test
    @DisplayName("이미지를 게시글에 연결할 때 URL이 일치하지 않으면 연결되지 않는다")
    void linkImagesToPost_urlMismatch() {
        // given
        String uploadedUrl = "https://s3.amazonaws.com/bucket/images/post/1/image1.jpg";
        String differentUrl = "https://s3.amazonaws.com/bucket/images/post/1/image2.jpg";

        // uploadedUrl로 이미지 업로드
        Image image = new Image("images/post/1/image1.jpg", uploadedUrl, testUser.getId(), null, ImageType.POST_CONTENT, null);
        imageRepository.save(image);

        Post post = Post.builder()
                .title("게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        entityManager.flush();
        entityManager.clear();

        // when - 다른 URL로 연결 시도
        imageUploadService.linkImagesToPost(post.getId(), testUser.getId(), List.of(differentUrl));

        entityManager.flush();
        entityManager.clear();

        // then - 연결되지 않음
        Image foundImage = imageRepository.findById(image.getId()).orElseThrow();
        assertThat(foundImage.getPostId()).isNull();
    }

    @Test
    @DisplayName("한 사용자가 여러 게시글에 이미지를 각각 업로드할 수 있다")
    void userCanUploadImagesToMultiplePosts() {
        // given - 사용자가 3개의 게시글을 작성하고 각각 이미지 업로드
        Post post1 = postRepository.save(Post.builder()
                .title("게시글 1")
                .content("내용 1")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build());

        Post post2 = postRepository.save(Post.builder()
                .title("게시글 2")
                .content("내용 2")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build());

        Post post3 = postRepository.save(Post.builder()
                .title("게시글 3")
                .content("내용 3")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build());

        // 각 게시글에 이미지 연결
        imageRepository.save(new Image("key1", "url1", testUser.getId(), post1.getId(), ImageType.POST_CONTENT, null));
        imageRepository.save(new Image("key2", "url2", testUser.getId(), post2.getId(), ImageType.POST_CONTENT, null));
        imageRepository.save(new Image("key3", "url3", testUser.getId(), post2.getId(), ImageType.POST_CONTENT, null));
        imageRepository.save(new Image("key4", "url4", testUser.getId(), post3.getId(), ImageType.POST_CONTENT, null));

        entityManager.flush();
        entityManager.clear();

        // when
        List<Image> allUserImages = imageRepository.findByUserId(testUser.getId()).stream()
                .filter(img -> img.getType() == ImageType.POST_CONTENT)
                .toList();

        // then
        assertThat(allUserImages).hasSize(4);
        assertThat(imageRepository.findByPostId(post1.getId())).hasSize(1);
        assertThat(imageRepository.findByPostId(post2.getId())).hasSize(2);
        assertThat(imageRepository.findByPostId(post3.getId())).hasSize(1);
    }
}
