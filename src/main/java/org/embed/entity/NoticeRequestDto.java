package org.embed.entity;
//공지사항 작성 및 수정 요청 시 사용되는 데이터 전송 객체

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequestDto {
    private String title;
    private String content;

    
}
