package org.embed.maincontroller;

import java.util.List;
import org.embed.DBService.CharacterData;
import org.embed.service.CharacterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MainController {
    
    private final CharacterService characterService;

    public MainController(CharacterService characterService){
        this.characterService = characterService;
    }

    @GetMapping("/mainHome")
    public String home() {
        return "mainHome";
    }
    
     @GetMapping("/character_Info") 
    public String character_Info(@RequestParam("characterName") String characterName, Model model) {

        List<CharacterData> siblingsList = characterService.CData(characterName);

        if (siblingsList != null && !siblingsList.isEmpty()) { 
        model.addAttribute("siblingsList", siblingsList);
        model.addAttribute("mainCharacter", siblingsList.get(0)); 
        model.addAttribute("searchStatus", "success");

        }else{
             model.addAttribute("searchStatus", "failure");
            model.addAttribute("errorMessage", "해당 캐릭터의 정보를 찾을 수 없거나 서버 응답이 유효하지 않습니다.");
        }
    
    return "character_Info";
}
}



