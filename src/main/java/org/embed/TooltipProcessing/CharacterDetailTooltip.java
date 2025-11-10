package org.embed.TooltipProcessing;

import java.util.Iterator;
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
               if (itemDetail.getType().equals("팔찌")) {
                    JsonNode e005braceletOption = rootNode
                         .path("Element_005")
                         .path("value");

                    if (e005braceletOption.isObject()) {

                         String rawText = e005braceletOption.path("Element_001").asText();
                         
                         // rawText가 비어있을 경우, 필드에 빈 값을 설정하고 다음 로직을 건너뜁니다.
                         if (rawText.isEmpty() || rawText.isBlank()) {
                              parsing.setBraceletOption("");
                              parsing.setBraceletPartOption("");
                              // 'return parsing;'이 메서드 끝에 있다면, 여기서 return 없이 다음 줄로 넘어가
                              // parsing.setBraceletOption/PartOption이 재차 호출되도록 둡니다.
                         }

                         String cleanText = rawText
                              .replaceAll("<[^>]*>", "") // 모든 HTML/FONT 태그 제거
                              .replaceAll("\\s{2,}", " ") // 2개 이상의 연속된 공백을 1개로 축소
                              .trim();

                         // 파싱된 3개 옵션 줄을 담을 변수 선언
                         String optionLine1 = ""; // 신속/치명 (기존 baseStatsLine)
                         String optionLine2 = ""; // 첫 번째 부여 옵션 (복합 문장)
                         String optionLine3 = ""; // 나머지 부여 옵션들 (줄 바꿈 적용됨)
                         
                         // remainingText에 cleanText 전체를 넣어 파싱 시작
                         String remainingText = cleanText;

                         // 1. Line 1: 신속/치명 등 기본 옵션 추출
                         Pattern line1Pattern = Pattern.compile("^(.+?\\+\\d+)\\s*(.+?\\+\\d+)");
                         Matcher line1Matcher = line1Pattern.matcher(remainingText);

                         if (line1Matcher.find()) {
                              // 신속/치명을 Line 1으로 추출
                              optionLine1 = line1Matcher.group(1).trim() + "  " + line1Matcher.group(2).trim();
                              
                              // Line 1을 제외한 나머지 텍스트 갱신
                              remainingText = remainingText.substring(line1Matcher.end()).trim();
                         }

                        // 첫 번째 부여옵션 추출 순서
                         Pattern line2Pattern = Pattern.compile("^(.+?[\\\\다\\\\.])\\\\s*"); 
                         Matcher line2Matcher = line2Pattern.matcher(remainingText);

                         if (line2Matcher.find()) {
                              // Line 2 추출
                              optionLine2 = line2Matcher.group(1).trim(); 
                              
                              // Line 2를 제외한 나머지 텍스트 갱신
                              remainingText = remainingText.substring(line2Matcher.end()).trim();
                         }

                         
                        //2,3 번 나머지 옵션 전체 + 줄 바꿈 포맷팅//
                         optionLine3 = remainingText; 
                         
                         optionLine3 = optionLine3.replaceAll("([다\\.])(\\s*)(?=[가-힣])", "$1$2<br>");

                         // B. ⭐️ 수정된 정규식: %/숫자 뒤에 공백이 오고, 그 뒤에 '증'으로 이어지지 않을 때만 <br> 추가  
                    //    (예: '8.4% 공격이...'는 분리, '3% 증가하며...'는 유지)
                         optionLine3 = optionLine3.replaceAll("(?<=[%\\d])(\\s+)(?![가-힣]*증)(?=[가-힣])", "$1<br>");

                         
                         // baseStatsLine 역할: Line 1 (신속/치명) 설정
                         parsing.setBraceletOption(optionLine1); 
                         
                         // formattedOptions 역할: Line 2와 Line 3을 <br>로 합쳐서 설정
                         // Line 2가 비어있으면 Line 3만 사용합니다.
                         String combinedOptions = "";
                         if (!optionLine2.isEmpty()) {
                              combinedOptions = optionLine2 + "<br>" + optionLine3;
                         } else {
                              // 신속/치명이 있었으나 Line 2가 없거나, 신속/치명/Line 2 모두 없었을 때
                              combinedOptions = optionLine3;
                         }
                         
                         // 만약 Line 1도 없었고, Line 2도 없었으며, Line 3도 없다면 (파싱 데이터 자체가 없을 때),
                         // combinedOptions는 빈 문자열처리 
                         
                         parsing.setBraceletPartOption(combinedOptions.trim()); 
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
               
               String slot1Option = ""; // 엘릭서 옵션 1
               JsonNode slot1Node = e010Content.path("Element_000").path("contentStr");
               
               if (slot1Node.isTextual()) {
                    String rawText = slot1Node.asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", ""); 
                    String[] lines = cleanText.split("\\n|<br>|<BR>");
                    String mainOption = lines[0].trim();
                    String finalOption = mainOption.replaceAll("\\[[가-힣]*\\]\\s*", "");
                    
                    if (finalOption.contains("Lv.")) {
                         Pattern pattern = Pattern.compile("(.+? Lv\\.\\d+)");
                         Matcher matcher = pattern.matcher(finalOption);
                         if (matcher.find()) {
                              finalOption = matcher.group(1).trim();
                         }
                    }
                    slot1Option = finalOption;
               }
               parsing.setElixirOption1(slot1Option); 
               
               
               String slot2Option = ""; // 엘릭서 옵션 2
               JsonNode slot2Node = e010Content.path("Element_001").path("contentStr"); 
               
               if (slot2Node.isTextual()) {
                    String rawText = slot2Node.asText();
                    String cleanText = rawText.replaceAll("<[^>]*>", ""); 
                    String[] lines = cleanText.split("\\n|<br>|<BR>");
                    String mainOption = lines[0].trim();
                    String finalOption = mainOption.replaceAll("\\[[가-힣]*\\]\\s*", "");
                    log.info("잔여물 제거 전 : " + finalOption);
                  
        
                    // 'Lv.' 뒤에 붙은 상세 스탯 잔여물 제거
                    if (finalOption.contains("Lv.")) {
                         Pattern pattern = Pattern.compile("(.+? Lv\\.\\d+)");
                         Matcher matcher = pattern.matcher(finalOption);
                         if (matcher.find()) {
                              finalOption = matcher.group(0).trim();
                               log.info("잔여물 제거  :" + finalOption);
                         }
                    }
                    slot2Option = finalOption;
               }
               parsing.setElixirOption2(slot2Option);
               
               log.info("엘릭서 옵션 1: {}, 엘릭서 옵션 2: {}", slot1Option, slot2Option);
               }
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
