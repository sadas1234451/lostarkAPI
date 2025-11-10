package org.embed.DBService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
//전투력/ 치특신 
//처리 순위 1순위 처음 받아오는 페이지
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterProfile {
    //data 
    @JsonProperty("TotalSkillPoint")
    private String TotalSkillPoint;
    
    private String characterStats;
}
