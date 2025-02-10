package com.toty.post.domain.repository;

import com.toty.post.domain.model.elasticsearch.PostEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.annotations.SourceFilters;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostEs, String> {
    /**
     * 제목으로 검색
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
     * 본문으로 검색
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
     * 제목+본문으로 검색
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
}

