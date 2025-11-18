package org.embed.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.embed.DBService.PostRequestDto;
import org.embed.DBService.PostResponseDto;
import org.embed.entity.CommunityPost;
import org.embed.repository.CommunityPostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final PasswordEncoder encoder;

    public CommunityPostService(CommunityPostRepository communityPostRepository, PasswordEncoder encoder) {
        this.communityPostRepository = communityPostRepository;
        this.encoder = encoder;
    }
    //게시글 작성
    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto){
        String hashedPassword = encoder.encode(requestDto.getPassword());

        CommunityPost newPost = new CommunityPost(requestDto.getTitle(), requestDto.getContent(), requestDto.getAuthorNickname(), hashedPassword
        );

        CommunityPost savedPost = communityPostRepository.save(newPost);
        return new PostResponseDto(savedPost);
    }
    //개시글 조회
    @Transactional(readOnly = true)
    public  List<PostResponseDto> getAllPost() {
    List<CommunityPost> posts = communityPostRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

    return  posts.stream()
            .map(post -> new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getAuthorNickname(),
                post.getViewCount(),
                post.getCreatedDate(),
                post.getModifiedDate()))
                .collect(Collectors.toList());
                
    }

    // ------------------------------------
    // 3. 게시글 상세 조회 (Read - One)
    // ------------------------------------
    @Transactional
    public Optional<PostResponseDto> getPostDetail(Long postId) {
        // 1. ID로 게시글 조회
        Optional<CommunityPost> postOptional = communityPostRepository.findById(postId);

        if (postOptional.isPresent()) {
            CommunityPost post = postOptional.get();
            
            // 2. 조회수 증가 로직
            post.incrementViewCount();
            
            // postRepository.save(post); // @Transactional 덕분에 생략 가능 (Dirty Checking)

            // 3. DTO로 변환하여 반환
            return Optional.of(new PostResponseDto(post));
        }

        return Optional.empty(); // 게시글이 없으면 빈 Optional 반환
    }

    // ------------------------------------
    // 4. 게시글 수정 (Update)
    // ------------------------------------
    @Transactional
    public Optional<PostResponseDto> updatePost(Long postId, PostRequestDto requestDto) {
        // 1. ID로 게시글 조회
        Optional<CommunityPost> postOptional = communityPostRepository.findById(postId);

        if (postOptional.isPresent()) {
            CommunityPost post = postOptional.get();

            // 2. 비밀번호 검증
            if (!encoder.matches(requestDto.getPassword(), post.getPasswordHash())) {
                // 비밀번호가 일치하지 않으면 빈 Optional 반환 (Controller에서 403 처리)
                return Optional.empty(); 
            }

            // 3. 제목 및 내용 업데이트
            post.update(requestDto.getTitle(), requestDto.getContent());

            // 4. DTO로 변환하여 반환
            // postRepository.save(post); // @Transactional에 의해 자동 저장

            return Optional.of(new PostResponseDto(post));
        }

        return Optional.empty(); // 게시글 ID가 존재하지 않음
    }

    // ------------------------------------
    // 5. 게시글 삭제 (Delete)
    // ------------------------------------
    @Transactional
    public boolean deletePost(Long postId, String password) {
        // 1. ID로 게시글 조회
        Optional<CommunityPost> postOptional = communityPostRepository.findById(postId);

        if (postOptional.isPresent()) {
            CommunityPost post = postOptional.get();

            // 2. 비밀번호 검증
            if (!encoder.matches(password, post.getPasswordHash())) {
                // 비밀번호 불일치
                return false; 
            }

            // 3. 삭제 실행
            communityPostRepository.delete(post);
            return true; // 삭제 성공
        }

        // 게시글 ID가 존재하지 않음
        return false;
    }
}
