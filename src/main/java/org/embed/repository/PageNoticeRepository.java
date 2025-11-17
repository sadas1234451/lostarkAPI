package org.embed.repository;

import java.util.List;

import org.embed.entity.PageNotice;
import org.springframework.data.jpa.repository.JpaRepository;


//PageNotice와 DB를 연결하는 인터페이스
public interface PageNoticeRepository extends JpaRepository<PageNotice, Long> {
    
    //모든 공지사항 내림차순 조회
    List<PageNotice> findAllByOrderByCreatedDateDesc();
    
}