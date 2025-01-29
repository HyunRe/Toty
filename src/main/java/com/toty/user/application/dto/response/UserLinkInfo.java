package com.toty.user.application.dto.response;

import com.toty.user.domain.Site;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLinkInfo {
    private Site site;
    private String url;

    public UserLinkInfo(Site site, String url) {
        this.site = site;
        this.url = url;
    }
}

