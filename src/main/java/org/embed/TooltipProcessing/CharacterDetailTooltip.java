package org.embed.TooltipProcessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.embed.DBService.CharacterDetail;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CharacterDetailTooltip {
     private final ObjectMapper objectMapper = new ObjectMapper();
     
     public TooltipParsing extractSummary(CharacterDetail itemDetail) throws Exception{
          
          TooltipParsing parsing = new TooltipParsing();

          parsing.setType(itemDetail.getType());
          parsing.setNameTagBox(itemDetail.getName());
          parsing.setIcon(itemDetail.getIcon());
          if (itemDetail.getTooltip() == null || itemDetail.getTooltip().isEmpty()) {
               return parsing;
          }
          try{
               JsonNode rootNode = objectMapper.readTree(itemDetail.getTooltip());

               JsonNode e000Value = rootNode.path("Element_000").path("value");
               if (e000Value.isTextual()) {

                    String rawText = e000Value.asText();
                    parsing.setNameTagBox(rawText.replaceAll("<[^>]*>", "").trim());
                     log.info(parsing.getNameTagBox());
               }

               JsonNode e001Value = rootNode.path("Element_001").path("value");
               if (e001Value.isObject()) {
                    //parsing.setItemTitle(e001Value.path("leftStr2").asText());

                    String firstrawText = e001Value.path("leftStr2").asText();
                    String firstcleanText = firstrawText.replaceAll("<[^>]*>", "").trim();
                    //String currentItemtitle = parsing.getItemTitle();

                    parsing.setItemTitle(firstcleanText);
                    JsonNode secondItemtitleNode = e001Value.path("Element_001");
                    
                    if (secondItemtitleNode.isTextual()) {
                         String secondaryRaw = secondItemtitleNode.asText();
                         String secondaryClean = secondaryRaw.replaceAll("<[^>]*>", "").trim();

                         String currentItemtitle = parsing.getItemTitle();
                         
                         if (!secondaryClean.isEmpty()) {
                                parsing.setItemTitle(currentItemtitle + "\n" + secondaryClean);
                              
                         }
                    }
               }//품질, 아이템 레벨 등
               //기본효과
               JsonNode e006Value = rootNode.path("Element_006").path("value");
               if (e006Value.isObject() && e006Value.has("Element_001")) {
                    
                    String rawText = e006Value.path("Element_001").asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", "").trim();
                    String finalResult = cleanText.replaceAll("(\\d[\\%\\+\\.]?)([가-힣])", "$1<br>$2");
                    log.info("기본 효과 cleanText 줄바꿈: {}", finalResult);
                    parsing.setItemPartBasicBox(finalResult ); 
               }//기본효과


               JsonNode e008Value = rootNode.path("Element_008").path("value");
               if(e008Value.isObject() && e008Value.has("Element_001")){

                    String rawText = e008Value.path("Element_001").asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", "").trim();

                    
                    log.info("추가 효과 cleanText 정리된 텍스트: {}", cleanText);
                    
                    parsing.setItemPartOptionalBox(cleanText);
               }//추가 효과(상급재련)

               // 초월 단계 및 수치
               JsonNode e009TopStr = rootNode 
                                             .path("Element_009")
                                             .path("value")
                                             .path("Element_000")
                                             .path("topStr");
               String transcendenceSummary = " "; 
               if (e009TopStr.isTextual()) {
                    
                    String rawText = e009TopStr.asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", "").replaceAll("\r\n", " ").trim();

                    Pattern pattern = Pattern.compile("\\[초월\\]\\s*(\\d+)\\s*단계\\s*(\\d+)");
                    Matcher matcher = pattern.matcher(cleanText);
                    if (matcher.find()) {
                         String level = matcher.group(1);
                         String totalvalue = matcher.group(2);
                         transcendenceSummary = level + "단계 " + totalvalue;
                         parsing.setIndentStringGroup(transcendenceSummary);
                         log.info(transcendenceSummary);
               } 
               }else {
                         transcendenceSummary = "0";
               }

               // 무기 초월 단계 및 수치
               JsonNode e010WeponTopStr = rootNode 
                                             .path("Element_010")
                                             .path("value")
                                             .path("Element_000")
                                             .path("topStr");// 루트 경로
               String transcendenceSummaryWepon = " "; // 저장  할 변수
               if (e010WeponTopStr.isTextual()) {
                    
                    String rawText = e010WeponTopStr.asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", "").replaceAll("\r\n", " ").trim();//html 태그 제거 및 공백 정리

                    Pattern pattern = Pattern.compile("\\[초월\\]\\s*(\\d+)\\s*단계\\s*(\\d+)");
                    Matcher matcher = pattern.matcher(cleanText);
                    if (matcher.find()) {
                         String level = matcher.group(1);
                         String totalvalue = matcher.group(2);
                         transcendenceSummaryWepon = level + "단계 " + totalvalue;
                         parsing.setIndentStringGroupWeapon(transcendenceSummaryWepon);
                         log.info(transcendenceSummaryWepon);
               } 
               }else {
                         transcendenceSummaryWepon = "0";
               }

               //상급재련 단계
               JsonNode e005Value = rootNode
                                             .path("Element_005")
                                             .path("value"); 
               if( e005Value.isTextual()){

                    String rawText = e005Value.asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", "");
                    
                    Pattern pattern = Pattern.compile("\\[상급 재련\\]\\s*(\\d+)\\s*단계");
                    Matcher matcher = pattern.matcher(cleanText);
                    String reforgeLevel = " ";
                    if (matcher.find()) {
                         reforgeLevel = matcher.group(1);
                         log.info("상급재련 단계: " + reforgeLevel);
                    } else {
                         reforgeLevel = "0";
                    }

                    parsing.setSingleTextBox(reforgeLevel);

                    
               }

               //엘릭서 추출
               JsonNode e010Content = rootNode
                                             .path("Element_010")
                                             .path("value")
                                             .path("Element_000") // Element_000의 contentStr 경로까지 접근
                                             .path("contentStr");

               if (e010Content.isObject()) {
               
               String slot1Option = "";
               JsonNode slot1Node = e010Content.path("Element_000").path("contentStr");
               
               if (slot1Node.isTextual()) {
                    String rawText = slot1Node.asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", ""); 
                    String[] lines = cleanText.split("\\n|<br>|<BR>");
                    String mainOption = lines[0].trim();
                    String finalOption = mainOption.replaceAll("\\[[가-힣]*\\]\\s*", "");
                    
                    if (finalOption.contains("Lv.")) {
                         Pattern pattern = Pattern.compile("([^\\s]* Lv\\.\\d+)");
                         Matcher matcher = pattern.matcher(finalOption);
                         if (matcher.find()) {
                              finalOption = matcher.group(1).trim();
                         }
                    }
                    slot1Option = finalOption;
               }
               parsing.setElixirOption1(slot1Option); 
               
               
               String slot2Option = "";
               JsonNode slot2Node = e010Content.path("Element_001").path("contentStr"); 
               
               if (slot2Node.isTextual()) {
                    String rawText = slot2Node.asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", ""); 
                    String[] lines = cleanText.split("\\n|<br>|<BR>");
                    String mainOption = lines[0].trim();
                    String finalOption = mainOption.replaceAll("\\[[가-힣]*\\]\\s*", "");
        
                    // 'Lv.' 뒤에 붙은 상세 스탯 잔여물 제거
                    if (finalOption.contains("Lv.")) {
                         Pattern pattern = Pattern.compile("([^\\s]* Lv\\.\\d+)");
                         Matcher matcher = pattern.matcher(finalOption);
                         if (matcher.find()) {
                              finalOption = matcher.group(1).trim();
                         }
                    }
                    slot2Option = finalOption;
               }
               parsing.setElixirOption2(slot2Option);
               
               log.info("엘릭서 옵션 1: {}, 엘릭서 옵션 2: {}", slot1Option, slot2Option);
               }
     }catch (Exception e){
               // 에러 발생 시 디버깅을 위해 출력
               System.err.println("장비 [" + itemDetail.getName() + "]의 Tooltip 파싱 중 오류 발생: " + e.getMessage());
               e.printStackTrace();
               throw new Exception("TooltipParsing Error: " + e.getMessage());
          }

          return parsing;
     }
}
