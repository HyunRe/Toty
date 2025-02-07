package com.toty.user.presentation;

import com.toty.base.response.SuccessResponse;
import com.toty.common.pagination.PaginationResult;
import com.toty.user.application.UserScrapeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class UserScrapeApiController {
    private final UserScrapeService userScrapeService;

    @GetMapping("/myScrape")
    public ResponseEntity<?> myScrape(@RequestParam("userId") Long userId,
                           @RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "postCategory", required = false) String postCategory,
                           Model model) {

        PaginationResult result = userScrapeService.getPagedPostsByMyScrape(userId, page, postCategory);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "전체 게시글 목록 조회",
                result
        );
        return ResponseEntity.ok(successResponse);
    }

}
