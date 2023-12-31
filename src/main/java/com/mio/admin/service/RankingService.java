package com.mio.admin.service;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
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
public class RankingService {

	private int number = 0;
	
	public void startCrawling(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		number = 0;
		
		String[] keywordList = CommonUtils.getParameter(param, "keywordList", "").split("\n");
		String[] blogList = CommonUtils.getParameter(param, "blogList", "").split("\n");
		
		int totalCnt = keywordList.length;
		
		response.setHeader("Transfer-Encoding", "chunked");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        
		try {
			for(int i=0; i<totalCnt; i++) {
				number++;
				Map<String, Object> resultMap = new HashMap<>();
				String keyword = keywordList[i];
				String blog = blogList[i];
				
				// PC 조회
				resultMap = pcCrawling(keyword, blog);
				
				// 1초 딜레이
				Thread.sleep(3000);
				
				// 서버로 진행상황 전달
		        float percent = (i+1) * 100 / totalCnt;
		        resultMap.put("percent", (int) percent);
		        Gson gson = new Gson();
		        String json = gson.toJson(resultMap);
		        
		        out.println(json);
		        out.flush();
			}

		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}
	
	public Map<String, Object> pcCrawling(String keyword, String blog) {
		HtmlUnit htmlUnit = new HtmlUnit();
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_60);
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String url = "https://m.search.naver.com/search.naver?where=m_view&query=" + URLEncoder.encode(keyword, "UTF-8");
			HtmlPage page = htmlUnit.getHtmlPageNonCss(webClient, url);

			// 게시물 리스트
			DomNodeList<DomNode> li_list = page.querySelectorAll("ul.lst_view li.bx");
			List<DomNode> filteredLiList = new ArrayList<DomNode>();

			for (DomNode li : li_list) {
				DomElement de = (DomElement) li;
			    if (!de.getAttribute("class").toString().contains("type_join")) {
			        filteredLiList.add(li);
			    }
			}
			
			resultMap.put("number", number);
			resultMap.put("keyword", keyword);
			resultMap.put("href", blog);
			resultMap.put("blog", blog);
			resultMap.put("rank", "-");
			resultMap.put("rank2", "-");
			resultMap.put("name", "-");
			resultMap.put("title", "-");
			
			int size = filteredLiList.size() > 10 ? 10 : filteredLiList.size();
			
			for (int i = 0; i < size; i++) {
				// 30개 * 블로그 리스트 검사
				DomNode item = filteredLiList.get(i);
				HtmlAnchor anchor = item.querySelector(".title_area a");
				DomNode adNode = item.querySelector(".spview.ico_ad");
				String title = anchor.asText();
				String href = anchor.getAttribute("href");
				String adFlag = "N";
				String name = "";
				String rank = (i + 1 > 10) ? "-" : Integer.toString(i + 1);
				
				// 광고여부 체크
				if(adNode != null) {
					adFlag = "Y";
					name = item.querySelector(".user_info a").asText();
					
					Thread.sleep(1000);
					page = htmlUnit.getHtmlPageNonCss(webClient, href);
					href = page.getBaseURL().toString();
				} else {
					name = item.querySelector(".user_info a").asText();
				}

				String href_path = CommonUtils.getPath(href);
				String blog_path = CommonUtils.getPath(blog);
				if (href_path.contains(blog_path)) {
					resultMap.put("rank", rank);
					resultMap.put("rank2", rank);
					resultMap.put("name", name);
					resultMap.put("title", title);
				}
			}
			
			String name = CommonUtils.getParameter(resultMap, "name", "-");
			if(name.equals("-")) {
				// 일치하지 않으면 url가서 제목이라도 가져옴
				Thread.sleep(500);
				page = htmlUnit.getHtmlPageNonCss(webClient, blog);
				
				String blogNm = page.querySelector(".blog_author").getTextContent().trim();
				String blogTitle = page.querySelector(".se-title-text").getTextContent().trim();
				resultMap.put("name", blogNm);
				resultMap.put("title", blogTitle);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			webClient.close();
		}
		
		return resultMap;
	}
	
