package com.toty.following.presentation.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowingListResponse {
    // 정보 담기(사진, 이름, 팔로우 여부, 페이지)
    List<Summary> userSummaries;
    PageInfo page;

    @AllArgsConstructor
    public static class PageInfo {
        int currentPage;
        int pageSize;
        int totalPages;
        // 페이지 네비게이션 없어서 사용하지 않음
//        Long startPages;
//        Long endPages;
    }

    @AllArgsConstructor
    public static class Summary {
        String profileImgUrl;
        String nickname;
        boolean isFollowing;
    }

    public FollowingListResponse(List<Summary> userSummaries, PageInfo page) {
        this.userSummaries = userSummaries;
        this.page = page;
    }
}
