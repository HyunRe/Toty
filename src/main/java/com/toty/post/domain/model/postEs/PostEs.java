package com.toty.post.domain.model.postEs;

import com.toty.comment.domain.model.CommentEs;
import com.toty.post.domain.model.post.PostCategory;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "posts")
@Setting(settingPath = "elasticsearch/settings.json")
@Mapping(mappingPath = "elasticsearch/mappings.json")
public class PostEs {
    @Id
    @Field(name = "id",type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword,name = "nickname")
    private String nickname;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private PostCategory category;

    @Field(type = FieldType.Integer)
    private int viewCount;

    @Field(type = FieldType.Integer)
    private int likeCount;

    @Field(type = FieldType.Integer)
    private int replyCount;

    @Field(type = FieldType.Nested)
    private List<CommentEs> comments;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private ZonedDateTime createdAt;
}

