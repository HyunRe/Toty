package com.toty.post.dto.request;

import com.toty.post.domain.model.PostTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostUpdateRequest {
    @NotBlank(message = "제목을 입력 하세요.")
    private String title;

    @NotBlank(message = "내용을 입력 하세요.")
    @Size(min = 1, max = 500, message = "한 글자 이상 내용을 입력 하세요.")
    private String content;

    private List<MultipartFile> postImages = new ArrayList<>();

    @NotNull(message = "기술 태그를 선택 하세요.")
    private List<PostTag> postTags = new ArrayList<>();

    public PostUpdateRequest(String title, String content, List<MultipartFile> postImages, List<PostTag> postTags) {
        this.title = title;
        this.content = content;
        this.postImages = postImages;
        this.postTags = postTags;
    }
}
