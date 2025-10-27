package org.embed.character.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class apikey implements key {

	    private String apiKey;

	    public apikey(String apiKey) {
	        this.apiKey = apiKey;
	    }

	    @Override
	    public String getCharacterInfo(String characterName) {
	        String url = "https://developer-lostark.game.onstove.com/characters/" + characterName + "/siblings";

	        try {
	            HttpURLConnection conn = (HttpURLConnection) new URL("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IktYMk40TkRDSTJ5NTA5NWpjTWk5TllqY2lyZyIsImtpZCI6IktYMk40TkRDSTJ5NTA5NWpjTWk5TllqY2lyZyJ9.eyJpc3MiOiJodHRwczovL2x1ZHkuZ2FtZS5vbnN0b3ZlLmNvbSIsImF1ZCI6Imh0dHBzOi8vbHVkeS5nYW1lLm9uc3RvdmUuY29tL3Jlc291cmNlcyIsImNsaWVudF9pZCI6IjEwMDAwMDAwMDA1ODc1NjAifQ.coar5rz8ux5uzz8zF1CZE7oPXpwqRrFG3_6_f-KgY0uEj5MSw7Ui5S2UlFJ5GwW5k5UmAoWVgIVrBLNCA3_DkVJskgXF9nkKkR9t9uSUy-htkMJn_ZnzQJONLGFsp43dQizbzV32mjTJHOO4z-dc4NQDe7RQq8ARne8NeU_nfu6N7w_WVKFDWSZV_Zyy3wK5smt2qwiuW-rBLYoQXC49zE8vbSgE9H52EqkpGQXDDFPjBe-54MfSly2w_uy81XQOphxax06UDDnL6vQd79Ynksx8uxFCmURHkF58u-qyuwwmn_YBGSRL4E5E0lF9VKYC-gryK2n5wPbpjNL_q8pYGA").openConnection();
	            conn.setRequestMethod("GET");
	            conn.setRequestProperty("Authorization", "bearer " + apiKey);
	            conn.setRequestProperty("accept", "application/json");

	            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
	}
