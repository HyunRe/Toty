package com.toty.post.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostScrapeRequest {
    @JsonProperty("scrapeAction")
    private String scrape;

    public String getScrape() {
        return scrape;
    }
}
