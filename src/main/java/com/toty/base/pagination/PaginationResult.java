package com.toty.base.pagination;

import com.toty.post.presentation.dto.response.postlist.PostListResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaginationResult {
    private List<?> content;
    private int currentPage;
    private int startPage;
    private int endPage;
    private int totalPages;
    private long totalElements;
    private List<Integer> pageList;

    public PaginationResult(List<?> content, int currentPage, int startPage, int endPage, int totalPages, long totalElements, List<Integer> pageList) {
        this.content = content;
        this.currentPage = currentPage;
        this.startPage = startPage;
        this.endPage = endPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageList = pageList;
    }
}
