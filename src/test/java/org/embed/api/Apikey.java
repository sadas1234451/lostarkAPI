package org.embed.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Key 인터페이스는 그대로 둡니다.
public class Apikey implements Key { 

    private String apiKey;

    public Apikey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getCharacterInfo(String characterName) {
		String encodedName = URLEncoder.encode(characterName, StandardCharsets.UTF_8);
        String url = "https://developer-lostark.game.onstove.com/characters/" + encodedName + "/siblings";

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "bearer " + apiKey);
            conn.setRequestProperty("accept", "application/json");

            // 응답 코드 확인 및 스트림 선택 (이전 논의에서 안전성을 위해 추가)
            BufferedReader in;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            return "API 호출 실패: " + e.getMessage();
        }
    }
    
    // 🟢 main 메서드 추가: 이 클래스를 실행하여 테스트할 수 있도록 합니다.
    public static void main(String[] args) {
        // 🚨 1. 여기에 당신의 실제 API 키를 넣어야 합니다. (주의: 보안에 유의)
        String testApiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IktYMk40TkRDSTJ5NTA5NWpjTWk5TllqY2lyZyIsImtpZCI6IktYMk40TkRDSTJ5NTA5NWpjTWk5TllqY2lyZyJ9.eyJpc3MiOiJodHRwczovL2x1ZHkuZ2FtZS5vbnN0b3ZlLmNvbSIsImF1ZCI6Imh0dHBzOi8vbHVkeS5nYW1lLm9uc3RvdmUuY29tL3Jlc291cmNlcyIsImNsaWVudF9pZCI6IjEwMDAwMDAwMDA1ODc1NjAifQ.coar5rz8ux5uzz8zF1CZE7oPXpwqRrFG3_6_f-KgY0uEj5MSw7Ui5S2UlFJ5GwW5k5UmAoWVgIVrBLNCA3_DkVJskgXF9nkKkR9t9uSUy-htkMJn_ZnzQJONLGFsp43dQizbzV32mjTJHOO4z-dc4NQDe7RQq8ARne8NeU_nfu6N7w_WVKFDWSZV_Zyy3wK5smt2qwiuW-rBLYoQXC49zE8vbSgE9H52EqkpGQXDDFPjBe-54MfSly2w_uy81XQOphxax06UDDnL6vQd79Ynksx8uxFCmURHkF58u-qyuwwmn_YBGSRL4E5E0lF9VKYC-gryK2n5wPbpjNL_q8pYGA"; 
        
        // 🚨 2. 실제로 존재하는 캐릭터 이름을 넣어야 합니다.
        String testCharacterName = "눈가루"; // 또는 당신의 부캐가 많은 캐릭터 이름

        Apikey apiTest = new Apikey(testApiKey);
        
        // 3. API 호출 및 결과 출력
        String jsonResponse = apiTest.getCharacterInfo(testCharacterName);
        
        System.out.println("=======================================================================");
        System.out.println("✅ Lost Ark API Response (JSON) for Character: " + testCharacterName);
        System.out.println("=======================================================================");
        System.out.println(jsonResponse); // ⬅️ 이 라인에서 실제 JSON이 출력됩니다.
        System.out.println("=======================================================================");
    }
}