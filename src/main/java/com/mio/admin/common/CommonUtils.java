package com.mio.admin.common;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mio.admin.config.jwtFilter.JwtUserDetails;
import com.mio.admin.dto.MemberDto;

@SuppressWarnings("rawtypes")
public class CommonUtils {

	public static MemberDto getLoginMember() {
		MemberDto member = ((JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMember();
		return member;
	}
	
	public static String getParameter(Map param, String paramName, String defaultValue) {
		String value = "";

		try {
			if (param != null) {

				if (param.containsKey(paramName)) {
					if (param.get(paramName) != null) {

						Object obj = param.get(paramName);

						if (obj.getClass().getName().equals("java.lang.Integer")) {
							value = Integer.toString((Integer) obj);
						} else if (obj.getClass().getName().equals("java.lang.Short")) {
							value = Short.toString((Short) obj);
						} else if (obj.getClass().getName().equals("java.lang.Long")) {
							value = Long.toString((Long) obj);
						} else if (obj.getClass().getName().equals("java.math.BigDecimal")) {
							BigDecimal bd = (BigDecimal) obj;
							value = bd.toString();
						} else {
							value = ((String) obj).trim();
							if (value.equals("")) {
								value = defaultValue;
							}
						}
					} else {
						value = defaultValue;
					}

				} else {
					value = defaultValue;
				}

			} else {
				value = defaultValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
			value = defaultValue;
		}

		return value;
	}

	static public String getRemoteIP(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");

		if (ip == null || ip.length() == 0 || ip.toLowerCase().equals("unknown"))
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");

		if (ip == null || ip.length() == 0 || ip.toLowerCase().equals("unknown"))
			ip = request.getHeader("REMOTE_ADDR");

		if (ip == null || ip.length() == 0 || ip.toLowerCase().equals("unknown"))
			ip = request.getRemoteAddr();

		return ip;
	}
	
	static public void removeDom(HtmlPage page) {
		DomNode header_wrap = page.querySelector("#header_wrap");
		DomNode footer = page.querySelector("#footer");
		DomNode mobile_header = page.querySelector(".header._header");
		DomNode mobile_footer = page.querySelector(".api_footer");
		List<DomNode> script = page.querySelectorAll("script");
		List<DomNode> api_ico_alert = page.querySelectorAll(".spnew.api_ico_alert");
		List<DomNode> ly_dic_alert = page.querySelectorAll(".ly_dic_alert");
		List<DomNode> ly_api_info = page.querySelectorAll(".ly_api_info._popup");
		
		script.forEach(item -> item.remove());
		api_ico_alert.forEach(item -> item.remove());
		ly_dic_alert.forEach(item -> item.remove());
		ly_api_info.forEach(item -> item.remove());
		
		if(header_wrap != null) header_wrap.remove();
		if(footer != null) footer.remove();
		if(mobile_header != null) mobile_header.getParentNode().remove();
		if(mobile_footer != null) mobile_footer.getParentNode().remove();
	}
	
	static public String getPath(String urlString) throws Exception {
		URL url = new URL(urlString);
		String path = url.getPath();
		return path;
	}
	
	static public String[] uniqueArray(String[] array) throws Exception {
		Set<String> uniqueSet = new HashSet<>();
		for (String item : array) {
			uniqueSet.add(item.trim()); // 중복 제거를 위해 trim() 사용
		}

		// 중복이 제거된 문자열 배열 다시 생성
		String[] uniqueArray = uniqueSet.toArray(new String[0]);
		
		return uniqueArray;
	}
	
}
