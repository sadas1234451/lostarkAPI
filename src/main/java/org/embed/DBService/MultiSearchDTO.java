package org.embed.DBService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiSearchDTO {
    
    // 1. 캐릭터 기본 정보
    private String characterName;
    private String className;
    private String itemLevel;
    private String combatPower;

    // 2. 특성 3종 (String 타입 유지)
    private String statCrit;            // 치명
    private String statSwiftness;       // 신속
    private String statSpecialization;  // 특화
    
    // 3. 보석 요약 (총 보석 개수)
    private int totalGemCount;          // 총 보석 개수
}