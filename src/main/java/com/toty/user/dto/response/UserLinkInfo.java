package com.toty.user.dto.response;

import com.toty.user.domain.model.Site;
import lombok.AccessLevel;
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

