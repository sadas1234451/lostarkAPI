package org.embed.service;

import java.util.List; // 최종 요약 DTO
import java.util.stream.Collectors; // 아이템 레벨 추출을 위한 DTO

import org.embed.DBService.CharacterData; // 보석 정보를 위한 DTO
import org.embed.DBService.MultiSearchDTO; // 프로필 및 특성 정보를 위한 DTO
import org.embed.TooltipProcessing.GemTooltipParsing;
import org.embed.TooltipProcessing.ProfileTooltipParsing;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service // 스프링 빈으로 등록하여 컨트롤러가 주입받을 수 있도록 합니다.
public class MultiSearchService {

    // 단일 캐릭터의 API 호출 및 파싱 로직을 담당하는 CharacterService를 주입받습니다.
    private final CharacterService characterService;

    public MultiSearchService(CharacterService characterService) {
        this.characterService = characterService;
    }

    /**
     * 여러 캐릭터 이름을 받아, 각 캐릭터의 요약 정보를 병렬 처리하여 List로 반환합니다.
     * @param characterNames 검색할 캐릭터 이름 목록
     * @return 요약된 MultiSearchDTO 리스트
     */
    public List<MultiSearchDTO> getMultiCharacterSummaries(List<String> characterNames) {
        // 병렬 스트림을 사용하여 여러 캐릭터의 API 호출을 동시에 진행하여 대기 시간을 줄입니다.
        return characterNames.parallelStream()
                .map(this::getSingleCharacterSummary) 
                .filter(summary -> summary != null) // 정보 로드에 실패한 캐릭터는 제외합니다.
                .collect(Collectors.toList());
    }

    /**
     * 단일 캐릭터의 모든 필수 API 정보를 요청하고, 파싱 결과를 MultiSearchDTO로 통합합니다.
     * @param characterName 처리할 캐릭터 이름
     * @return MultiSearchDTO, 실패 시 null
     */
    private MultiSearchDTO getSingleCharacterSummary(String characterName) {
        try {
            log.info("캐릭터 [{}] 정보 수집 시작.", characterName);

            // 1. 필요한 모든 API 호출 및 파싱 로직 실행
            
            // A. 프로필 데이터 (전투력, 직업, 특성)
            ProfileTooltipParsing profileData = characterService.profiles(characterName);

            // B. 아이템 레벨 데이터를 포함하는 형제 캐릭터 리스트
            List<CharacterData> siblingData = characterService.CData(characterName);
            
            // C. 보석 데이터
            List<GemTooltipParsing> gemData = characterService.CharacterGemsList(characterName);

            // D. (요약에 직접 쓰이지 않더라도 API는 호출하여 데이터 처리 로직을 유지합니다.)
            characterService.CharacterDetailData(characterName);
            characterService.CharacterEngravingsData(characterName);

            // 2. 필수 데이터 유효성 검사
            if (profileData == null || profileData.getCombatPower() == null) {
                log.warn("캐릭터 [{}]의 필수 프로필 정보 로드 실패. 검색 결과에서 제외.", characterName);
                return null;
            }

            // 3. 통합 및 요약
            return buildMultiSearchDTO(characterName, siblingData, profileData, gemData);
            
        } catch (Exception e) {
            log.error("캐릭터 [{}] 정보 통합 중 오류 발생: {}", characterName, e.getMessage());
            return null; // 단일 캐릭터 실패는 무시하고 null 반환
        }
    }

    /**
     * API 호출 결과를 종합하여 최종 MultiSearchDTO를 생성합니다.
     * 요약 항목: 닉네임, 아이템레벨, 직업, 전투력, 특성 3종(String), 총 보석 개수
     */
    private MultiSearchDTO buildMultiSearchDTO(
            String currentCharacterName,
            List<CharacterData> siblingData,
            ProfileTooltipParsing profileData,
            List<GemTooltipParsing> gemData) {

        // 1. 아이템 레벨 찾기 (CData 결과에서 현재 캐릭터의 아이템 레벨 추출)
        String itemLevel = siblingData.stream()
                .filter(c -> currentCharacterName.equals(c.getCharacterName()))
                .findFirst()
                .map(CharacterData::getItemLevel)
                .orElse("정보 없음");
        
        // 2. 보석 요약: 장착된 총 보석 개수 (리스트 크기)
        int totalGemCount = gemData.size();

        // 3. MultiSearchDTO의 빌더를 사용하여 최종 객체 생성
        return MultiSearchDTO.builder()
                .characterName(currentCharacterName)
                .className(profileData.getClassName())
                .itemLevel(itemLevel)
                .combatPower(profileData.getCombatPower())
                
                // 특성 필드 (ProfileTooltipParsing의 Fatal, Speed, Specialization 필드에 매핑)
                .statCrit(profileData.getFatal()) 
                .statSwiftness(profileData.getSpeed())
                .statSpecialization(profileData.getSpecialization())
                
                .totalGemCount(totalGemCount)
                .build();
    }
}