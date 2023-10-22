package com.mio.admin.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mio.admin.common.Signatures;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchAdService {

	private String baseUrl = "https://api.searchad.naver.com";
    private String path = "/keywordstool";
    private String accessKey = "01000000001ad4797f5ba311cdfe0e742a092eb4d40a2b3b5cb8669d23a7dcd21f0f9cec04"; // 액세스키
    private String secretKey = "AQAAAAAa1Hl/W6MRzf4OdCoJLrTUw5e18ZjiHzpSBgV/wJKbNw==";  // 시크릿키
    private String customerId = "3027280";  // ID
    
    public Map<String, String> requestKeyword(String keyword) {
    	Map<String, String> resultMap = new HashMap<String, String>();
    	keyword = keyword.replaceAll(" ", "");
    	
        String parameter = "hintKeywords=";
        long timeStamp = System.currentTimeMillis();
        URL url = null;
        String times = String.valueOf(timeStamp);

        try {
            keyword = URLEncoder.encode(keyword, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("인코딩 실패.");
        }

        try {
            url = new URL(baseUrl+path+"?"+parameter+keyword);
            String signature = Signatures.of(times,  "GET", path, secretKey);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("X-Timestamp", times);
            con.setRequestProperty("X-API-KEY", accessKey);
            con.setRequestProperty("X-Customer", customerId);
            con.setRequestProperty("X-Signature", signature);
            con.setDoOutput(true);

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            }
            else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.toString());
            JsonNode keywordList = jsonNode.get("keywordList");

            resultMap.put("relKeyword", keywordList.get(0).get("relKeyword").toString());
            resultMap.put("pcCnt", keywordList.get(0).get("monthlyPcQcCnt").toString());
            resultMap.put("moCnt", keywordList.get(0).get("monthlyMobileQcCnt").toString());
            
        } catch (Exception e) {
            System.out.println("Wrong URL.");
        }

        return resultMap;
    }
}
