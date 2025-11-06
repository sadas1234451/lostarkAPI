package org.embed.service;

import java.util.Collections;
import java.util.List;

import org.embed.DBService.CharacterData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CharacterService {
    @Value("${lostark.api.key}")
    private String apikey;

    private final WebClient webC;
    private final ObjectMapper OBJMapper;

    public CharacterService(WebClient.Builder webClientBuilder){
        this.webC = webClientBuilder.baseUrl("https://developer-lostark.game.onstove.com").build();

        this.OBJMapper = new ObjectMapper();
    }

    public List<CharacterData> CData(String characterName){
        String apiURL = "/characters/" + characterName + "/siblings";
        String apiResponseJson;


        try{
            apiResponseJson = webC.get()
            .uri(apiURL)
            .header("Authorization", "bearer " + apikey)
            .retrieve()
            .bodyToMono(String.class)
            .block();
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
}
