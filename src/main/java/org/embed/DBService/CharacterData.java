package org.embed.DBService;//캐릭터 정보

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CharacterData {
     @JsonProperty("ServerName")
    private String serverName;
    @JsonProperty("CharacterName")
    private String characterName; 
    @JsonProperty("CharacterClassName")
    private String job;          // 또는 characterClassName
    @JsonProperty("CharacterLevel")
    private int level;           // 캐릭터 레벨
    @JsonProperty("ItemAvgLevel")
    private String itemLevel;    // 아이템 레벨 (API에서 String으로 받을 가능성 높음)
}
