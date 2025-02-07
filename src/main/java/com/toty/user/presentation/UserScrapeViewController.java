package com.toty.user.presentation;

import com.toty.common.pagination.PaginationResult;
import com.toty.user.application.UserScrapeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class UserScrapeViewController {
    private final UserScrapeService userScrapeService;

    @GetMapping("/myScrape")
    public String myScrape(@RequestParam("userId") Long userId,
                           @RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "postCategory", required = false) String postCategory, // free, qna, knowledge
                           Model model) {

        PaginationResult result = userScrapeService.getPagedPostsByMyScrape(userId, page, postCategory);
        model.addAttribute("result", result);
        model.addAttribute("postCategory", postCategory);
        return "user/myScrape";
    }

}
