package com.toty.post.dto.request;

import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.PostTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCreateRequest {
    @NotBlank(message = "제목을 입력 하세요.")
    private String title;

    @NotBlank(message = "내용을 입력 하세요.")
    @Size(min = 1, max = 500, message = "한 글자 이상 내용을 입력 하세요.")
    private String content;

    @NotNull(message = "게시글 유형을 선택 하세요.")
    private PostCategory postCategory;

    @NotNull(message = "기술 태그를 선택 하세요.")
    private List<String> postTags = new ArrayList<>();

    public PostCreateRequest(String title, String content, PostCategory postCategory, List<String> postTags) {
        this.title = title;
        this.content = content;
        this.postCategory = postCategory;
        this.postTags = postTags;
    }
}
