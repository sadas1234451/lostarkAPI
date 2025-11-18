package org.embed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "community_post")
public class CommunityPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_nickname", nullable = false, length = 100)
    private String authorNickname; // 닉네임 (필수)

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // 해시된 비밀번호 (4자리 입력 후 해시값 저장)

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0; // 조회수 (기본값 0)

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    public CommunityPost(String title, String content, String authorNickname, String passwordHash){
        this.title = title;
        this.content = content;
        this.authorNickname= authorNickname;
        this.passwordHash = passwordHash;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;

    }

    public void incrementViewCount(){
        this.viewCount++;
    }
}
