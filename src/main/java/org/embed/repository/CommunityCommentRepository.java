package org.embed.repository;

import org.embed.entity.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    /**
     * 특정 게시글 ID(postId)에 해당하는 모든 댓글을 조회.
     * 댓글은 작성된 순서대로 (id 오름차순) 반환.
     */
    List<CommunityComment> findAllByPost_IdOrderByIdAsc(Long postId);

    /**
     * 특정 게시글 ID와 댓글 ID로 댓글을 상세 조회.
     * 댓글 수정/삭제 시 해당 댓글이 게시글에 속하는지 확인하기 위해 사용.
     */
    List<CommunityComment> findByIdAndPost_Id(Long id, Long postId);
}