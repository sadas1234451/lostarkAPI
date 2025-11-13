package org.embed.DBService;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Notices {
    @JsonProperty("Title")
    private String Title; 

    @JsonProperty("Date")
    private LocalDateTime Date; 
    
    @JsonProperty("Link")
    private String Link; 

    @JsonProperty("Type")
    private String Type; 

}
