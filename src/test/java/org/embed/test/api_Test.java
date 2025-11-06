package org.embed.test;


	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import java.net.HttpURLConnection;
	import java.net.URL;
	import java.util.Scanner;

	public class api_Test {
	    public static void main(String[] args) {
	        Scanner scanner = new Scanner(System.in);

	        System.out.print("API 키를 입력하세요: ");
	        String apiKey = scanner.nextLine();

	        System.out.print("조회할 캐릭터 이름을 입력하세요: ");
	        String characterName = scanner.nextLine();

	        String apiBaseUrl = "https://developer-lostark.game.onstove.com/characters/";
	        String fullUrl = apiBaseUrl +"/armories/characters/" + characterName + "/profiles";
	        try {
	            URL url = new URL(apiBaseUrl);
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("GET");
	            conn.setRequestProperty("Authorization", "bearer " + apiKey);
	            conn.setRequestProperty("accept", "application/json");

	            int responseCode = conn.getResponseCode();
	            System.out.println("응답 코드: " + responseCode);

	            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            String inputLine;
	            StringBuilder response = new StringBuilder();

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();

	            System.out.println("API 응답 내용:");
	            System.out.println(response.toString());

	        } catch (Exception e) {
	            System.out.println("API 호출 실패: " + e.getMessage());
	        }

	        scanner.close();
	    }
	}

