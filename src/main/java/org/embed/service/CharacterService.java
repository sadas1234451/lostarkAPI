package org.embed.service;//API에서 불러올 정보 처리

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.embed.DBService.CharacterData;
import org.embed.DBService.CharacterDetail;
import org.embed.DBService.CharacterGem;
import org.embed.DBService.CharacterProfile;
import org.embed.TooltipProcessing.CharacterDetailTooltip;
import org.embed.TooltipProcessing.CharacterGemTooltip;
import org.embed.TooltipProcessing.CharacterProfileTooltip;
import org.embed.TooltipProcessing.GemTooltipParsing;
import org.embed.TooltipProcessing.ProfileTooltipParsing;
import org.embed.TooltipProcessing.TooltipParsing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class CharacterService {
    @Value("${lostark.api.key}")
    private String apikey;
    //데이터 받을 클레스 먼저 정의
    private final WebClient webC;
    private final ObjectMapper OBJMapper;
    private final CharacterDetailTooltip tooltipProcessor;
    private final CharacterProfileTooltip profilesProcessor;
    private final CharacterGemTooltip gemProcessor;
    // 클래스 초기화
    public CharacterService(WebClient.Builder webClientBuilder, CharacterDetailTooltip tooltipProcessor, CharacterProfileTooltip profileProcessor, CharacterGemTooltip gemProcessor){
        this.webC = webClientBuilder.baseUrl("https://developer-lostark.game.onstove.com").build();
        this.OBJMapper = new ObjectMapper();
        this.tooltipProcessor = tooltipProcessor;
        this.profilesProcessor = profileProcessor;
        this.gemProcessor = gemProcessor;
    }
    //보유 캐릭터 목록
    public List<CharacterData> CData(String characterName){
        String apiURL = "/characters/" + characterName + "/siblings";
        String apiResponseJson;


        try{
            apiResponseJson = webC.get()
            .uri(apiURL)
            .header("Authorization", "Bearer " + apikey)
            .retrieve()
            .bodyToMono(String.class)
            .block();

            // 💡 디버깅 코드 추가: API 응답 출력
           log.info("API 응답 JSON: {}", apiResponseJson); 

            if(apiResponseJson == null || apiResponseJson.startsWith("null") || apiResponseJson.contains("message")){
                throw new RuntimeException("API 응답이 없습니다.");
            }
            List<CharacterData> characterList = OBJMapper.readValue( 
                apiResponseJson, 
                new TypeReference<List<CharacterData>>(){}
            );
            return characterList;
        }catch (Exception e){
            System.out.println("APi 호출 실패" + e.getMessage());
           return Collections.emptyList();

        }

    }
    //캐릭터 장착 중인 장비 목록
    public List<TooltipParsing> CharacterDetailData(String characterName){
        String apiURL =  "/armories/characters/" + characterName + "/equipment";
        String apiResponseJson;
        List<CharacterDetail> rawDetailList;


        try{
            apiResponseJson = webC.get()
            .uri(apiURL)
            .header("Authorization", "Bearer " + apikey)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                log.error("API Error: Status={}, URL={}", clientResponse.statusCode(), apiURL);
                return clientResponse.bodyToMono(String.class)
                        .map(body -> new RuntimeException("API 응답 오류: " + clientResponse.statusCode() + ", Body: " + body));
            })
            .bodyToMono(String.class)
            .block();

            log.info("CharacterDetailData API 응답 JSON: {}", apiResponseJson);
             if (apiResponseJson != null && apiResponseJson.trim().startsWith("<")) {
                log.error("CharacterDetailData API 응답이 HTML 오류 페이지입니다. API 키나 URL을 확인하세요. 응답 본문 첫 100자: {}", apiResponseJson.substring(0, Math.min(apiResponseJson.length(), 100)));
                return Collections.emptyList();
            }
            
            if(apiResponseJson == null || apiResponseJson.startsWith("null") || apiResponseJson.contains("message")){
                throw new RuntimeException("API 응답이 없습니다.");
            }

            rawDetailList = OBJMapper.readValue( 
                apiResponseJson, 
                new TypeReference<List<CharacterDetail>>(){}
            );
            List<TooltipParsing> parsedList = new ArrayList<>();
            for (CharacterDetail rawItem : rawDetailList) {
               try{
                TooltipParsing parsedItem = tooltipProcessor.extractSummary(rawItem);
                parsedList.add(parsedItem);
               }catch (Exception e){
              log.error("장비 [{}]의 Tooltip 파싱 실패", rawItem.getName(), e); 
               }
            }
            return parsedList;
        }catch (Exception e){
            log.error("데이터 처리 중 예외 발생: {}", e.getMessage(), e); // e 추가하여 스택 트레이스 출력
            System.out.println("APi 호출 실패" + e.getMessage());
           return Collections.emptyList();
        }
   }

   //캐릭터 상세 스텟 받아오는곳
   public ProfileTooltipParsing profiles(String characterName){
     String apiURL =  "/armories/characters/" + characterName + "/profiles";
        String apiResponseJson;
        CharacterProfile profiles;

         try{
            apiResponseJson = webC.get()
            .uri(apiURL)
            .header("Authorization", "Bearer " + apikey)
            .retrieve()
            .bodyToMono(String.class)
            .block();

            // 💡 디버깅 코드 추가: API 응답 출력
           log.info("API 응답 JSON: {}", apiResponseJson); 

            if(apiResponseJson == null || apiResponseJson.startsWith("null") || apiResponseJson.contains("message")){
                throw new RuntimeException("API 응답이 없습니다.");
            }
            profiles = OBJMapper.readValue( 
                apiResponseJson, 
                CharacterProfile.class
            );
            
            JsonNode root = OBJMapper.readTree(apiResponseJson);
            JsonNode statsNode = root.path("Stats");
            profiles.setCharacterStats(statsNode.toString());
            
            

            // 3. 파싱 위임
            ProfileTooltipParsing parsedItem = profilesProcessor.extractSummary(profiles);
            //전투력 정보 세팅
            parsedItem.setCombatPower(profiles.getCombatPower());
            return parsedItem;
        }catch (Exception e){
            System.out.println("APi 프로필 호출 실패" + e.getMessage());
           return new ProfileTooltipParsing();

        }

   }

   //캐릭터 보석 받아오는곳
   public List<GemTooltipParsing> CharacterGemsList(String characterName){
    String apiURL = "/armories/characters/" + characterName + "/gems";
    String apiResponseJson;
    List<CharacterGem> gemList;
       
    try{
            apiResponseJson = webC.get()
            .uri(apiURL)
            .header("Authorization", "Bearer " + apikey)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                log.error("API Error: Status={}, URL={}", clientResponse.statusCode(), apiURL);
                return clientResponse.bodyToMono(String.class)
                        .map(body -> new RuntimeException("API 응답 오류: " + clientResponse.statusCode() + ", Body: " + body));
            })
            .bodyToMono(String.class)
            .block();

            log.info("CharacterDetailData API 응답 JSON: {}", apiResponseJson);
             if (apiResponseJson != null && apiResponseJson.trim().startsWith("<")) {
                log.error("CharacterDetailData API 응답이 HTML 오류 페이지입니다. API 키나 URL을 확인하세요. 응답 본문 첫 100자: {}", apiResponseJson.substring(0, Math.min(apiResponseJson.length(), 100)));
                return Collections.emptyList();
            }
            
            if(apiResponseJson == null || apiResponseJson.startsWith("null") || apiResponseJson.contains("message")){
                throw new RuntimeException("API 응답이 없습니다.");
            }
            //받아온 api값을 문자열화 하기
            JsonNode rootNode = OBJMapper.readTree(apiResponseJson);
            //문자열화 한 값에서 gems필드 경로 진입(경로를 진입 할 필요가 없는 아이템이면 진입 안하게짜임 ex<장비텝> 장비텝은 진입할 필요 없이 툴팁 필드 자체를 받아와서 파싱 반복)
            //보석은 Gems필드 들어가서 파싱을 반복하기 때문에 Gems까지 진입해야함
            JsonNode gemsNode = rootNode.path("Gems");
            
            gemList = OBJMapper.readValue( 
                 gemsNode.toString(), 
                 new TypeReference<List<CharacterGem>>(){}
            );
            List<GemTooltipParsing> parsedgemList = new ArrayList<>();
            for (CharacterGem gemItem : gemList) {
               try{
                GemTooltipParsing parsedgemItem = gemProcessor.extractSummary(gemItem);
                parsedgemList.add(parsedgemItem);
               }catch (Exception e){
              log.error("[{}]의 Gems(보석) 파싱 실패", gemItem.getGemsName(), e); 
               }
            }
            return parsedgemList;
        }catch (Exception e){
            log.error("데이터 처리 중 예외 발생: {}", e.getMessage(), e); // e 추가하여 스택 트레이스 출력
            System.out.println("APi 호출 실패" + e.getMessage());
           return Collections.emptyList();
        }
    }
}