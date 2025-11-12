package org.embed.DBService;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown= true)
public class CharacterEngravings {
    
    @JsonProperty("ArkPassiveEffects") 
    private List<EngravingDetail> engravings; 
    
    // 각인 목록의 개별 항목을 정의하는 DTO
    @Data
    @JsonIgnoreProperties(ignoreUnknown= true)
    public static class EngravingDetail {
        @JsonProperty("Name")
        private String name; 
        
        @JsonProperty("Level")
        private Integer level;
        @JsonProperty("Grade")
        private String grade;
        @JsonProperty("Description")
        private String description; 
    }
}
