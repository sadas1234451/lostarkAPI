package org.embed.DBService;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CharacterProfile {
    //data 
    @JsonProperty("Stats")
    private String Stats;
}
