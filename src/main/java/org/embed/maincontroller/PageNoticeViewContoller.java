package org.embed.maincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller 
@RequestMapping("/page/notice")
public class PageNoticeViewContoller {
    // GET /page/notice 요청 시, notice_list.html 반환
    @GetMapping
    public String noticeList() { 
        return "page/notice_list"; 
    }
    // GET /page/notice/detail/{id} 요청 시, notice_detail.html 반환
    @GetMapping("/detail/{id}")
    public String noticeDetail(@PathVariable Long id) {
        return "page/notice_detail";
    }
}