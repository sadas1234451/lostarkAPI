package org.embed.DBService;//캐릭터 상세정보 (장비)DTO

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class CharacterDetail {
@JsonProperty("Type")
private String type;
@JsonProperty("Name")
private String name;
@JsonProperty("Icon")
private String icon;
@JsonProperty("Grade")
private String grade;
@JsonProperty("Tooltip")
private String tooltip;
}

