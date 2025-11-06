package org.embed.maincontroller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.embed.DBService.CharacterData;
import org.embed.TooltipProcessing.TooltipParsing;
import org.embed.service.CharacterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        model.addAttribute("mainCharacter", characterName);
        model.addAttribute("searchStatus", "success");

        }else{
             model.addAttribute("searchStatus", "failure");
            model.addAttribute("errorMessage", "해당 캐릭터의 정보를 찾을 수 없거나 서버 응답이 유효하지 않습니다.");
        }

        List<TooltipParsing> CharacterDetail = characterService.CharacterDetailData(characterName);

        if(CharacterDetail != null && !CharacterDetail.isEmpty()){
            List<String> armorTypes = Arrays.asList("무기", "상의", "하의", "투구", "어깨", "장갑");
            List<String> accessoryTypes = Arrays.asList("목걸이", "반지", "귀걸이", "팔찌", "어빌리티 스톤");
            List<TooltipParsing> weaponAndArmorItems = CharacterDetail.stream()
                .filter(item -> armorTypes.stream().anyMatch(type -> item.getType().contains(type)))
                .collect(Collectors.toList());
            
           List<TooltipParsing> accessoryAndSpecialItems = CharacterDetail.stream()
                .filter(item -> accessoryTypes.stream().anyMatch(type -> item.getType().contains(type))) 
                .collect(Collectors.toList());
                
            model.addAttribute("weaponAndArmorItems", weaponAndArmorItems != null ? weaponAndArmorItems : List.of());
            model.addAttribute("accessoryAndSpecialItems", accessoryAndSpecialItems != null ? accessoryAndSpecialItems : List.of());
            log.info("캐릭터 [{}]의 파싱된 장비 상세 정보 {}개를 모델에 추가했습니다.", characterName, CharacterDetail.size());
        } else {
            log.warn("캐릭터 [{}]의 장비 상세 정보를 가져오지 못했거나 파싱에 실패했습니다.", characterName);
        }
    
    return "character_Info";
    }
}



