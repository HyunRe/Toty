package com.toty.comment.domain.pagination;

import com.toty.common.pagination.PaginationResult;
import com.toty.common.pagination.PaginationStrategy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentPaginationStrategy implements PaginationStrategy {
    @Override
    public PaginationResult getPaginationResult(Page<?> page, int pageSize, List<?> content) {
        int totalPages = page.getTotalPages();
        int currentPage = page.getNumber() + 1; // 현재 페이지

        // 페이지 시작점 계산
        int startPage = Math.max(1, currentPage - ((currentPage - 1) % pageSize));
        // 페이지 끝점 계산
        int endPage = Math.min(totalPages, startPage + pageSize - 1);

        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        return new PaginationResult(content, currentPage, startPage, endPage, totalPages, page.getTotalElements(), pageList);
    }
}
