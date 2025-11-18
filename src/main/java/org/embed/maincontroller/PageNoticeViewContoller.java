package org.embed.maincontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller 
@RequestMapping("/page/notice")
public class PageNoticeViewContoller {
    // GET /page/notice 요청 시, notice_list.html 반환
    @GetMapping
    public String noticeList() { 
        return "/page/notice_list"; 
    }
    @GetMapping("/detail/{id}")
    public String noticeDetailView(@PathVariable Long id, Model model) {
        return "/page/notice_detail";
    }
    @GetMapping("/edit/{id}")
    public String noticeEditView(@PathVariable Long id, Model model){
        return "/page/notice_edit";
    }
}