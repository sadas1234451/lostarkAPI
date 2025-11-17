package org.embed.service;

import java.util.List;
import java.util.Optional;

import org.embed.entity.NoticeRequestDto;
import org.embed.entity.PageNotice;
import org.embed.repository.PageNoticeRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
@Service
public class PageNoticeService {
    
    private final PageNoticeRepository noticeRepository;

    public PageNoticeService(PageNoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Transactional
    public PageNotice createdNotice(NoticeRequestDto requsetDto, String author){
        PageNotice pageNotice = new PageNotice();
        //주입할 제목, 컨텐츠 
        pageNotice.setTitle(requsetDto.getTitle());
        pageNotice.setContent(requsetDto.getContent());

        pageNotice.setAuthor(author);

        return noticeRepository.save(pageNotice);
    }
    //공지사항 목록조회 최신순
    public  List<PageNotice> getAllNotices() {
        return  noticeRepository.findAllByOrderByCreatedDateDesc();
    }
    //공지사항 상세 조회
    public Optional<PageNotice> getNoticeById(Long id) {
        // JpaRepository의 기본 메소드인 findById를 사용하여 상세 정보를 조회
        return noticeRepository.findById(id);
    }
    //공지사항 수정
    @Transactional
    public Optional<PageNotice> updateNotice(Long id, NoticeRequestDto requestDto){
        Optional<PageNotice> optionalNotice = noticeRepository.findById(id);
        
        if(optionalNotice.isPresent()){
            //존재하면 내용 업데이트
            PageNotice notice = optionalNotice.get();
            notice.setTitle(requestDto.getTitle());
            notice.setContent(requestDto.getContent());
            
            return Optional.of(notice);
        }

        return Optional.empty();
    }
    //공지사항 삭제
    @Transactional
    public boolean deleteNotice(Long id){
        
        
        if(noticeRepository.existsById(id)){
            //존재하면 내용 삭제
            noticeRepository.deleteById(id);
            
            return true;
        }

        return false;
    }
}
