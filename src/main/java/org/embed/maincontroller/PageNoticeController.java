package org.embed.maincontroller;

import java.util.List;
import java.util.Optional;

import org.embed.entity.NoticeRequestDto;
import org.embed.entity.PageNotice;
import org.embed.service.PageNoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/page/notice")
public class PageNoticeController {

    private final PageNoticeService pageNoticeService;

    public PageNoticeController(PageNoticeService pageNoticeService) {
        this.pageNoticeService = pageNoticeService;
    }
    //공지 작성. 어드민 권한 필수.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PageNotice> createNotice(
        @RequestBody NoticeRequestDto requestDto,
        @AuthenticationPrincipal UserDetails userDetails){
            
            String authorUsername = userDetails.getUsername();

            PageNotice newNotice = pageNoticeService.createdNotice(requestDto, authorUsername);
            
            return ResponseEntity.ok(newNotice);
        }
    //공지사항 목록 조회. 모두 가능
    @GetMapping
    public ResponseEntity<List<PageNotice>> getAllNotices(){
        List<PageNotice> notices = pageNoticeService.getAllNotices();

        if(notices.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(notices);
    }
    //공지사항 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PageNotice> getNoticeDetail(@PathVariable Long id){
        Optional<PageNotice> notice = pageNoticeService.getNoticeById(id);



        return  notice.map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //공지사항 수정(관리자 전용)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PageNotice> updateNotice(
        @PathVariable Long id,
        @RequestBody NoticeRequestDto requestDto){
                
            Optional<PageNotice> updateNotice = pageNoticeService.updateNotice(id, requestDto);

            return updateNotice.map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());

        }
    //공지사항 삭제(관리자 전용)
    @PreAuthorize("hasRole('ADMIN')") // ⭐ ROLE_ADMIN 권한 필수 ⭐
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        
        boolean isDeleted = pageNoticeService.deleteNotice(id);
        
        if (isDeleted) {
            // 삭제 성공 시 204 No Content 반환
            return ResponseEntity.noContent().build();
        } else {
            // ID가 없으면 404 Not Found 반환
            return ResponseEntity.notFound().build();
        }
    }
}
