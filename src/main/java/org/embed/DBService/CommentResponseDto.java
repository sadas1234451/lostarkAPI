package org.embed.DBService;

import org.embed.entity.CommunityComment;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private final Long id;
    private final Long postId; // 어떤 게시글에 달린 댓글인지
    private final String content;
    private final String authorNickname;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;
    private final boolean isModified; // 수정됨 여부 판단

    public CommentResponseDto(CommunityComment comment) {
        this.id = comment.getId();
        this.postId = comment.getPost().getId(); // 댓글이 속한 게시글 ID
        this.content = comment.getContent();
        this.authorNickname = comment.getAuthorNickname();
        this.createdDate = comment.getCreatedDate();
        this.modifiedDate = comment.getModifiedDate();
        // 수정됨 여부 판단 로직: modifiedDate가 null이 아니면 true
        this.isModified = comment.getModifiedDate() != null;
    }
}