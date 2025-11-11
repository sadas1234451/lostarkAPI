package org.embed.DBService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown= true)
public class CharacterGem {
    //보석 data Gems슬롯을 service에서 처리 후 내부 필드 분리
    @JsonProperty("Slot")
    private String GemsSlot;

    @JsonProperty("Name")
    private String GemsName;

    @JsonProperty("Icon")
    private String GemsIcon;

    @JsonProperty("Level")
    private String GemsLevel;

     @JsonProperty("Grade")
    private String GemsGrade;

    @JsonProperty("Tooltip")
    private String GemsTooltip;
    
}
