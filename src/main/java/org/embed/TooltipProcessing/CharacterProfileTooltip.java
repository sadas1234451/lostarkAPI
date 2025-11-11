package org.embed.TooltipProcessing;

import org.embed.DBService.CharacterProfile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
//프로필 파싱페이지 처리순위 2순위
@Slf4j
@Service
public class CharacterProfileTooltip {
     private final ObjectMapper objectMapper = new ObjectMapper();

     public ProfileTooltipParsing extractSummary(CharacterProfile profileDetail) throws Exception{
        
        ProfileTooltipParsing parsing = new ProfileTooltipParsing();
         
        JsonNode statsArrayNode = objectMapper.readTree(profileDetail.getCharacterStats());
         //기본 스텟
         String fatalValue = extractStatValue(statsArrayNode, "치명");
         String specializationValue = extractStatValue(statsArrayNode, "특화");
         String speedValue = extractStatValue(statsArrayNode, "신속");
         //전투력
         


         //스텟, 전투력 파싱 후 저장
         parsing.setFatal(fatalValue);
         parsing.setSpecialization(specializationValue);
         parsing.setSpeed(speedValue);

         //로그 표시로 재대로 받는지 확인
        log.info("파싱된 스탯 (Value만): 치명={} | 특화={} | 신속={}", 
                 fatalValue, specializationValue, speedValue);
        
        return parsing;
     }
     private String extractStatValue(JsonNode statsArrayNode, String statType){

      if(statsArrayNode.isArray()){
         for(JsonNode stat : statsArrayNode){
            if(statType.equals(stat.path("Type").asText())){
               return stat.path("Value").asText("0");
            }
         }
      }
      return  "0";
  }
}
