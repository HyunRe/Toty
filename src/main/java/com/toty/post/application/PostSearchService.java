package com.toty.post.application;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.elasticsearch.PostEs;
import com.toty.post.domain.model.elasticsearch.SearchField;
import com.toty.post.domain.repository.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostSearchService {

    private final PostSearchRepository postSearchRepository;

    /**
     * 구현해야 할 기능
     * 1. 검색 속성(field)(제목, 본문, 제목 + 본문)마다 검색 방식(조회 메서드)이 다르다.
     * 2. 검색은 모든 게시판(일반, 정보, QnA)의 게시글들을 포함하고 있어야 한다.
     */
    public Map<PostCategory, Page<PostEs>> searchPosts(String keyword, SearchField field, int size) {
        PageRequest pageRequest = PageRequest.of(0, size);

        // 검색어가 공백인 경우
        if (keyword.trim().isEmpty()) {
           return Map.of(
                   PostCategory.GENERAL, postSearchRepository.searchByCategory(PostCategory.GENERAL.toString() ,pageRequest),
                   PostCategory.KNOWLEDGE, postSearchRepository.searchByCategory(PostCategory.KNOWLEDGE.toString(), pageRequest),
                   PostCategory.QnA, postSearchRepository.searchByCategory(PostCategory.QnA.toString(), pageRequest)
           );
        }

        // 검색어가 공백이 아닌 경우
        if (field == SearchField.TITLE) {
            return Map.of(
                    PostCategory.GENERAL, postSearchRepository.searchByTitleAndCategory(keyword, PostCategory.GENERAL.toString() ,pageRequest),
                    PostCategory.KNOWLEDGE, postSearchRepository.searchByTitleAndCategory(keyword,PostCategory.KNOWLEDGE.toString(), pageRequest),
                    PostCategory.QnA, postSearchRepository.searchByTitleAndCategory(keyword, PostCategory.QnA.toString(), pageRequest)
            );
        } else if (field == SearchField.CONTENT) {
            return Map.of(
                    PostCategory.GENERAL, postSearchRepository.searchByContentAndCategory(keyword, PostCategory.GENERAL.toString() ,pageRequest),
                    PostCategory.KNOWLEDGE, postSearchRepository.searchByContentAndCategory(keyword,PostCategory.KNOWLEDGE.toString(), pageRequest),
                    PostCategory.QnA, postSearchRepository.searchByContentAndCategory(keyword, PostCategory.QnA.toString(), pageRequest)
            );
        } else if (field == SearchField.TITLE_CONTENT) {
            return Map.of(
                    PostCategory.GENERAL, postSearchRepository.searchTitleAndContentAndCategory(keyword, PostCategory.GENERAL.toString() ,pageRequest),
                    PostCategory.KNOWLEDGE, postSearchRepository.searchTitleAndContentAndCategory(keyword,PostCategory.KNOWLEDGE.toString(), pageRequest),
                    PostCategory.QnA, postSearchRepository.searchTitleAndContentAndCategory(keyword, PostCategory.QnA.toString(), pageRequest)
            );
        } else {
            throw new ExpectedException(ErrorCode.INVALID_SEARCH_FIELD);
        }
    }

    public Map<PostCategory, Page<PostEs>> searchPostsByCategory(String keyword, SearchField field, PostCategory category, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // 검색어가 공백인 경우
        if (keyword.trim().isEmpty()) {
            return Map.of(
                    PostCategory.GENERAL, postSearchRepository.searchByCategory(PostCategory.GENERAL.toString() ,pageRequest),
                    PostCategory.KNOWLEDGE, postSearchRepository.searchByCategory(PostCategory.KNOWLEDGE.toString(), pageRequest),
                    PostCategory.QnA, postSearchRepository.searchByCategory(PostCategory.QnA.toString(), pageRequest)
            );
        }

        // 검색어가 공백이 아닌 경우
        if (field == SearchField.TITLE) {
            return Map.of(
                    category, postSearchRepository.searchByTitleAndCategory(keyword, category.toString() ,pageRequest)
            );
        } else if (field == SearchField.CONTENT) {
            return Map.of(
                    category, postSearchRepository.searchByContentAndCategory(keyword, category.toString() ,pageRequest)
            );
        } else if (field == SearchField.TITLE_CONTENT) {
            return Map.of(
                    category, postSearchRepository.searchTitleAndContentAndCategory(keyword, category.toString() ,pageRequest)
            );
        } else {
            throw new ExpectedException(ErrorCode.INVALID_SEARCH_FIELD);
        }
    }

    public String savePost(String title, String content, String nickname, PostCategory category) {
        PostEs postSearch = PostEs.builder()
                .id(UUID.randomUUID().toString()) // todo : mysql의 pk 값이 저장되어야 함
                .nickname(nickname) // 사용자가 입력한 닉네임
                .title(title) // 사용자가 입력한 제목
                .content(content) // 사용자가 입력한 본문
                .category(category) // 사용자가 선택한 게시글 카테고리
                .viewCount(0) // 기본값: 0
                .likeCount(0) // 기본값: 0
                .replyCount(0) // 기본값: 0
                .comments(Collections.emptyList()) // 기본값: 빈 리스트
                .createdAt(ZonedDateTime.now())
                .build();

        return postSearchRepository.save(postSearch).getId();
    }
}
