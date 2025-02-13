package com.toty.user.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.common.pagination.PaginationResult;
import com.toty.user.application.UserScrapeService;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PaginationResult> myScrape(@CurrentUser User user,
                                                     @RequestParam(name = "page", defaultValue = "1") int page,
                                                     @RequestParam(name = "postCategory", required = false) String postCategory) {

        PaginationResult result = userScrapeService.getPagedPostsByMyScrape(user.getId(), page, postCategory);
        return ResponseEntity.ok(result);
    }
}
