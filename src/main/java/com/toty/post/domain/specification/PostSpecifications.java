package com.toty.post.domain.specification;

import com.toty.common.baseException.InvalidCategoryException;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import org.springframework.data.jpa.domain.Specification;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PostSpecifications {
    // 오늘 날짜로 필터링
    public static Specification<Post> isToday() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1); // 오늘 끝 시간 (23:59:59.999...)
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get("updatedAt"), todayStart, todayEnd
        );
    }

    // 이번 주 날짜로 필터링
    public static Specification<Post> isThisWeek() {
        LocalDateTime startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY).atTime(23, 59, 59, 999999999);
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get("updatedAt"),
                startOfWeek,
                endOfWeek
        );
    }

    // 이번 달 날짜로 필터링
    public static Specification<Post> isThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();  // 이번 달의 첫 날
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);  // 이번 달의 마지막 날
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get("updatedAt"), startOfMonth, endOfMonth
        );
    }

    // 삭제 되지 않은 사용자 데이터 필터링
    public static Specification<Post> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("user").get("isDeleted"));
    }

    // 특정 사용자 ID로 필터링
    public static Specification<Post> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    // 특정 카테고리로 필터링
    public static Specification<Post> hasCategory(String postCategory) {
        return (root, query, criteriaBuilder) -> {
            if (postCategory == null || postCategory.isEmpty()) {
                throw new IllegalArgumentException("카테고리는 null 또는 빈 문자열일 수 없습니다.");
            }

            List<PostCategory> validCategories = List.of(PostCategory.KNOWLEDGE, PostCategory.GENERAL, PostCategory.QnA);
            PostCategory categoryEnum = PostCategory.fromString(postCategory);
            // PostCategory categoryEnum = PostCategory.valueOf(postCategory.toUpperCase()); // 입력된 값을 enum으로 변환
            if (!validCategories.contains(categoryEnum)) {
                throw new InvalidCategoryException(postCategory);
            }

            return criteriaBuilder.equal(root.get("postCategory"), categoryEnum);
        };
    }
}
