package com.toty.user.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum Site {
    GITHUB("GITHUB"), BLOG("BLOG");

    private String value;

    Site(String value) {
        this.value = value;
    }

}
