package org.embed.maincontroller; // MainController와 동일한 패키지 사용

import java.util.Collections;
import java.util.List;

import org.embed.DBService.GoogleMarketSheetDTO;
import org.embed.service.GoogleMarketSheetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // <-- @Controller로 변경
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j // 로깅 어노테이션 추가
@Controller // View를 반환하도록 @Controller로 변경
@RequestMapping("/market") // 기본 경로를 /api/lostark 대신 /market 등으로 변경 (선택 사항)
public class GoogleSheetController {

    private final GoogleMarketSheetService googleMarketSheetService;

    // 생성자 주입
    public GoogleSheetController(GoogleMarketSheetService googleMarketSheetService) {
        this.googleMarketSheetService = googleMarketSheetService;
    }

    /**
     * Google Sheets에서 마켓 데이터를 가져와 Thymeleaf 템플릿으로 전달합니다.
     * Endpoint: GET /market/data-view
     * @return 템플릿 파일 이름 ("marketDataView" -> marketDataView.html)
     */
    @GetMapping("/market")
    public String getMarketDataView(Model model) {
        log.info("Google Sheets 마켓 데이터 뷰 요청 시작.");
        
        List<GoogleMarketSheetDTO> data = googleMarketSheetService.getMarketData();
        
        if (data != null && !data.isEmpty()) {
            // 데이터가 있을 경우 모델에 추가
            model.addAttribute("marketDataList", data);
            model.addAttribute("loadStatus", "success");
            log.info("Google Sheets에서 {}개의 마켓 데이터를 성공적으로 가져왔습니다.", data.size());
        } else {
            // 데이터가 없거나 로드에 실패한 경우
            model.addAttribute("marketDataList", Collections.emptyList());
            model.addAttribute("loadStatus", "failure");
            model.addAttribute("errorMessage", "Google Sheets 데이터를 가져오지 못했거나 스프레드시트가 비어있습니다.");
            log.warn("Google Sheets 데이터를 가져오지 못했습니다.");
        }
        
        // Thymeleaf가 'templates' 폴더 내의 'marketDataView.html' 파일을 찾도록 반환합니다.
        return "marketDataView"; 
    }
}