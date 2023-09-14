package com.mio.admin.common;

import java.math.BigDecimal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;

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
}
