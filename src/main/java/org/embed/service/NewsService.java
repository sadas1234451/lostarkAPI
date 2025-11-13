package org.embed.service;

import java.util.Collections;
import java.util.List;

import org.embed.DBService.Notices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NewsService {
    //API키 받아오기
    @Value("${lostark.api.key}")    
    private String apikey;
    private final WebClient webC;
    private final ObjectMapper OBJMapper;
    
    public NewsService(WebClient.Builder webClientBuilder){
        this.webC = webClientBuilder.baseUrl("https://developer-lostark.game.onstove.com").build();
        this.OBJMapper = new ObjectMapper();
    }
    //공지사항 처리
     public List<Notices> noticesData(){
        String apiURL = "/news/notices/";
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
            List<Notices> noticesList = OBJMapper.readValue( 
                apiResponseJson, 
                new TypeReference<List<Notices>>(){}
            );
             if (!noticesList.isEmpty()) {
                 Notices firstNotice = noticesList.get(0);
                 log.info("--- DTO 매핑 결과 확인 (첫 번째 요소) ---");
                 log.info("Title: {}", firstNotice.getTitle());
                 log.info("Date Type: {}", firstNotice.getDate().getClass().getSimpleName()); // LocalDateTime인지 확인
                 log.info("Date Value: {}", firstNotice.getDate()); // 값이 제대로 변환되어 들어왔는지 확인
                 log.info("Link: {}", firstNotice.getLink());
                 log.info("Type: {}", firstNotice.getType());
                 log.info("------------------------------------------");
             }
            return noticesList;
        }catch (Exception e){
            System.out.println("APi 호출 실패" + e.getMessage());
           return Collections.emptyList();

        }

    }
}
