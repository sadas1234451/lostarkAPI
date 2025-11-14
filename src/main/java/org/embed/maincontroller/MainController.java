package org.embed.maincontroller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.embed.DBService.CharacterData;
import org.embed.DBService.CharacterEngravings;
import org.embed.DBService.Notices;
import org.embed.TooltipProcessing.GemTooltipParsing;
import org.embed.TooltipProcessing.ProfileTooltipParsing;
import org.embed.TooltipProcessing.TooltipParsing;
import org.embed.service.CharacterService;
import org.embed.service.NewsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {
    
    private final CharacterService characterService;
    private final NewsService newsService;

    public MainController(CharacterService characterService, NewsService newsService){
        this.characterService = characterService;
        this.newsService = newsService;
    }

    @GetMapping("/mainHome")
    public String home(Model model) {
        List<Notices> noticesList = newsService.noticesData();
        if(noticesList != null && !noticesList.isEmpty()){
            List<Notices> sortedNoticesList = noticesList.stream()
            .sorted(Comparator.comparing(Notices::getDate).reversed())
            .limit(5)
            .collect(Collectors.toList());
            
            model.addAttribute("noticesList", sortedNoticesList);
        }
        List<Notices> updatesList = newsService.updatesData();
        if(updatesList != null && !updatesList.isEmpty()){
            List<Notices> sortedUpdatesList = updatesList.stream()
                .sorted(Comparator.comparing(Notices::getDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
                
            // "updatesList"라는 이름으로 모델에 추가
            model.addAttribute("updatesList", sortedUpdatesList); 
        }

        return "mainHome";
    }
    //보유캐릭터 목록
     @GetMapping("/character_Info") 
    public String character_Info(@RequestParam("characterName") String characterName, Model model) {

        List<CharacterData> siblingsList = characterService.CData(characterName);
        if (siblingsList != null && !siblingsList.isEmpty()) { 

            List<CharacterData> sortedSiblingsList = siblingsList.stream()
            .sorted((c1, c2) -> {
                try {
                    // String으로 저장된 ItemLevel을 Double로 변환
                    Double level1 = Double.parseDouble(c1.getItemLevel().replace(",", ""));
                    Double level2 = Double.parseDouble(c2.getItemLevel().replace(",", ""));
                    // 내림차순 정렬 (높은 레벨이 먼저)
                    return level2.compareTo(level1);
                } catch (NumberFormatException e) {
                    // ItemLevel 파싱 오류 발생 시 순서 변경하지 않음
                    return 0; 
                }
            })
            .collect(Collectors.toList());
            //캐릭터 아이템 레벨 표시 개별 처리
            String mainCharacterItemLevel = siblingsList.stream()
            .filter(c -> c.getCharacterName().equals(characterName)) 
            .findFirst() 
            .map(CharacterData::getItemLevel) 
            .orElse("정보 없음");
        model.addAttribute("mainCharacterItemLevel", mainCharacterItemLevel);
        model.addAttribute("siblingsList", sortedSiblingsList);
        model.addAttribute("mainCharacter", characterName);
        model.addAttribute("searchStatus", "success");
        log.info("siblingsList: {}", sortedSiblingsList);

        }else{
             model.addAttribute("searchStatus", "failure");
            model.addAttribute("errorMessage", "해당 캐릭터의 정보를 찾을 수 없거나 서버 응답이 유효하지 않습니다.");
        }
        //캐릭터 착용 장비목록
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
        //캐릭터 상세스텟
        ProfileTooltipParsing profile = characterService.profiles(characterName);
        if(profile != null ){
            model.addAttribute("fatal", profile.getFatal());
            model.addAttribute("specialization", profile.getSpecialization());
            model.addAttribute("speed", profile.getSpeed());
            model.addAttribute("combatPower", profile.getCombatPower());
            model.addAttribute("className", profile.getClassName());

            log.info("캐릭터 [{}]의 치명 영구 증가량: {}", characterName, profile.getFatal());
            log.info("캐릭터 [{}]의 특화 영구 증가량: {}", characterName, profile.getSpecialization());
            log.info("캐릭터 [{}]의 신속 영구 증가량: {}", characterName, profile.getSpeed());
            log.info("캐릭터 [{}]의 전투력: {}", characterName, profile.getCombatPower());
            log.info("캐릭터 [{}]의 직업: {}", characterName, profile.getClassName());
        }
        //캐릭터 장착 보석처리
        List<GemTooltipParsing> CharacterGem = characterService.CharacterGemsList(characterName);
        if(CharacterGem != null && !CharacterGem.isEmpty()){
            model.addAttribute("characterGems", CharacterGem);
            
            log.info("캐릭터 [{}]의 파싱된 보석 상세 정보 {}개를 모델에 추가했습니다.", characterName, CharacterGem.size());

        }else {
            // 보석 정보가 없거나 파싱에 실패한 경우
            model.addAttribute("characterGems", Collections.emptyList()); // 빈 리스트 전달
            log.warn("캐릭터 [{}]의 보석 정보를 가져오지 못했거나 파싱에 실패했습니다.", characterName);
        }
        
        //캐릭터 장착 각인
        CharacterEngravings engravingsData = characterService.CharacterEngravingsData(characterName);

        if (engravingsData != null && engravingsData.getEngravings() != null) {
            // 각인 정보(List<EngravingDetail>)를 모델에 추가
            model.addAttribute("engravingsList", engravingsData.getEngravings());
            log.info("캐릭터 [{}]의 각인 상세 정보 {}개를 모델에 추가했습니다.", characterName, engravingsData.getEngravings().size());
        } else {
            // 각인 정보가 없거나 API 호출 실패 시 빈 리스트 전달
            model.addAttribute("engravingsList", Collections.emptyList()); 
            log.warn("캐릭터 [{}]의 각인 정보를 가져오지 못했습니다.", characterName);
        }
    return "character_Info";
    }
    @GetMapping("/multiSearch")
    public String multiSearch(Model model) {
        
        return "multiSearch";
    }
}



