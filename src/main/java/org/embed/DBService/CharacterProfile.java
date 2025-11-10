package org.embed.DBService;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
//전투력/ 치특신 
//처리 순위 1순위 처음 받아오는 페이지
@Data
public class CharacterProfile {
    //data 
    @JsonProperty("Stats")
    private String Stats;
}
