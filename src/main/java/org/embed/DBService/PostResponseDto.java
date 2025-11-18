package org.embed.DBService;

import org.embed.entity.CommunityPost;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PostResponseDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorNickname;
    private final int viewCount;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;
    private final boolean isModified; // 💡 수정됨 여부를 프론트엔드에 전달

    public PostResponseDto(CommunityPost post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorNickname = post.getAuthorNickname();
        this.viewCount = post.getViewCount();
        this.createdDate = post.getCreatedDate();
        this.modifiedDate = post.getModifiedDate();
        // 수정됨 여부 판단 로직: modifiedDate가 null이 아니면 true
        this.isModified = post.getModifiedDate() != null;
    }

    // 목록 조회용 생성자 (content를 제외하고 필요한 필드만 포함)
    public PostResponseDto(Long id, String title, String authorNickname, int viewCount, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.title = title;
        this.content = null; // 목록에서는 내용 제외
        this.authorNickname = authorNickname;
        this.viewCount = viewCount;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.isModified = modifiedDate != null;
    }
}