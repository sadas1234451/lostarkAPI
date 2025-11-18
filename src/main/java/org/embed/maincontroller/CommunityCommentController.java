package org.embed.maincontroller; 

import java.util.List;

import org.embed.DBService.CommentRequestDto;
import org.embed.DBService.CommentResponseDto;
import org.embed.service.CommunityCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/community/posts/{postId}/comments") // 💡 게시글 ID를 경로에 포함
@Validated 
public class CommunityCommentController {

    private final CommunityCommentService commentService;

    public CommunityCommentController(CommunityCommentService commentService) {
        this.commentService = commentService;
    }

    // ------------------------------------
    // 1. 댓글 생성 (POST)
    // POST /api/community/posts/{postId}/comments
    // ------------------------------------
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto requestDto) {
        
        // Service에서 댓글 생성 및 게시글 존재 여부 확인
        return commentService.createComment(postId, requestDto)
                .map(comment -> ResponseEntity.status(HttpStatus.CREATED).body(comment)) // 생성 성공 (201)
                .orElseGet(() -> ResponseEntity.notFound().build()); // 게시글 ID가 없는 경우 (404)
    }

    // ------------------------------------
    // 2. 댓글 목록 조회 (GET)
    // GET /api/community/posts/{postId}/comments
    // ------------------------------------
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        // 댓글 목록만 조회. 게시글 존재 여부는 클라이언트가 상세 페이지 로드 시 이미 확인했다고 가정합니다.
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
        
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(comments); // 200 OK
    }

    // ------------------------------------
    // 3. 댓글 수정 (PUT)
    // PUT /api/community/posts/{postId}/comments/{commentId}
    // ------------------------------------
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            // postId는 URL 구조의 일관성을 위해 받지만, Service에서 사용하지 않아도 무방합니다.
            @Valid @RequestBody CommentRequestDto requestDto) {
        
        return commentService.updateComment(commentId, requestDto)
                .map(ResponseEntity::ok) // 수정 성공 (200)
                .orElseGet(() -> {
                    // Service에서 비밀번호 불일치(403) 또는 댓글 ID 없음(404) 반환
                    // 보안상 403 Forbidden으로 통합 응답
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); 
                });
    }

    // ------------------------------------
    // 4. 댓글 삭제 (DELETE)
    // DELETE /api/community/posts/{postId}/comments/{commentId}
    // ------------------------------------
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) { // 삭제 비밀번호를 Body로 받습니다.

        boolean isDeleted = commentService.deleteComment(commentId, requestDto.getPassword());

        if (isDeleted) {
            return ResponseEntity.noContent().build(); // 204 No Content (삭제 성공)
        } else {
            // 삭제 실패 (비밀번호 불일치 또는 ID 없음)
            // 403 Forbidden 응답
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); 
        }
    }
}