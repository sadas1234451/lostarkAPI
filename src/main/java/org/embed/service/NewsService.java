package org.embed.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.embed.DBService.Notices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        this.OBJMapper.registerModule(new JavaTimeModule()); 
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
            List<Notices> filteredNoticesList = noticesList.stream()
            // .filter()의 조건이 'false'인 요소만 통과(제외)
            .filter(notice -> !notice.getTitle().contains("업데이트")) 
            
            // 정렬 및 5개 제한 로직은 그대로 유지
            .sorted(Comparator.comparing(Notices::getDate).reversed())
            .limit(5)
            .collect(Collectors.toList());
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
            return filteredNoticesList;
        }catch (Exception e){
            System.out.println("APi 호출 실패" + e.getMessage());
           return Collections.emptyList();

        }

    }
    public List<Notices> updatesData(){
     String apiURL = "/news/notices/"; // API 주소는 동일
     String apiResponseJson;

     try{
        apiResponseJson = webC.get()
            .uri(apiURL)
            .header("Authorization", "Bearer " + apikey)
            .retrieve()
            .bodyToMono(String.class)
            .block();


        if(apiResponseJson == null || apiResponseJson.startsWith("null") || apiResponseJson.contains("message")){
                throw new RuntimeException("API 응답이 없습니다.");
        }

         // ... (JSON 매핑) ...
         List<Notices> allNotices = OBJMapper.readValue( 
             apiResponseJson, 
             new TypeReference<List<Notices>>(){}
         );
         
        //필터링 로직 추가: 제목에 "업데이트"가 포함된 요소만 선택
         List<Notices> updateNoticesList = allNotices.stream()
             .filter(notice -> notice.getTitle().contains("업데이트")).limit(5)
             .collect(Collectors.toList());
         
         // (선택 사항: 로그 추가)
         log.info("업데이트 공지사항 {}개 필터링 완료.", updateNoticesList.size());
         
         return updateNoticesList;
     }catch (Exception e){
         log.error("업데이트 API 호출 또는 DTO 매핑 실패:", e);
         return Collections.emptyList();
     }
 }
}
