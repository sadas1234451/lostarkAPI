package org.embed.maincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller 
@RequestMapping("/community/posts") // 기본 URL: /community/posts
public class CommunityPostViewContoller {

    // ------------------------------------
    // 1. 게시글 목록 페이지
    // GET /community/posts
    // ------------------------------------
    @GetMapping
    public String postList() { 
        return "/page/post_list"; // templates/community/post_list.html
    }
    
    // ------------------------------------
    // 2. 게시글 상세 페이지
    // GET /community/posts/detail/{id}
    // ------------------------------------
    @GetMapping("/detail/{postId}")
    public String postDetailView(@PathVariable Long postId, Model model) {
        // 상세 페이지에서 필요한 경우 Model에 데이터를 담아 전달할 수 있지만,
        // 비회원 게시판은 대부분 API 호출로 데이터를 로드하므로 현재는 단순 뷰 반환만 합니다.
        return "/page/post_detail"; // templates/community/post_detail.html
    }
    
    // ------------------------------------
    // 3. 게시글 작성 페이지
    // GET /community/posts/write
    // ------------------------------------
    @GetMapping("/write")
    public String postWriteView() {
        return "/page/post_write"; // templates/community/post_write.html
    }

    // ------------------------------------
    // 4. 게시글 수정 페이지
    // GET /community/posts/edit/{id}
    // ------------------------------------
    @GetMapping("/edit/{postId}")
    public String postEditView(@PathVariable Long postId, Model model) {
        // 수정 페이지는 해당 ID를 JavaScript가 사용할 수 있도록 URL에 포함합니다.
        return "/page/post_edit"; // templates/community/post_edit.html
    }
}