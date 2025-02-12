package com.toty.user.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkInfo {
    private String site;
    private String url;

    public LinkInfo(String site, String url) {
        this.site = site;
        this.url = url;
    }
}

