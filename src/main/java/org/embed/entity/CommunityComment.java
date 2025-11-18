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
@Table(name = "community_comment")
public class CommunityComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💡 외래 키(FK): 해당 댓글이 달린 게시글 정보
    // @ManyToOne 관계를 통해 CommunityPost 엔티티와 연결됩니다.
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정
    @JoinColumn(name = "post_id", nullable = false) // SQL의 post_id 컬럼과 매핑
    private CommunityPost post; 

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "author_nickname", nullable = false, length = 100)
    private String authorNickname; // 닉네임 (필수)

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // 해시된 비밀번호 (4자리 입력 후 해시값 저장)

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    // --- 생성자 ---
    public CommunityComment(CommunityPost post, String content, String authorNickname, String passwordHash) {
        this.post = post;
        this.content = content;
        this.authorNickname = authorNickname;
        this.passwordHash = passwordHash;
    }

    // --- 비즈니스 로직: 댓글 업데이트 ---
    public void update(String content) {
        this.content = content;
        // modifiedDate는 @LastModifiedDate에 의해 자동으로 업데이트됩니다.
    }
}
