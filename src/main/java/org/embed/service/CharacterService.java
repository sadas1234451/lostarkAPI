package org.embed.service;//API에서 불러올 정보 처리

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.embed.DBService.CharacterData;
import org.embed.DBService.CharacterDetail;
import org.embed.DBService.CharacterProfile;
import org.embed.TooltipProcessing.CharacterDetailTooltip;
import org.embed.TooltipProcessing.CharacterProfileTooltip;
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
    // 클래스 초기화
    public CharacterService(WebClient.Builder webClientBuilder, CharacterDetailTooltip tooltipProcessor, CharacterProfileTooltip profileProcessor){
        this.webC = webClientBuilder.baseUrl("https://developer-lostark.game.onstove.com").build();
        this.OBJMapper = new ObjectMapper();
        this.tooltipProcessor = tooltipProcessor;
        this.profilesProcessor = profileProcessor;
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
            return parsedItem;
        }catch (Exception e){
            System.out.println("APi 프로필 호출 실패" + e.getMessage());
           return new ProfileTooltipParsing();

        }

   }
}