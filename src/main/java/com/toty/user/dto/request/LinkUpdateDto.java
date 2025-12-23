package com.toty.user.dto.request;

import com.toty.user.dto.response.LinkDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkUpdateDto {
    private Long id;
    private List<LinkDto> links;

    public LinkUpdateDto(Long id, List<LinkDto> links) {
        this.id = id;
        this.links = links;
    }
}
