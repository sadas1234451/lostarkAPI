package org.embed.service;

import org.embed.DBService.CommentRequestDto;
import org.embed.DBService.CommentResponseDto;
import org.embed.entity.CommunityComment;
import org.embed.entity.CommunityPost;
import org.embed.repository.CommunityCommentRepository;
import org.embed.repository.CommunityPostRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityCommentService {

    private final CommunityCommentRepository commentRepository;
    private final CommunityPostRepository postRepository; // 게시글 존재 여부 확인용
    private final PasswordEncoder passwordEncoder;

    public CommunityCommentService(
            CommunityCommentRepository commentRepository,
            CommunityPostRepository postRepository,
            PasswordEncoder passwordEncoder) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------
    // 1. 댓글 생성 (Create)
    // ------------------------------------
    @Transactional
    public Optional<CommentResponseDto> createComment(Long postId, CommentRequestDto requestDto) {
        // 1. 댓글을 달 게시글이 존재하는지 확인
        Optional<CommunityPost> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return Optional.empty(); // 게시글이 없으면 댓글 생성 불가
        }
        CommunityPost post = postOptional.get();

        // 2. 평문 비밀번호를 해시화
        String hashedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. 엔티티 생성 (게시글 엔티티와 해시된 비밀번호 포함)
        CommunityComment newComment = new CommunityComment(
                post,
                requestDto.getContent(),
                requestDto.getAuthorNickname(),
                hashedPassword
        );

        // 4. DB에 저장 및 DTO로 변환하여 반환
        CommunityComment savedComment = commentRepository.save(newComment);
        return Optional.of(new CommentResponseDto(savedComment));
    }

    // ------------------------------------
    // 2. 특정 게시글의 댓글 목록 조회 (Read - All by Post)
    // ------------------------------------
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        // Repository에서 정의한 쿼리 메서드 사용: 게시글 ID로 조회 후 ID 오름차순 정렬
        List<CommunityComment> comments = commentRepository.findAllByPost_IdOrderByIdAsc(postId);

        // DTO로 변환하여 반환
        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    // ------------------------------------
    // 3. 댓글 수정 (Update)
    // ------------------------------------
    @Transactional
    public Optional<CommentResponseDto> updateComment(Long commentId, CommentRequestDto requestDto) {
        // 1. 댓글 ID로 엔티티 조회
        Optional<CommunityComment> commentOptional = commentRepository.findById(commentId);
        
        if (commentOptional.isEmpty()) {
            return Optional.empty(); // 댓글 ID가 존재하지 않음 (404 처리용)
        }
        CommunityComment comment = commentOptional.get();

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), comment.getPasswordHash())) {
            return Optional.empty(); // 비밀번호 불일치 (403 처리용)
        }

        // 3. 내용 업데이트
        comment.update(requestDto.getContent());

        // 4. DTO로 변환하여 반환
        // commentRepository.save(comment); // @Transactional에 의해 자동 저장
        return Optional.of(new CommentResponseDto(comment));
    }

    // ------------------------------------
    // 4. 댓글 삭제 (Delete)
    // ------------------------------------
    @Transactional
    public boolean deleteComment(Long commentId, String password) {
        // 1. 댓글 ID로 엔티티 조회
        Optional<CommunityComment> commentOptional = commentRepository.findById(commentId);

        if (commentOptional.isPresent()) {
            CommunityComment comment = commentOptional.get();

            // 2. 비밀번호 검증
            if (!passwordEncoder.matches(password, comment.getPasswordHash())) {
                return false; // 비밀번호 불일치
            }

            // 3. 삭제 실행
            commentRepository.delete(comment);
            return true; // 삭제 성공
        }

        // 댓글 ID가 존재하지 않음
        return false;
    }
}