package org.embed.TooltipProcessing;

import org.embed.DBService.CharacterGem;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class CharacterGemTooltip {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public GemTooltipParsing extractSummary(CharacterGem gem) throws Exception{
        
        GemTooltipParsing gemparsing = new GemTooltipParsing();
        gemparsing.setIcon(gem.getGemsIcon());
        gemparsing.setLevel(gem.getGemsLevel());
        //보석 이름 클린 업 처리
        String cleanGems = gem.getGemsName();
        String cleanName = removeHtmlTags(cleanGems);
        gemparsing.setName(cleanName);

        if (gem.getGemsTooltip() == null || gem.getGemsTooltip().isEmpty()) {
            return gemparsing;
        }
        
        try {
             
             JsonNode rootNode = objectMapper.readTree(gem.getGemsTooltip());
            //보석 툴팁(세부사항 위치 정의)
            //정제할 툴팁 세부 위치 정의하고 if문의로 텍스트 클린업 ex)장비탭
            JsonNode effectNode = rootNode.path("Element_007").path("value").path("Element_001");
            if (effectNode.isMissingNode() || !effectNode.isTextual()) {
                log.warn("보석 툴팁에서 Element_007/Element_001 노드가 텍스트가 아니거나 누락됨: {}", gem.getGemsName());
                return gemparsing; // 유효한 텍스트가 아니므로 부분 파싱된 객체 반환
            }
            String rawEffectText = effectNode.asText();
            String cleanEffectText = removeHtmlTags(rawEffectText);

            // ⭐️ 1. mainEffectText 정의 (필수 추가 로직)
            int index = cleanEffectText.indexOf("추가 효과");
            String mainEffectText = (index != -1) ? cleanEffectText.substring(0, index).trim() : cleanEffectText.trim();

           int end;
            // 1. 홍염 체크
            if (mainEffectText.contains("재사용 대기시간")) {
                end = mainEffectText.indexOf("재사용 대기시간");
            } 
            // 2. 멸화 체크
            else if (mainEffectText.contains("피해")) {
                end = mainEffectText.indexOf("피해");
            } 
            // 3. 예외 처리 (인게임 오류 방지)
            else {
                // 인게임 오류 발생 상황 가정 및 방어 코드 실행
                log.warn("[{}] 보석 효과 텍스트에서 키워드(피해/재사용 대기시간)를 찾을 수 없음. 데이터 스킵 처리.", cleanName);
                return gemparsing;
            }
            int start = cleanEffectText.indexOf(']') + 1;

            String skillName = mainEffectText.substring(start, end).trim(); //스킬이름
            String effectValue = mainEffectText.substring(end).split(" ")[0].trim(); //스킬 효과 피해/재사용 대기시간 감소
            String percentage = mainEffectText.substring(end).split(" ")[1].trim(); // 효과의 % 값

            //각각 따로 파싱(html사용 유용)
            gemparsing.setSkillName(skillName);
            gemparsing.setSkillValue(effectValue);
            gemparsing.setSkillPercentage(percentage);
        } catch (Exception e) {
            log.error("[{}] 보석 툴팁 파싱 중 처리 오류 발생", cleanName, e);
        }

        return gemparsing;
    }

    public String removeHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        // <...> 형태의 HTML 태그를 제거하는 정규 표현식
        String noHtml = html.replaceAll("<[^>]*>", ""); 
        
        // 추가로 불필요한 엔터키 (\r, \n) 및 공백 제거
        noHtml = noHtml.replaceAll("\\s+", " ").trim();
        
        return noHtml;
    }
}

