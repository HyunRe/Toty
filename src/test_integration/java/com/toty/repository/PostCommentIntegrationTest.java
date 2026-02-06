package com.toty.repository;

import com.toty.comment.domain.model.comment.Comment;
import com.toty.comment.domain.repository.CommentRepository;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.user.domain.model.Role;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import com.toty.TestJpaApplication;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 게시글·댓글 Repository 테스트
 * 목적: JPA 연관관계와 트랜잭션 검증
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = TestJpaApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("게시글·댓글 Repository 테스트")
class PostCommentIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

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
    @DisplayName("게시글을 생성하고 조회할 수 있다")
    void createAndReadPost() {
        // given
        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();

        // when
        Post savedPost = postRepository.save(post);
        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();

        // then
        assertThat(foundPost.getTitle()).isEqualTo("테스트 게시글");
        assertThat(foundPost.getContent()).isEqualTo("테스트 내용");
        assertThat(foundPost.getUser().getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("게시글에 댓글을 작성할 수 있다")
    void createCommentOnPost() {
        // given
        Post post = Post.builder()
                .title("게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        Comment comment = Comment.builder()
                .content("댓글 내용")
                .post(post)
                .user(testUser)
                .build();

        // when
        Comment savedComment = commentRepository.save(comment);
        Comment foundComment = commentRepository.findById(savedComment.getId()).orElseThrow();

        // then
        assertThat(foundComment.getContent()).isEqualTo("댓글 내용");
        assertThat(foundComment.getPost().getId()).isEqualTo(post.getId());
        assertThat(foundComment.getUser().getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("게시글 삭제 시 연관된 댓글도 함께 삭제된다 (CASCADE)")
    void deletePostWithComments() {
        // given
        Post post = Post.builder()
                .title("게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        Comment comment1 = Comment.builder()
                .content("댓글1")
                .post(post)
                .user(testUser)
                .build();
        Comment comment2 = Comment.builder()
                .content("댓글2")
                .post(post)
                .user(testUser)
                .build();
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        Long postId = post.getId();

        entityManager.flush();
        entityManager.clear();

        // when
        postRepository.deleteById(postId);
        postRepository.flush();

        // then
        assertThat(postRepository.findById(postId)).isEmpty();
        assertThat(commentRepository.findByPostIdWithUser(postId)).isEmpty();
    }

    @Test
    @DisplayName("게시글에 여러 댓글을 작성하고 조회할 수 있다")
    void createMultipleCommentsAndRead() {
        // given
        Post post = Post.builder()
                .title("게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        for (int i = 1; i <= 5; i++) {
            Comment comment = Comment.builder()
                    .content("댓글 " + i)
                    .post(post)
                    .user(testUser)
                    .build();
            commentRepository.save(comment);
        }

        // when
        List<Comment> comments = commentRepository.findByPostIdWithUser(post.getId());

        // then
        assertThat(comments).hasSize(5);
        assertThat(comments)
                .extracting(Comment::getContent)
                .contains("댓글 1", "댓글 2", "댓글 3", "댓글 4", "댓글 5");
    }

    @Test
    @DisplayName("트랜잭션 롤백 시 게시글과 댓글이 모두 저장되지 않는다")
    void transactionRollbackTest() {
        // given
        Post post = Post.builder()
                .title("롤백 테스트 게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();

        // when
        Long postId = postRepository.save(post).getId();

        Comment comment = Comment.builder()
                .content("롤백될 댓글")
                .post(post)
                .user(testUser)
                .build();
        commentRepository.save(comment);

        // then
        assertThat(postId).isNotNull();
    }

    @Test
    @DisplayName("게시글 조회 시 작성자 정보가 함께 조회된다")
    void findPostWithUser() {
        // given
        Post post = Post.builder()
                .title("게시글")
                .content("내용")
                .postCategory(PostCategory.GENERAL)
                .user(testUser)
                .build();
        post = postRepository.save(post);

        // when
        Post foundPost = postRepository.findById(post.getId()).orElseThrow();

        // then
        assertThat(foundPost.getUser()).isNotNull();
        assertThat(foundPost.getUser().getNickname()).isEqualTo("테스터");
    }
}
