package com.toty.user.presentation.dto;

import com.toty.user.domain.Site;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkDto {
    Site site;
    String url;
}

