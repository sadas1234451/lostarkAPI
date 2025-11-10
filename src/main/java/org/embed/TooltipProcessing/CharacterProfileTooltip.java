package org.embed.TooltipProcessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.embed.DBService.CharacterProfile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
//프로필 파싱페이지 처리순위 2순위
@Slf4j
public class CharacterProfileTooltip {
     private final ObjectMapper objectMapper = new ObjectMapper();

     public ProfileTooltipParsing extractSummary(CharacterProfile profileDetail) throws Exception{
        
        ProfileTooltipParsing parsing = new ProfileTooltipParsing();

        JsonNode rootNode =objectMapper.readTree(profileDetail.getStats());

         String crit = extractCrit(rootNode);

         parsing.setCrit(crit);
         
        return parsing;
     }
     private String extractCrit(JsonNode rootNode){
      String permanentIncrease= "0";
      JsonNode statsNode = rootNode.path("Stats");

      if(statsNode.isArray()){
         for(JsonNode stat : statsNode){

            if("치명".equals(stat.path("Type").asText())){

               JsonNode tootlipNode = stat.path("Tooltip");

               if(tootlipNode.isArray() && tootlipNode.size() > 1){
                  String rawText = tootlipNode.get(1).asText();

                  Pattern pattern = Pattern.compile("보상 효과로 <font[^>]*>(\\\\d+)</font>만큼 영구적으로 증가되었습니다.");
                  Matcher matcher = pattern.matcher(rawText);

                  if(matcher.find()){
                     permanentIncrease = matcher.group(1).trim();

                     break;
                  }
               }
            }
         }
      }
      log.info(permanentIncrease);
      return  permanentIncrease;
  }
}
