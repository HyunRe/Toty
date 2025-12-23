package com.toty.comment.domain.event;

import com.toty.comment.dto.CommentDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentEvent {
    private String type; // "CREATE", "UPDATE", "DELETE"
    private CommentDto commentDto;
    private LocalDateTime timestamp;

    public CommentEvent(String type, CommentDto commentDto) {
        this(type, commentDto, LocalDateTime.now());
    }
}
