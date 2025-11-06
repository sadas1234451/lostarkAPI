package org.embed.TooltipProcessing;

import lombok.Data;

@Data
public class TooltipParsing {
    private String Type;// 아이템 종류(무기,방어구 등)
    private String NameTagBox; //아이템 이름
    private String Icon;//아이템 아이콘
    private String ItemTitle; // 품질, 아이템 레벨
    private String ItemPartBasicBox; // 기본효과
    private String ItemPartOptionalBox; // 추가효과
    private String IndentStringGroup; // 초월
    private String SingleTextBox; //상급재련
    private String IndentStringGroupWeapon; //무기 초월
    private String ElixirOption1;
    private String ElixirOption2;

}
