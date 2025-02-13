package com.toty.post.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapeRequest {
    private String Scrape;

    public ScrapeRequest(String Scrape) {
        this.Scrape = Scrape;
    }
}
