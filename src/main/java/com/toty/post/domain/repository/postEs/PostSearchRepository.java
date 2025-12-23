package com.toty.post.domain.repository.postEs;

import com.toty.post.domain.model.postEs.PostEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.annotations.SourceFilters;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostEs, String> {
    /**
     * 제목으로 검색 (키워드 공백 x)
     */
    @SourceFilters(excludes = {"content","replies"})
    @Query("""
    {
      "bool": {
        "must": [
          { "match": { "title": "?0" } },
          { "term": { "category": "?1" } }
        ]
      }
    }
    """)
    Page<PostEs> searchByTitleAndCategory(String keyword, String category, Pageable pageable);

    /**
     * 본문으로 검색 (키워드 공백 x)
     */
    @SourceFilters(excludes = {"content","replies"})
    @Query("""
    {
      "bool": {
        "must": [
          { "match": { "content": "?0" } },
          { "term": { "category": "?1" } }
        ]
      }
    }
    """)
    Page<PostEs> searchByContentAndCategory(String keyword, String category, Pageable pageable);

    /**
     * 제목+본문으로 검색 (키워드 공백 x)
     */
    @SourceFilters(excludes = {"content","replies"})
    @Query("""
        {
         "bool": {
           "must": {
             "multi_match": {
               "query": "?0",
               "fields": ["title^1.4", "content^1.1"]
             }
           },
           "filter": {
             "term": {
               "category": "?1"
             }
           }
         }
        }
    """)
    Page<PostEs> searchTitleAndContentAndCategory(String keyword, String category, Pageable pageable);

    /**
     * 키워드 공백 o
     * 제목, 본문, 제목 + 본문 모두 공통으로 사용
     */
    @SourceFilters(excludes = {"content","replies"})
    @Query("""
    {
      "bool": {
        "must": [
          { "term": { "category": "?0" } }
        ]
      }
    }
    """)
    Page<PostEs> searchByCategory(String category, Pageable pageable);

}

