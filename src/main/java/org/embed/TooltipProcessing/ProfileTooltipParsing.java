package org.embed.TooltipProcessing;

import lombok.Data;
//데이터 명 시작은 무조건 대문자
//전투력/ 치특신
//파싱 데이터 저장 처리 순위 3순위
@Data
public class ProfileTooltipParsing {
    private String fatal;//치명
    private String Specialization;//특화
    private String speed;//신속

}
