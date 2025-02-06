package com.toty.post.application;

import com.toty.post.domain.model.elasticsearch.PostEs;
import com.toty.post.domain.model.elasticsearch.SearchField;
import com.toty.post.domain.repository.elasticsearch.PostEsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostEsService {

    private final PostEsRepository postEsRepository;

    public Page<PostEs> searchPosts(String keyword, SearchField field, int page) {

        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return switch (field) {
            case TITLE -> postEsRepository.searchGeneralPostsByTitle(keyword, pageRequest);
            case CONTENT -> postEsRepository.searchGeneralPostsByContent(keyword, pageRequest);
            case TITLE_CONTENT -> postEsRepository.searchGeneralPostsByTitleAndContent(keyword, pageRequest);
        };
    }
}
