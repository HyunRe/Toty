package com.toty.user.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkDto {
    private String site;
    private String url;

    public LinkDto(String site, String url) {
        this.site = site;
        this.url = url;
    }
}

