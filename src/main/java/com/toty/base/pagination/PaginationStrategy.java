package com.toty.base.pagination;


import org.springframework.data.domain.Page;

import java.util.List;

public interface PaginationStrategy {
    PaginationResult getPaginationResult(Page<?> page, int pageSize, List<?> content);
}
