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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.mio.admin.common.CommonUtils;
import com.mio.admin.common.HtmlUnit;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntergrationService {

	private int number = 0;
	public void startCrawling(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		List<Map<String, Object>> resultList = new ArrayList<>();
		List<String[]> blogList = new ArrayList<>();
		
		String[] keywordList = CommonUtils.getParameter(param, "keywordList", "").split("\n");
		keywordList = CommonUtils.uniqueArray(keywordList);
		
		setBlog(param, blogList);
		
		int totalCnt = keywordList.length;
		
		response.setHeader("Transfer-Encoding", "chunked");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        
		try {
			for(int j=0; j<blogList.size(); j++) {
				number = 0;
				for(int i=1; i<=totalCnt; i++) {
					int idx = i-1;
					String keyword = keywordList[idx];
					// PC 조회
					pcCrawling(keyword, blogList.get(j), resultList);
					
					// 1초 딜레이
					Thread.sleep(600);
					
					// 서버로 진행상황 전달
					int a = i + (j*totalCnt);
					float percent = (a) * 100 / (totalCnt * blogList.size());
					out.println((int) percent);
					out.flush();
				}
				Thread.sleep(100);
				
				// 서버에 list 객체 전달
				Gson gson = new Gson(); // 또는 다른 JSON 라이브러리 사용
				String json = gson.toJson(resultList); // 리스트를 JSON 문자열로 변환
				out.println("data"+ (j+1) + ": " + json + "\n\n");
				out.flush();
				
				// 초기화
				resultList = new ArrayList<>();
			}


		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			out.close();
		}

		// map으로 전달할거면 사용
		resultMap.put("resultList", resultList);
	}

	private void setBlog(Map<String, Object> param, List<String[]> blogList) throws Exception {
		String[] blogList1 = CommonUtils.getParameter(param, "blogList1", "").split("\n");
		String[] blogList2 = CommonUtils.getParameter(param, "blogList2", "").split("\n");
		String[] blogList3 = CommonUtils.getParameter(param, "blogList3", "").split("\n");
		String[] blogList4 = CommonUtils.getParameter(param, "blogList4", "").split("\n");
		String[] blogList5 = CommonUtils.getParameter(param, "blogList5", "").split("\n");
		
		// 중복제거
		blogList1 = CommonUtils.uniqueArray(blogList1);
		blogList2 = CommonUtils.uniqueArray(blogList2);
		blogList3 = CommonUtils.uniqueArray(blogList3);
		blogList4 = CommonUtils.uniqueArray(blogList4);
		blogList5 = CommonUtils.uniqueArray(blogList5);
		
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

			// 불필요 dom 제거 (헤더, 푸터, 스크립트)
//			CommonUtils.removeDom(page);

			// 컨텐츠
//			DomNode main = page.querySelector("._panel");

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

}

/*
 
수원산부인과 
대구 임신중절수술 
강남산부인과 
부천산부인과
 
https://blog.naver.com/ijw0111 
https://blog.naver.com/tarjanil
https://blog.naver.com/cabbianopiu 
https://blog.naver.com/kagurazuki
https://blog.naver.com/kangsn7312 
https://blog.naver.com/lttyrugv
https://blog.naver.com/vxztd8s9
https://blog.naver.com/dtta8i1
https://blog.naver.com/dkflstjdd


 */

