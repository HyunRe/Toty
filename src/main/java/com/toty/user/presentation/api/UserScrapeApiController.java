package com.toty.user.presentation.api;

import com.toty.common.annotation.CurrentUser;
import com.toty.common.pagination.PaginationResult;
import com.toty.user.application.UserScrapeService;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Scrape", description = "사용자 스크랩 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class UserScrapeApiController {
    private final UserScrapeService userScrapeService;

    @Operation(summary = "내 스크랩 목록 조회", description = "로그인한 사용자가 스크랩한 게시물 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/myScrape")
    public ResponseEntity<PaginationResult> myScrape(@CurrentUser User user,
                                                     @RequestParam(name = "page", defaultValue = "1") int page,
                                                     @RequestParam(name = "postCategory", required = false) String postCategory) {

        PaginationResult result = userScrapeService.getPagedPostsByMyScrape(user.getId(), page, postCategory);
        return ResponseEntity.ok(result);
    }
}
