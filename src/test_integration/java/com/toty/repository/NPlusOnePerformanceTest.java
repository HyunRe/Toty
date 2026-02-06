package com.toty.repository;

import com.toty.following.domain.model.Following;
import com.toty.following.domain.repository.FollowingRepository;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.model.post.PostLike;
import com.toty.post.domain.repository.post.PostLikeRepository;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.user.domain.model.Role;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import com.toty.TestJpaApplication;

import static org.assertj.core.api.Assertions.*;

/**
 * N+1 성능 개선 Repository 테스트
 * 목적: Fetch Join 적용 전/후 쿼리 수 비교
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
@DisplayName("N+1 쿼리 성능 개선 검증 테스트")
class NPlusOnePerformanceTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private FollowingRepository followingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Statistics statistics;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Hibernate Statistics 활성화
        statistics = entityManager.getEntityManagerFactory()
                .unwrap(org.hibernate.SessionFactory.class)
                .getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        // 테스트 사용자 생성
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
    @DisplayName("[PostRepository] fetch join 사용 시 N+1 문제가 해결된다")
    void postRepository_fetchJoin_preventNPlusOne() {
        // given - 10개의 게시글 생성
        for (int i = 1; i <= 10; i++) {
            User author = User.builder()
                    .email("user" + i + "@example.com")
                    .password("password")
                    .nickname("사용자" + i)
                    .username("유저" + i)
                    .phoneNumber("010-0000-000" + i)
                    .role(Role.USER)
                    .build();
            author = userRepository.save(author);

            Post post = Post.builder()
                    .title("게시글 " + i)
                    .content("내용 " + i)
                    .postCategory(PostCategory.GENERAL)
                    .user(author)
                    .build();
            postRepository.save(post);
        }

        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when - fetch join 사용
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> posts = postRepository.findAllWithUser(pageRequest);

        // 모든 게시글의 작성자 닉네임 접근 (실제 사용 시나리오)
        posts.getContent().forEach(post -> {
            String nickname = post.getUser().getNickname();
            assertThat(nickname).isNotNull();
        });

        // then - 쿼리 수 검증
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("🔍 Fetch Join 적용 후 쿼리 수: " + queryCount);

        // fetch join으로 1번의 쿼리만 실행 (게시글 + 사용자 조인)
        // count 쿼리 포함해도 2~3개 이내
        assertThat(queryCount).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("[FollowingRepository] fetch join 사용 시 fromUser와 toUser를 한 번에 조회한다")
    void followingRepository_fetchJoin_preventNPlusOne() {
        // given - 10명의 팔로워 생성
        for (int i = 1; i <= 10; i++) {
            User follower = User.builder()
                    .email("follower" + i + "@example.com")
                    .password("password")
                    .nickname("팔로워" + i)
                    .username("팔로워" + i)
                    .phoneNumber("010-1111-000" + i)
                    .role(Role.USER)
                    .build();
            follower = userRepository.save(follower);

            Following following = new Following(follower, testUser);
            followingRepository.save(following);
        }

        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when - fetch join 사용
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Following> followings = followingRepository.findPagedFollowingByToUserIdWithUsers(pageRequest, testUser.getId());

        // 모든 팔로잉의 fromUser, toUser 정보 접근
        followings.getContent().forEach(following -> {
            String fromNickname = following.getFromUser().getNickname();
            String toNickname = following.getToUser().getNickname();
            assertThat(fromNickname).isNotNull();
            assertThat(toNickname).isNotNull();
        });

        // then - 쿼리 수 검증
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("🔍 Following Fetch Join 적용 후 쿼리 수: " + queryCount);

        // fetch join으로 최소 쿼리 실행
        assertThat(queryCount).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("[PostLikeRepository] fetch join 사용 시 Post와 User를 한 번에 조회한다")
    void postLikeRepository_fetchJoin_preventNPlusOne() {
        // given - 10개의 좋아요한 게시글 생성
        for (int i = 1; i <= 10; i++) {
            User author = User.builder()
                    .email("author" + i + "@example.com")
                    .password("password")
                    .nickname("작성자" + i)
                    .username("작성자" + i)
                    .phoneNumber("010-2222-000" + i)
                    .role(Role.USER)
                    .build();
            author = userRepository.save(author);

            Post post = Post.builder()
                    .title("게시글 " + i)
                    .content("내용 " + i)
                    .postCategory(PostCategory.GENERAL)
                    .user(author)
                    .build();
            post = postRepository.save(post);

            PostLike postLike = new PostLike(testUser, post);
            postLikeRepository.save(postLike);
        }

        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when - fetch join 사용
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> likedPosts = postLikeRepository.findLikedPostsByUserIdWithUser(testUser.getId(), pageRequest);

        // 모든 게시글의 작성자 정보 접근
        likedPosts.getContent().forEach(post -> {
            String authorNickname = post.getUser().getNickname();
            assertThat(authorNickname).isNotNull();
        });

        // then - 쿼리 수 검증
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("🔍 PostLike Fetch Join 적용 후 쿼리 수: " + queryCount);

        assertThat(queryCount).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("[성능 비교] 데이터 100개 조회 시 fetch join의 효과를 확인한다")
    void comparePerformance_withLargeDataset() {
        // given - 100개의 게시글 생성
        for (int i = 1; i <= 100; i++) {
            User author = User.builder()
                    .email("bulk" + i + "@example.com")
                    .password("password")
                    .nickname("대량" + i)
                    .username("대량" + i)
                    .phoneNumber(String.format("010-9999-%04d", i))
                    .role(Role.USER)
                    .build();
            author = userRepository.save(author);

            Post post = Post.builder()
                    .title("대량 게시글 " + i)
                    .content("내용 " + i)
                    .postCategory(PostCategory.GENERAL)
                    .user(author)
                    .build();
            postRepository.save(post);
        }

        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Post> posts = postRepository.findAllWithUser(pageRequest);

        posts.getContent().forEach(post -> post.getUser().getNickname());

        // then
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("🔥 100개 데이터 Fetch Join 쿼리 수: " + queryCount);

        // fetch join 없으면 101개 (1 + 100), 적용 후에는 2~3개
        assertThat(queryCount).isLessThan(10);
    }
}
