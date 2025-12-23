package com.toty.user.dto.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagUpdateDto {
    private Long id;
    private List<String> tags;

    public TagUpdateDto(Long id, List<String> tags) {
        this.id = id;
        this.tags = tags;
    }
}
