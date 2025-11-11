package org.embed.TooltipProcessing;

import lombok.Data;
@Data
public class GemTooltipParsing {
    //대문자로 추가
    //장착 보석 목록 String으로 저장
    //보석이름
    private String Name;
    //보석레벨
    private String Level;
    //보석아이콘
    private String Icon;
    //보석 툴팁 -> 스킬 효과 뽑아내는데 사용
    private String Tooltip;

    private String SkillName;
    private String SkillValue;
    private String SkillPercentage;
}
