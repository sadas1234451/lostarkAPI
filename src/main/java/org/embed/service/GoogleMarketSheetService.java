package org.embed.service; // <--- 사용자님의 새 패키지 경로

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.embed.DBService.GoogleMarketSheetDTO;
import org.springframework.beans.factory.annotation.Value; // <-- 수정: Spring의 @Value 어노테이션 임포트
import org.springframework.stereotype.Service; // <-- 추가: Spring Service로 등록하는 어노테이션

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Service // <-- 추가: 이 클래스를 Spring Bean으로 등록하여 @Value 사용 가능
public class GoogleMarketSheetService { 
    
  //스프레드시트 ID로 변경
    private static final String SPREADSHEET_ID = "1qg-lf_LPLkK1uSIIIKdwP0De-pY5EWFc_z6_UR0yBsI";
    
    // 마켓 데이터가 기록된 시트 이름과 데이터 범위 (A2부터 C열 끝까지)
    // 1행은 헤더이므로 A2부터 시작합니다.
    private static final String DATA_RANGE = "마켓 데이터!A2:C";
    
    // 오타 수정: JacksonFactor -> JacksonFactory
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    // application.properties에서 서비스 계정 파일 경로를 주입 받습니다.
    @Value("${sheets.api.credentials.path}") // <-- 이제 @Service 덕분에 정상 작동
    private String credentialsPath;
    
    /**
     * Google Sheets 서비스 객체를 생성하고 서비스 계정으로 인증합니다.
     * @return Sheets 객체
     * @throws IOException, GeneralSecurityException
     */
    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        // 서비스 계정 키 파일을 클래스 경로에서 읽어옵니다. (e.g., /service-account-key.json)
        // Spring Boot 환경이므로 ClassLoader를 사용하여 리소스를 안전하게 로드합니다.
        InputStream inputStream = getClass().getResourceAsStream(credentialsPath.replace("classpath:", "/"));
        if (inputStream == null) {
            throw new IOException("Service account key file not found at: " + credentialsPath);
        }

        // Sheets API에 대한 권한 범위를 설정합니다. (읽기 전용)
        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets.readonly"));

        // Sheets 객체 빌드 및 반환
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("LostArk-Market-Tracker")
                .build();
    }

    /**
     * Google Sheets에서 로스트아크 마켓 데이터를 가져옵니다.
     * @return GoogleMarketSheetDTO 리스트
     */
    public List<GoogleMarketSheetDTO> getMarketData() { 
        try {
            Sheets sheetsService = getSheetsService();
            
            // 지정된 스프레드시트 ID와 범위의 값을 가져옵니다.
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, DATA_RANGE)
                    .execute();
            
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                System.out.println("SheetService: No data found in the spreadsheet.");
                return Collections.emptyList();
            }

            // 가져온 List<List<Object>> 데이터를 GoogleMarketSheetDTO 리스트로 변환합니다.
            return values.stream()
                    .map(this::mapToDto)
                    .filter(dto -> dto != null) // 변환 중 오류가 발생한 행은 제외
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("SheetService: Error fetching market data from Google Sheets: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Sheets API 응답의 한 행(List<Object>)을 GoogleMarketSheetDTO 객체로 매핑합니다.
     * @param row API에서 가져온 데이터 행 (A열: 이름, B열: 최저가, C열: 시간)
     * @return GoogleMarketSheetDTO 객체
     */
    private GoogleMarketSheetDTO mapToDto(List<Object> row) { 
        // 최소 3개의 열 데이터가 있어야 합니다.
        if (row == null || row.size() < 3) {
            System.err.println("Invalid row data (less than 3 columns): " + row);
            return null;
        }
        try {
            String itemName = row.get(0).toString();
            
            // B열: CurrentMinPrice는 String으로 올 수 있으므로 Long으로 변환
            // 혹시 데이터에 천 단위 쉼표가 있을 경우를 대비해 제거합니다.
            String priceString = row.get(1).toString();
            Long currentMinPrice = Long.parseLong(priceString.replaceAll(",", "")); 
            
            String searchTime = row.get(2).toString();
            
            return new GoogleMarketSheetDTO(itemName, currentMinPrice, searchTime); 
        } catch (NumberFormatException e) {
            System.err.println("Error parsing price to Long in row: " + row + ". Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error during row mapping: " + row + ". Error: " + e.getMessage());
            return null;
        }
    }
}