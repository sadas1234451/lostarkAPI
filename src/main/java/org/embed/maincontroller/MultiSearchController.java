package org.embed.maincontroller;

import org.embed.service.MultiSearchService;
import org.embed.DBService.MultiSearchDTO; // 최종 DTO import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

@Slf4j
@RestController
public class MultiSearchController {
    
    private final MultiSearchService multiSearchService;

    // 생성자 주입
    public MultiSearchController(MultiSearchService multiSearchService){
        this.multiSearchService = multiSearchService;
    }
    
    /**
     * 여러 캐릭터 이름을 쉼표로 구분하여 받아, 각 캐릭터의 요약 정보를 반환합니다.
     * 예: GET /api/multi-search?charNames=캐릭터A,캐릭터B,캐릭터C
     * @param charNames 쉼표로 구분된 캐릭터 이름 문자열
     * @return List<MultiSearchDTO> 캐릭터별 요약 정보 목록
     */
    @GetMapping("/api/multi-search")
    // 반환 타입을 List<MultiSearchDTO>로 변경
    public List<MultiSearchDTO> getMultiCharacterData(@RequestParam("charNames") String charNames) { 
        
        if (charNames == null || charNames.trim().isEmpty()) {
            log.warn("API 호출: 멀티 서치 요청, 빈 검색어 목록");
            return Collections.emptyList();
        }

        // 1. 쉼표로 구분된 문자열을 캐릭터 이름 목록(List<String>)으로 변환
        List<String> characterNames = Arrays.stream(charNames.split(","))
                                                 .map(String::trim)
                                                 .filter(name -> !name.isEmpty())
                                                 .toList();

        if (characterNames.isEmpty()) {
            log.warn("API 호출: 유효한 캐릭터 이름이 포함되지 않았습니다.");
            return Collections.emptyList();
        }

        log.info("API 호출: 멀티 캐릭터 요약 정보 요청. 대상: {}", characterNames);
        
        try {
            // 2. Service에 멀티 캐릭터 정보 요청 로직을 위임합니다.
            // Service 메소드명에 맞게 호출 (getMultiCharacterData 또는 getMultiCharacterSummaries)
            // 여기서는 getMultiCharacterData로 통일합니다.
            List<MultiSearchDTO> summaries = multiSearchService.getMultiCharacterSummaries(characterNames); 
            
            log.info("멀티 서치 성공. 총 {}개 캐릭터 정보 반환.", summaries.size());
            return summaries;
        } catch (Exception e) {
            log.error("멀티 캐릭터 정보 처리 중 오류 발생:", e);
            // 전체 실패 시 빈 리스트 반환
            return Collections.emptyList(); 
        }
    }
}