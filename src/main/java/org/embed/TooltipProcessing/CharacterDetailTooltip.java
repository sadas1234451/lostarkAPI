package org.embed.TooltipProcessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
               //아이템 이름
               JsonNode e000Value = rootNode.path("Element_000").path("value");
               if (e000Value.isTextual()) {

                    String rawText = e000Value.asText();
                    parsing.setNameTagBox(rawText.replaceAll("<[^>]*>", "").trim());
                     log.info(parsing.getNameTagBox());
               }
               // 아이템 레벨
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
               }
               //팔찌 옵션
               // 팔찌 옵션
               if (itemDetail != null && itemDetail.getType().equals("팔찌")) {
               
               JsonNode e005braceletOption = rootNode
                    .path("Element_005")
                    .path("value");

               if (e005braceletOption.isObject()) {
                    
                    String rawText = e005braceletOption.path("Element_001").asText();
                    
                    if (rawText.isEmpty() || rawText.isBlank()) {
                         parsing.setBraceletOption("");
                         parsing.setBraceletPartOption("");
                    } else {
                         
                         // 1. <BR> 태그를 줄바꿈 문자(\n)로 치환하여 옵션들을 줄 단위로 분리 (핵심)
                         String lineSeparatedText = rawText.replaceAll("(?i)<\\s*BR\\s*>", "\n").trim();
                         
                         // 2. 모든 HTML 태그 제거 (단, \n은 유지됨)
                         String cleanText = lineSeparatedText.replaceAll("<[^>]*>", "").trim();
                         
                         // 3. 줄(\n) 단위로 분리하여 배열 생성
                         String[] options = cleanText.split("\n");
                         
                         List<String> baseStatsList = new ArrayList<>();
                         List<String> partOptionsList = new ArrayList<>();
                         
                         // ⭐️ 4. 허용되는 기본 스탯 이름 목록과 정규식 패턴 정의 ⭐️
                         // (체력|신속|치명|특화|제압|인내|숙련) +숫자 형태만 기본 스탯으로 인정
                         String statNames = "(체력|신속|치명|특화|제압|인내|숙련)"; 
                         Pattern baseStatPattern = Pattern.compile("^(" + statNames + "\\s*\\+\\d+)$");
                         
                         // 5. 배열 순회하며 기본 스탯과 부여 옵션 분리
                         for (String option : options) {
                              String trimmedOption = option.replaceAll("\\s{2,}", " ").trim();
                              
                              if (trimmedOption.isEmpty()) continue;
                              
                              Matcher baseStatMatcher = baseStatPattern.matcher(trimmedOption);
                              
                              if (baseStatMatcher.find()) {
                                   // 기본 스탯인 경우 (예: "신속 +105")
                                   baseStatsList.add(baseStatMatcher.group(1).trim());
                              } else {
                                   // 부여 옵션인 경우
                                   partOptionsList.add(trimmedOption);
                              }
                         }

                         // 6. 기본 스탯 포맷팅 (예: "신속 +105  치명 +116")
                         String baseStatsFormatted = String.join("  ", baseStatsList);
                         
                         // 7. 부여 옵션 포맷팅 (각 옵션 사이에 <br> 삽입)
                         String partOptionsFormatted = String.join("<br>", partOptionsList);

                         // 8. 최종 DTO 저장
                         // baseStatsFormatted: 신속 +105  치명 +116
                         // partOptionsFormatted: 적에게 주는 피해가 3% 증가하며, 무력화 상태의 적에게 주는 피해가 5% 증가한다.<br>치명타 적중률 +5.00%...
                         parsing.setBraceletOption(baseStatsFormatted); 
                         parsing.setBraceletPartOption(partOptionsFormatted);
                    }
               }
               }
               //기본효과
               JsonNode e006Value = rootNode.path("Element_006").path("value");
               if (e006Value.isObject() && e006Value.has("Element_001")) {
                    
                    String rawText = e006Value.path("Element_001").asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", "").trim();
                    String finalResult = cleanText.replaceAll("(\\d[\\%\\+\\.]?)([가-힣])", "$1<br>$2");
                    log.info("기본 효과 cleanText 줄바꿈: {}", finalResult);
                    parsing.setItemPartBasicBox(finalResult ); 
               }


               JsonNode e008Value = rootNode.path("Element_008").path("value");
               if(e008Value.isObject() && e008Value.has("Element_001")){

                    String rawText = e008Value.path("Element_001").asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", "").trim();

                    
                    log.info("추가 효과 cleanText 정리된 텍스트: {}", cleanText);
                    
                    parsing.setItemPartOptionalBox(cleanText);
               }//추가 효과(상급재련)

               // 초월 단계 및 수치
               String transcendenceSummary = "0"; 
               JsonNode targetNode = null;
               log.info("--- 초월 정보 파싱 시작 ---");

               // JSON 전체를 순회하면서 "topStr" 키를 가진 노드를 찾습니다.
               Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();

               while (fields.hasNext()) {
               Map.Entry<String, JsonNode> entry = fields.next();
               JsonNode elementNode = entry.getValue(); 
               
               // 1-1. Element_XXX의 value 노드 확인
               JsonNode valueNode = elementNode.path("value");

               if (valueNode.isObject()) {
                    
                    Iterator<String> subFieldNames = valueNode.fieldNames();
                    while (subFieldNames.hasNext()) {
                         String subElementKey = subFieldNames.next();
                         JsonNode subElementNode = valueNode.path(subElementKey);

                         JsonNode topStrNode = subElementNode.path("topStr");
                         
                         // 1-2. topStr 노드가 존재하는지 확인
                         if (!topStrNode.isMissingNode() && !topStrNode.isNull()) {
                              
                              String rawText = topStrNode.asText();
                              
                              // 1-3. topStr 안에 [초월] 키워드가 있는지 확인
                              if (rawText.contains("[초월]")) {
                                   log.info("DEBUG - [초월] 키워드 포함된 topStr 노드 확정");
                                   targetNode = topStrNode;
                                   break; 
                              } else {
                                   log.info("DEBUG - [초월] 키워드 없음 (엘릭서 또는 다른 정보일 가능성)");
                              }
                         }
                    }
                    if (targetNode != null) break;
               } 
               }
               // ------------------------------------------------------------------

               // 2. targetNode에서 문자열을 가져와 파싱을 시도합니다.
               if (targetNode != null) {
               String rawText = targetNode.asText();
               
               // HTML 태그와 개행 문자 제거
               String cleanText = rawText.replaceAll("<[^>]*>", "").replaceAll("\r\n", " ").trim();
               
               
               // 정규식: [초월]을 포함하여 단계와 숫자를 추출
               Pattern pattern = Pattern.compile("\\[초월\\].*?(\\d+)\\s*단계.*?(\\d+)"); 
               Matcher matcher = pattern.matcher(cleanText);

               if (matcher.find()) {
                    String level = matcher.group(1);
                    String totalValue = matcher.group(2); 
                    transcendenceSummary = level + "단계 " + totalValue;

                    parsing.setIndentStringGroup(transcendenceSummary);
                    
                    log.info("--- 초월 정보 찾음: {} 단계 {} ---", level, totalValue);
               } else {
                    log.warn("--- 초월 정규식 매칭 실패 --- cleanText: {}", cleanText);
               }
               } else {
               log.info("--- JSON 내에서 [초월] 키워드를 포함하는 topStr 노드를 찾지 못했습니다. ---");
               }
                // 최종 결과 저장

               // 무기 초월 단계 및 수치
               if (itemDetail.getType().equals("무기")) {
               String transcendenceSummaryWepon = "0"; // 저장할 변수 (초기값 '0')
               // JSON 전체를 순회하며 초월 정보(topStr)를 찾습니다.
               Iterator<Map.Entry<String, JsonNode>> weaponFields = rootNode.fields();
               
               boolean foundTranscendence = false;

               while (weaponFields.hasNext() && !foundTranscendence) {
                    Map.Entry<String, JsonNode> entry = weaponFields.next();
                    JsonNode elementNode = entry.getValue(); 
                    
                    JsonNode valueNode = elementNode.path("value");

                    if (valueNode.isObject()) {
                         
                         // value/Element_XXX 노드를 순회합니다. (초월 정보는 보통 Element_000 하위에 있음)
                         JsonNode subElementNode = valueNode.path("Element_000"); 

                         // 1. topStr 노드에 접근합니다.
                         JsonNode topStrNode = subElementNode.path("topStr"); 
                         
                         if (topStrNode.isTextual()) {
                              String rawText = topStrNode.asText();
                              
                              // 2. 초월 키워드 ([초월] 및 단계) 포함 여부 체크
                              if (rawText.contains("[초월]") && rawText.contains("단계")) {
                                   
                                   foundTranscendence = true; // 초월 정보를 찾았으므로 순회 중지
                                   
                                   // 3. HTML 태그 제거 및 공백 정리
                                   String cleanText = rawText.replaceAll("<[^>]*>", "").replaceAll("\r\n", " ").trim();
                                   
                                   // 4. 정규식을 사용하여 초월 단계 및 수치 추출
                                   // 패턴: [초월] 다음에 오는 단계(\d+)와 이모티콘 뒤에 오는 총 수치(\d+)를 찾습니다.
                                   // 예: [초월] 7단계 21
                                   Pattern pattern = Pattern.compile("\\[초월\\]\\s*(\\d+)\\s*단계.*?(\\d+)");
                                   Matcher matcher = pattern.matcher(cleanText);
                                   
                                   if (matcher.find()) {
                                   String level = matcher.group(1);
                                   String totalValue = matcher.group(2);
                                   
                                   transcendenceSummaryWepon = level + "단계 " + totalValue;
                                   
                                   } else {
                                   // 패턴이 매칭되지 않은 경우 (예: 초월 단계만 있고 수치가 없는 경우 등)
                                   Pattern simplePattern = Pattern.compile("\\[초월\\]\\s*(\\d+)\\s*단계");
                                   Matcher simpleMatcher = simplePattern.matcher(cleanText);
                                   if(simpleMatcher.find()){
                                        transcendenceSummaryWepon = simpleMatcher.group(1) + "단계";
                                   }
                                   }
                              } 
                         }
                    }
               }
               
               // 5. 최종 DTO 저장
               parsing.setIndentStringGroupWeapon(transcendenceSummaryWepon);
               // log.info(transcendenceSummaryWepon); // 로그 제거됨
               
               } else {
               // 무기가 아닐 경우 (방어구/악세 등) 초기값 '0' 또는 빈 값으로 설정
               // DTO 초기화는 엘릭서 로직에서 이미 처리되지만, 명시적으로 설정
               parsing.setIndentStringGroupWeapon("0"); 
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
               List<String> elixirOptions = new ArrayList<>(); 
               log.info("--- 엘릭서 정보 파싱 시작 ---");

               // Null 안전성을 고려한 무기 체크
               if (!"무기".equals(itemDetail.getType())) { 


               // JSON 전체를 순회하면서 옵션 텍스트가 담긴 최종 contentStr 노드를 찾습니다.
               Iterator<Map.Entry<String, JsonNode>> ElixirOptionfields = rootNode.fields();

               while (ElixirOptionfields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = ElixirOptionfields.next();
                    JsonNode elementNode = entry.getValue(); 
                    
                    // 1. Element_XXX의 value 노드 확인
                    JsonNode valueNode = elementNode.path("value");

                    if (valueNode.isObject()) {
                         
                         Iterator<String> subFieldNames = valueNode.fieldNames();
                         while (subFieldNames.hasNext()) {
                              String subElementKey = subFieldNames.next();
                              JsonNode subElementNode = valueNode.path(subElementKey);

                              // 2. contentStr 객체에 접근 (엘릭서 옵션은 이 객체 내부에 있음)
                              JsonNode contentStrGroup = subElementNode.path("contentStr"); 
                              
                              if (contentStrGroup.isObject()) {
                                   
                                   // contentStrGroup 하위의 옵션 노드들 (Element_000, Element_001...) 순회
                                   Iterator<String> optionKeys = contentStrGroup.fieldNames();
                                   
                                   while (optionKeys.hasNext()) {
                                   String optionKey = optionKeys.next();
                                   // 최종 옵션 텍스트 노드: contentStrGroup/Element_XXX/contentStr
                                   JsonNode optionTextNode = contentStrGroup.path(optionKey).path("contentStr"); 

                                   if (optionTextNode.isTextual()) {
                                        String rawText = optionTextNode.asText();
                                        
                                        // 3. 엘릭서 옵션 고유 키워드 체크 (Lv.N과 [공용]/[부위] 키워드를 가진 경우만)
                                        if (rawText.contains("Lv.") && (rawText.contains("[") && rawText.contains("]"))) {
                                             
                                             log.info("DEBUG - 엘릭서 옵션 후보 텍스트 발견: {}", rawText);
                                             
                                             // 4. HTML 태그 제거 및 전처리
                                             String cleanText = rawText.replaceAll("<[^>]*>", "").replaceAll("\r\n", " ").trim();
                                             
                                             // 5. 첫 줄만 사용 (옵션만)
                                             String[] lines = cleanText.split("\\n|<br>|<BR>");
                                             String mainOption = lines[0].trim();
                                             
                                             // 6. [부위] 키워드 제거 (예: [공용] 공격력 Lv.5 -> 공격력 Lv.5)
                                             String finalOption = mainOption.replaceAll("\\[[가-힣]*\\]\\s*", "");

                                             // 7. 'Lv.' 뒤에 붙은 상세 스탯 잔여물 제거
                                             if (finalOption.contains("Lv.")) {
                                                  Pattern pattern = Pattern.compile("(.+? Lv\\.\\d+)");
                                                  Matcher matcher = pattern.matcher(finalOption);
                                                  if (matcher.find()) {
                                                       finalOption = matcher.group(1).trim(); 
                                                  }
                                             }
                                             
                                             // 8. 옵션 리스트에 추가 (순서대로 옵션 1, 2)
                                             if (!finalOption.isEmpty()) {
                                                  elixirOptions.add(finalOption);
                                                  log.info("DEBUG - 최종 추출된 엘릭서 옵션: {}", finalOption);
                                             }
                                        }
                                   }
                                   }
                              }
                         }
                    } 
               }
               } else {
               // ⭐️ 무기이거나 itemDetail이 null인 경우 ⭐️
               log.info("--- 엘릭서 정보 파싱 스킵 --- 아이템 타입이 '무기'이거나 타입 정보가 없습니다. 스킵합니다.");
               }

               // ------------------------------------------------------------------

               // 9. 최종적으로 DTO에 저장 (if/else 블록이 끝난 후, 모든 경우에 대해 실행)
               // ------------------------------------------------------------------
               if (elixirOptions.size() >= 1) {
               parsing.setElixirOption1(elixirOptions.get(0));
               } else {
               // 옵션이 없거나 무기인 경우 Option1도 빈 문자열로 초기화
               parsing.setElixirOption1("");
               }

               if (elixirOptions.size() >= 2) {
               parsing.setElixirOption2(elixirOptions.get(1));
               } else {
               // 옵션이 1개 이하인 경우 Option2는 빈 문자열로 초기화
               parsing.setElixirOption2(""); 
               }

               log.info("--- 엘릭서 추출 완료 --- 옵션 1: {}, 옵션 2: {}", 
               parsing.getElixirOption1(), // DTO에서 최종 결과 확인
               parsing.getElixirOption2());


// ------------------------------------------------------------------


               
               //어빌리티 스톤
               if (itemDetail.getType().equals("어빌리티 스톤")) {
                    JsonNode e007Stone = rootNode
                                             .path("Element_007")
                                             .path("value")
                                             .path("Element_000")
                                             .path("contentStr");
                    
                    if(e007Stone.isObject()){
                         StringBuilder combinedOption = new StringBuilder(); //옵션 찾을 때 옵션을 쌓아 둘 변수
                         Iterator<JsonNode> optionFields = e007Stone.elements(); // 하위 필드들을 순서대로 처리하기위해 리스트 처럼 보관

                         boolean isFirst = true; // 줄바꿈 적용할 변수
                         //어빌리티 스톤 옵션을 순회하면서 찾음
                         while(optionFields.hasNext()){
                              JsonNode optionNode = optionFields.next();

                              String rawText = optionNode.path("contentStr").asText();

                              if(!rawText.isEmpty() && !rawText.isBlank()){
                                   //rawText에 값이 있다면 html테그 제거
                                   String cleanTextOption = rawText
                                                                 .replaceAll("<[^>]*>", "") 
                                                                 .replaceAll("\\s{2,}", " ").trim();
                                   cleanTextOption = cleanTextOption.replaceAll("Lv\\.(\\d+)", "Lv.$1");
                                   
                                   if(cleanTextOption.contains("감소")){
                                        cleanTextOption = "<span class = 'text-red-600'>" + cleanTextOption + "</span>";
                                   }
                                   
                                   //두 번째 옵션에 줄바꿈 적용(보고 별로면 지우기)
                                   if(!isFirst){
                                        combinedOption.append("<br>");
                                   }
                                   combinedOption.append(cleanTextOption);
                                   isFirst = false;
                              }
                         }
                         parsing.setAbilityStone(combinedOption.toString());
                         log.info(combinedOption.toString());
                    }
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