	public void xlsDownload(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		List<Map<String, Object>> dataList = new ArrayList<>();
		String[] columnList = {"keyword|키워드", "name|블로그명", "href|블로그주소", "title|제목", "rank|PC순위", "rank2|MO순위"};
		String fileName = "";

		try {
			Date currentDate = new Date();
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        String formattedDate = dateFormat.format(currentDate);
	        
			fileName = "ranking_" + formattedDate;
			
			String bodyList = CommonUtils.getParameter(param, "bodyList", "");
			JsonElement jsonElement = JsonParser.parseString(bodyList);
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			
	        for (int i = 0; i < jsonArray.size(); i++) {
	        	JsonObject item = jsonArray.get(i).getAsJsonObject();
	        	Map<String, Object> map = new HashMap<>();
	            map.put("keyword", item.get("keyword").getAsString());
	            map.put("name", item.get("name").getAsString());
	            map.put("href", item.get("href").getAsString());
	            map.put("title", item.get("title").getAsString());
	            map.put("rank", item.get("rank").getAsString());
	            map.put("rank2", item.get("rank2").getAsString());
	            dataList.add(map);
	        }

			// 엑셀 다운로드
			buildExcelDocument(fileName, columnList, dataList, response);

		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private void buildExcelDocument(String fileName, String[] columnList, List<Map<String, Object>> dataList, HttpServletResponse response) throws Exception {
		response.setContentType("application/msexcel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
		HSSFWorkbook workbook = new HSSFWorkbook();
		CreationHelper createHelper = workbook.getCreationHelper();
		HSSFSheet sheet = workbook.createSheet(fileName);
		HSSFCellStyle bodyStyle = workbook.createCellStyle();
		HSSFRow row;
		HSSFCell cell;
		
		String[] enColumnList = new String[columnList.length];
		short fm = 0;
		short color = 0;
		int rowNum = 0;		// 행숫자

		// 셀스타일 
		setCellStyle(bodyStyle, fm, color);
		
		rowNum ++;
		row = sheet.createRow(rowNum);
		String[] temp;

		// 컬럼명 세팅
		int cellIdx = 0;
		for(int i = 0 ; i < columnList.length ; i++) {
			temp = columnList[i].split("[|]");
			
			if(temp.length == 2) {
				enColumnList[i] = temp[0];
				cell = setCellValueAndStyle(row, cellIdx, temp[1], bodyStyle);
				cellIdx++;
			}
		}

		// 데이터 조회
		if(columnList != null && dataList != null) {
			Map<String, Object> dataMap = null;
			String value = "";
			rowNum++;

			// 데이터 리스트
			for(int i = 0; i < dataList.size(); i++) {
				dataMap = dataList.get(i);
				row = sheet.createRow(rowNum);
				cell = row.createCell(0);

				cellIdx = 0;
				for(int j=0; j<enColumnList.length; j++) {
					value = dataMap.containsKey(enColumnList[j]) ? String.valueOf(dataMap.get(enColumnList[j])) : "";
					
					cell = setCellValueAndStyle(row, cellIdx, value, bodyStyle);
					
					sheet.autoSizeColumn(cellIdx);
					sheet.setColumnWidth(cellIdx, (sheet.getColumnWidth(cellIdx)) + 2048);

					if(j == 2) {
						Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
						link.setAddress(dataMap.get("href").toString());
						cell.setHyperlink(link);
					}
					cellIdx++;
				}
				rowNum++;
			}
		}

		workbook.write(response.getOutputStream());
		workbook.close();
	}
	
	@SuppressWarnings("deprecation")
	private void setCellStyle(HSSFCellStyle cellStyle, short fm, short color) {
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);

		if(fm != 0) {
			cellStyle.setDataFormat(fm);
		}

		if(color != 0) {
			cellStyle.setFillForegroundColor(color);	// 노란색
		}
	}
	
	private HSSFCell setCellValueAndStyle(HSSFRow row, int index, String value, HSSFCellStyle cellStyle) {
		HSSFCell cell = row.createCell(index);
		cell.setCellStyle(cellStyle);

		if(isNumeric(value)) {
			try {
				if(value.length() > 10) {
					cell.setCellValue(value);						// 10자리이상 숫자는 문자로 출력
				} else {
					cell.setCellValue(Double.parseDouble(value));	// 숫자형태의 문자열데이터는 숫자로 변환하여 출력
				}
			}catch(Exception e) {
				cell.setCellValue(value);
			}
		} else {
			cell.setCellValue(new HSSFRichTextString(value));
		}

		return cell;
	}
	
	private boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}
}
