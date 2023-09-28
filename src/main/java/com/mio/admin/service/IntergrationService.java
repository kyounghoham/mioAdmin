package com.mio.admin.service;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mio.admin.common.CommonUtils;
import com.mio.admin.common.HtmlUnit;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntergrationService {

	private int number = 0;
	public void startCrawling(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<String[]> blogList = new ArrayList<>();
		String[] keywordList = CommonUtils.getParameter(param, "keywordList", "").split("\n");
		setBlog(param, blogList);
		
		int totalCnt = keywordList.length;
		
		response.setHeader("Transfer-Encoding", "chunked");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        
        // 
		try {
			// 블로그 리스트 loop
			for(int j=0; j<blogList.size(); j++) {
				// 블로그 마다 10초 쉬는 텀 제공
				Thread.sleep(1000);
				// 블로그 별 게시글번호
				number = 0;
				
				// 키워드 리스트 loop
				for(int i=0; i<totalCnt; i++) {
					Map<String, Object> resultMap = new HashMap<>();
					List<Map<String, Object>> resultList = new ArrayList<>();
					String keyword = keywordList[i];
					
					// 크롤링
					pcCrawling(keyword, blogList.get(j), resultList);
					
					// 1초 딜레이
					Thread.sleep(1000);
					
					// 서버로 진행상황 전달
					int a = (i+1) + (j*totalCnt);
					float percent = (a) * 100 / (totalCnt * blogList.size());
			        resultMap.put("percent", (int) percent);
			        resultMap.put("resultList", resultList);
			        resultMap.put("number", j+1);	// 블로그 번호
			        Gson gson = new Gson();
			        String json = gson.toJson(resultMap);
			        
			        out.println(json);
			        out.flush();
				}
			}


		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}

	private void setBlog(Map<String, Object> param, List<String[]> blogList) throws Exception {
		String[] blogList1 = CommonUtils.getParameter(param, "blogList1", "").split("\n");
		String[] blogList2 = CommonUtils.getParameter(param, "blogList2", "").split("\n");
		String[] blogList3 = CommonUtils.getParameter(param, "blogList3", "").split("\n");
		String[] blogList4 = CommonUtils.getParameter(param, "blogList4", "").split("\n");
		String[] blogList5 = CommonUtils.getParameter(param, "blogList5", "").split("\n");
		
		// loop 돌리기 위해 배열로 저장
		if(!blogList1[0].equals("")) blogList.add(blogList1);
		if(!blogList2[0].equals("")) blogList.add(blogList2);
		if(!blogList3[0].equals("")) blogList.add(blogList3);
		if(!blogList4[0].equals("")) blogList.add(blogList4);
		if(!blogList5[0].equals("")) blogList.add(blogList5);
	}
	
	// 크롤링
	public void pcCrawling(String keyword, String[] blogList, List<Map<String, Object>> resultList) {
		HtmlUnit htmlUnit = new HtmlUnit();
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_60);

		try {
			String url = "https://m.search.naver.com/search.naver?where=m_view&sm=mtb_jum&query=" + URLEncoder.encode(keyword, "UTF-8");
			HtmlPage page = htmlUnit.getHtmlPageNonCss(webClient, url);

			// 게시물 리스트
			DomNodeList<DomNode> li_list = page.querySelectorAll("ul.lst_total li._svp_item");
			for (int i = 0; i < li_list.size(); i++) {
				// 30개 * 블로그 리스트 검사
				DomNode item = li_list.get(i);
				HtmlAnchor anchor = item.querySelector(".total_tit");
				DomNode adNode = item.querySelector(".spview.ico_ad");
				String title = anchor.asText();
				String href = anchor.getAttribute("href");
				String adFlag = "N";
				String name = "";
				
				// 광고여부 체크
				if(adNode != null) {
					adFlag = "Y";
					name = item.querySelector(".source_txt.name").asText();
					
					Thread.sleep(500);
					page = htmlUnit.getHtmlPageNonCss(webClient, href);
					href = page.getBaseURL().toString();
				} else {
					name = item.querySelector(".sub_name").asText();
				}

				for (String blog : blogList) {
					String blog_path = CommonUtils.getPath(blog).split("/")[1];
					String href_path = CommonUtils.getPath(href).split("/")[1];
					if (href_path.contains(blog_path)) {
						Map<String, Object> map = new HashMap<>();
						number++;
						map.put("number", number);
						map.put("type", "pc");
						map.put("keyword", keyword);
						map.put("rank", i + 1);
						map.put("name", name);
						map.put("title", title);
						map.put("href", href.replace("m.blog", "blog").replace("blog", "m.blog"));
						map.put("blog", blog.replace("m.blog", "blog").replace("blog", "m.blog"));
						map.put("adFlag", adFlag);
						resultList.add(map);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			webClient.close();
		}
	}

	public void xlsDownload(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		List<Map<String, Object>> dataList = new ArrayList<>();
		String[] keywordList = CommonUtils.getParameter(param, "keywordList", "").split("\n");
		String bodyList = CommonUtils.getParameter(param, "bodyList", "");

		JsonElement jsonElement = JsonParser.parseString(bodyList);
		JsonArray jsonArray = jsonElement.getAsJsonArray();
		
		// 전체 검색결과 리스트
		for(String keyword : keywordList) {
			keyword = keyword.trim();
			Map<String, Object> map = new HashMap<>();
			map.put("keyword", keyword);
			for (int i = 0; i < jsonArray.size(); i++) {
				JsonObject item = jsonArray.get(i).getAsJsonObject();
	            if(keyword.equals(item.get("keyword").getAsString())) {
	            	String rank = item.get("rank").getAsString();
	            	map.put("rank"+rank, rank);
	            }
			}
			dataList.add(map);
		}
		
//        for (int i = 0; i < jsonArray.size(); i++) {
//        	JsonObject item = jsonArray.get(i).getAsJsonObject();
//            map.put("name", item.get("name").getAsString());
//            map.put("href", item.get("href").getAsString());
//            map.put("title", item.get("title").getAsString());
//            map.put("rank", item.get("rank").getAsString());
//            dataList.add(map);
//        }
        
        dataList.forEach(item -> {
        	System.out.println(item.toString());
        });
        
	}
}
