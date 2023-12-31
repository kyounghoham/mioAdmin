package com.mio.admin.service;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
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
public class IntergrationService {

	// private int number = 0;
	@Autowired
	private SearchAdService searchAdService;
	
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
        
		try {
			// 키워드 리스트 loop
			Map<String, Integer> blogMap = new HashMap<>();
			for(int j=0; j<blogList.size(); j++) {
				int blogNumber = j+1;
				blogMap.put("blogNumber"+blogNumber, 0);
			}
			
			for(int i=0; i<totalCnt; i++) {
				// 50건 마다 60초 휴식
				if(i > 0 && i % 100 == 0) {
					Thread.sleep(60000);					
				}
				Map<String, Object> resultMap = new HashMap<>();
				List<Map<String, Object>> resultList = new ArrayList<>();
				String keyword = keywordList[i];
				
				System.out.println("No : " + (i+1) + " / 키워드 :" + keyword);
				
				BrowserVersion browser = (i % 2 == 0) ? BrowserVersion.FIREFOX_60 : BrowserVersion.CHROME;
				// 크롤링
				pcCrawling(keyword, blogList, resultList, blogMap, browser);
				
				// 5초 딜레이
				Thread.sleep(5000);
				
				// 서버로 진행상황 전달
				float percent = (i+1) * 100 / (totalCnt);
		        resultMap.put("percent", (int) percent);
		        resultMap.put("resultList", resultList);
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

	private void setBlog(Map<String, Object> param, List<String[]> blogList) throws Exception {
		String[] blogList1 = CommonUtils.getParameter(param, "blogList1", "").split("\n");
		String[] blogList2 = CommonUtils.getParameter(param, "blogList2", "").split("\n");
		String[] blogList3 = CommonUtils.getParameter(param, "blogList3", "").split("\n");
		String[] blogList4 = CommonUtils.getParameter(param, "blogList4", "").split("\n");
		String[] blogList5 = CommonUtils.getParameter(param, "blogList5", "").split("\n");
		
		// loop 돌리기 위해 배열로 저장
		setStringArr(blogList, blogList1);
		setStringArr(blogList, blogList2);
		setStringArr(blogList, blogList3);
		setStringArr(blogList, blogList4);
		setStringArr(blogList, blogList5);
	}
	
	private void setStringArr(List<String[]> blogList, String[] strArr) throws Exception {
		if(!strArr[0].equals("")) {
			blogList.add(strArr);
		} else {
			String[] str = {};
			blogList.add(str);
		}
	}
	
	public void pcCrawling(String keyword, List<String[]> blogList, List<Map<String, Object>> resultList, Map<String, Integer> blogMap, BrowserVersion browser) {
		HtmlUnit htmlUnit = new HtmlUnit();
		WebClient webClient = new WebClient(browser);

		try {
			String url = "https://m.search.naver.com/search.naver?where=m_view&sm=mtb_jum&query=" + URLEncoder.encode(keyword, "UTF-8");
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
			
			int size = filteredLiList.size() > 10 ? 10 : filteredLiList.size();
			
			for (int i = 0; i < size; i++) {
				DomNode item = filteredLiList.get(i);
				HtmlAnchor anchor = item.querySelector(".title_area a");
				DomNode adNode = item.querySelector(".spview.ico_ad");
				String title = anchor.asText();
				String href = anchor.getAttribute("href");
				String adFlag = "N";
				String name = "";
				
				// 광고여부 체크
				if(adNode != null) {
					adFlag = "Y";
					name = item.querySelector(".user_info a").asText();
					
					Thread.sleep(3000);
					page = htmlUnit.getHtmlPageNonCss(webClient, href);
					href = page.getBaseURL().toString();
				} else {
					name = item.querySelector(".user_info a").asText();
				}
				
				for(int j=0; j<blogList.size(); j++) {
					String[] blogArr = blogList.get(j);
					int blogNumber = j+1;
					
					for (String blog : blogArr) {
						if(!blog.equals("")) {
							boolean isValidUrl = isValidUrl(blog);
							
							if(!isValidUrl) {
								// 1. 의료광고번호
								String blog_medical_num = blog.replaceAll(" ", "");
								DomNode medical_num = item.querySelector(".user_info .sub:nth-child(3)");
								
								if(medical_num != null) {
									String medicalNum = medical_num.asText().replaceAll(" ", "");;
									if(medicalNum.contains(blog_medical_num)) {
										// 블로그 별로 number는 0부터 시작
										int number = blogMap.get("blogNumber"+blogNumber);
										number++;
										blogMap.put("blogNumber"+blogNumber, number);
										
										Map<String, Object> map = new HashMap<>();
										map.put("blogNumber", blogNumber);
										map.put("number", number);
										map.put("keyword", keyword);
										map.put("rank", i + 1);
										map.put("name", name);
										map.put("title", title);
										map.put("href", href.replace("m.blog", "blog").replace("blog", "m.blog"));
										map.put("blog", blog.replace("m.blog", "blog").replace("blog", "m.blog"));
										map.put("adFlag", adFlag);
										
										Thread.sleep(200);
										Map<String, String> searchMap = searchAdService.requestKeyword(keyword);
										map.put("pcCnt", searchMap.get("pcCnt"));
										map.put("moCnt", searchMap.get("moCnt"));
										
										resultList.add(map);
										
										// insertMap
									}
								}
								
							} else {
								// 2. blog주소 
								String blog_path = CommonUtils.getPath(blog).split("/")[1];
								String href_path = CommonUtils.getPath(href).split("/")[1];
								if (href_path.contains(blog_path)) {
									// 블로그 별로 number는 0부터 시작
									int number = blogMap.get("blogNumber"+blogNumber);
									number++;
									blogMap.put("blogNumber"+blogNumber, number);
									
									Map<String, Object> map = new HashMap<>();
									map.put("blogNumber", blogNumber);
									map.put("number", number);
									map.put("keyword", keyword);
									map.put("rank", i + 1);
									map.put("name", name);
									map.put("title", title);
									map.put("href", href.replace("m.blog", "blog").replace("blog", "m.blog"));
									map.put("blog", blog.replace("m.blog", "blog").replace("blog", "m.blog"));
									map.put("adFlag", adFlag);
									
									Thread.sleep(200);
									Map<String, String> searchMap = searchAdService.requestKeyword(keyword);
									map.put("pcCnt", searchMap.get("pcCnt"));
									map.put("moCnt", searchMap.get("moCnt"));
									
									resultList.add(map);
									
									// insertMap
								}
							}
								
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			webClient.close();
		}
	}
	
	private boolean isValidUrl(String url) {
		try {
			new URL(url);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	public void xlsDownload(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		List<Map<String, Object>> dataList = new ArrayList<>();
		String fileName = "";
		String[] columnList = {"keyword|키워드", "pcCnt|PC검색수", "moCnt|MO검색수", "rank1|1위", "rank2|2위", "rank3|3위", "rank4|4위", "rank5|5위", "rank6|6위", "rank7|7위", "rank8|8위", "rank9|9위", "rank10|10위"}; 
		String[] keywordList = CommonUtils.getParameter(param, "keywordList", "").split("\n");
		String[] blogList = {"로앤 최적화", "로앤 준최적화", "애플", "기타", "외"};
		String bodyList = CommonUtils.getParameter(param, "bodyList", "");

		try {
			Date currentDate = new Date();
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        String formattedDate = dateFormat.format(currentDate);
	        
			fileName = "intergration_" + formattedDate;
			
			JsonElement jsonElement = JsonParser.parseString(bodyList);
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			
			// 전체 검색결과 리스트
			for(int ii=0; ii<keywordList.length; ii++) {
				String keyword = keywordList[ii];
				keyword = keyword.trim();
				Map<String, Object> map = new HashMap<>();
				map.put("keyword", keyword);
				for (int i = 0; i < jsonArray.size(); i++) {
					JsonObject item = jsonArray.get(i).getAsJsonObject();
					if(keyword.equals(item.get("keyword").getAsString())) {
						String rank = item.get("rank").getAsString();
						String number = item.get("blogNumber").getAsString();
						String adFlag = item.get("adFlag").getAsString();
						String pcCnt = item.get("pcCnt").getAsString();
						String moCnt = item.get("moCnt").getAsString();
						String blogNm = blogList[Integer.parseInt(number) - 1];
						
						if(adFlag.equals("Y")) {
							blogNm += "(AD)";
						}
						map.put("pcCnt", pcCnt);
						map.put("moCnt", moCnt);
						map.put("rank"+rank, blogNm);
					}
				}
				
				String pcCnt = CommonUtils.getParameter(map, "pcCnt", "");
				if(pcCnt.equals("")) {
					Thread.sleep(250);
					Map<String, String> searchMap = searchAdService.requestKeyword(keyword);
					map.put("pcCnt", searchMap.get("pcCnt"));
					map.put("moCnt", searchMap.get("moCnt"));
				}
				
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
		HSSFSheet sheet = workbook.createSheet(fileName);
		HSSFCellStyle bodyStyle = workbook.createCellStyle();
		HSSFCellStyle redStyle = workbook.createCellStyle();
		HSSFCellStyle blueStyle = workbook.createCellStyle();
		HSSFCellStyle yellowStyle = workbook.createCellStyle();
		HSSFCellStyle limeStyle = workbook.createCellStyle();
		HSSFCellStyle violetStyle = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		
		HSSFRow row;
		HSSFCell cell;
		
		String[] enColumnList = new String[columnList.length];
		short fm = 0;
		short color = IndexedColors.WHITE.index;
		int rowNum = 0;		// 행숫자

		// 셀스타일 
		setCellStyle(bodyStyle, fm, font, color);
		setCellStyle(redStyle, fm, font, IndexedColors.RED.index);
		setCellStyle(blueStyle, fm, font, IndexedColors.LIGHT_BLUE.index);
		setCellStyle(yellowStyle, fm, font, IndexedColors.YELLOW.index);
		setCellStyle(limeStyle, fm, font, IndexedColors.LIME.index);
		setCellStyle(violetStyle, fm, font, IndexedColors.VIOLET.index);
		
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

					if(value.contains("로앤 최적화")) {
						cell = setCellValueAndStyle(row, cellIdx, value, redStyle);
					} else if(value.contains("로앤 준최적화")) {
						cell = setCellValueAndStyle(row, cellIdx, value, blueStyle);
					} else if(value.contains("애플")) {
						cell = setCellValueAndStyle(row, cellIdx, value, yellowStyle);
					} else if(value.contains("기타")) {
						cell = setCellValueAndStyle(row, cellIdx, value, limeStyle);
					} else if(value.contains("외")) {
						cell = setCellValueAndStyle(row, cellIdx, value, violetStyle);
					} else {
						cell = setCellValueAndStyle(row, cellIdx, value, bodyStyle);
					}
					
//					sheet.autoSizeColumn(cellIdx);
//					sheet.setColumnWidth(cellIdx, (sheet.getColumnWidth(cellIdx)) + 2048);

					cellIdx++;
				}
				rowNum++;
			}
		}
		
		sheet.setColumnWidth(0, (sheet.getColumnWidth(0)) + 1024*3);

		workbook.write(response.getOutputStream());
		workbook.close();
	}
	
	private void setCellStyle(HSSFCellStyle cellStyle, short fm, HSSFFont font, short color) {
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		
		// 가운데 정렬
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		
		if(fm != 0) {
			cellStyle.setDataFormat(fm);
		}

		// 색상있을시 적용
		if(color != IndexedColors.WHITE.index) {
			// cell color 적용
			cellStyle.setFillForegroundColor(color);
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// font bold 처리
			font.setBold(true);
			cellStyle.setFont(font);
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
			value = value.replace(" 최적화", "").replace(" 준최적화", "");
			cell.setCellValue(new HSSFRichTextString(value));
		}

		return cell;
	}
	
	private boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}
}
