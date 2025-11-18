package org.embed.maincontroller;

import org.embed.service.CommunityPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

import org.embed.DBService.PostRequestDto;
import org.embed.DBService.PostResponseDto;

@RestController
@RequestMapping("/api/community/posts")
@Validated // @PathVariable 등의 유효성 검사를 위해 추가 (선택적)
public class CommunityPostController {

    private final CommunityPostService postService;

    public CommunityPostController(CommunityPostService postService) {
        this.postService = postService;
    }

    // ------------------------------------
    // 1. 게시글 생성 (POST)
    // ------------------------------------
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto) {
        // @Valid를 통해 DTO의 유효성 검사(4자리 숫자 비밀번호 등)가 자동으로 수행됩니다.
        PostResponseDto newPost = postService.createPost(requestDto);
        // 201 Created 응답 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(newPost);
    }

    // ------------------------------------
    // 2. 게시글 목록 조회 (GET - All)
    // ------------------------------------
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> posts = postService.getAllPost();
        if (posts.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(posts); // 200 OK
    }

    // ------------------------------------
    // 3. 게시글 상세 조회 (GET - One)
    // ------------------------------------
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPostDetail(@PathVariable Long postId) {
        // 상세 조회 시, Service에서 조회수가 증가됩니다.
        return postService.getPostDetail(postId)
                .map(ResponseEntity::ok) // 게시글이 있으면 200 OK 반환
                .orElseGet(() -> ResponseEntity.notFound().build()); // 없으면 404 Not Found
    }

    // ------------------------------------
    // 4. 게시글 수정 (PUT)
    // ------------------------------------
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto requestDto) {
        
        return postService.updatePost(postId, requestDto)
                .map(ResponseEntity::ok) // 수정 성공 시 (비밀번호 일치, 게시글 존재)
                .orElseGet(() -> {
                    // Service에서 Optional.empty()가 반환되는 두 가지 경우 처리:
                    // 1. 비밀번호 불일치 (403 Forbidden)
                    // 2. 게시글 ID 없음 (404 Not Found)
                    // 현재 서비스 로직은 비밀번호 불일치와 ID 없음 모두 empty를 반환하므로, 
                    // 두 경우 중 하나로 처리합니다. 여기서는 403 또는 404로 응답을 시도합니다.
                    // 실제로는 403을 반환하는 것이 더 적절합니다.
                    
                    // 단순화를 위해, 여기서는 403을 기본으로 반환하거나, 좀 더 상세한 에러 처리를 위해 
                    // Service에서 비밀번호 불일치와 ID 없음 케이스를 분리하는 것이 이상적입니다.
                    // (현재 Service 구조상 404로 통합 처리하는 것이 간결합니다.)
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden (비밀번호 오류 가정)
                });
    }

    // ------------------------------------
    // 5. 게시글 삭제 (DELETE)
    // ------------------------------------
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestBody PostRequestDto requestDto) { // 삭제 시에도 비밀번호를 Body로 받습니다.

        boolean isDeleted = postService.deletePost(postId, requestDto.getPassword());

        if (isDeleted) {
            return ResponseEntity.noContent().build(); // 204 No Content (삭제 성공)
        } else {
            // 삭제 실패 (비밀번호 불일치 또는 ID 없음)
            // 비회원 게시판에서는 보안상 403/404 구분을 사용자에게 명확히 알리지 않는 것이 좋습니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized 또는 403 Forbidden
        }
    }
}
