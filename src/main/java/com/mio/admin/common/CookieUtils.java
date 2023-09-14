package com.mio.admin.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

	private static String COOKIE_ENCODING = "euc-kr";
	
	@Value("${spring.profiles.active}")
    private String activeProfile;

	public String getCookie(HttpServletRequest req, String key) {

		Cookie[] cookies = req.getCookies();
		String value = null;
		if (cookies != null) {
			try {
				int cookieLength = cookies.length;
				for (int i = 0; i < cookieLength; i++) {
					if (cookies[i].getName().equals(key)) {
						value = cookies[i].getValue();
						break;
					}
				}
				if (value != null) {
					try {
						value = URLDecoder.decode(value, COOKIE_ENCODING);
					} catch (UnsupportedEncodingException e) {

					}
				}
			} catch (Exception e) {
				return null;
			}
		}
		return value;
	}

	public void setCookie(HttpServletResponse res, String key, String value) throws Exception {
		value = URLEncoder.encode(value, COOKIE_ENCODING);
		Cookie cookie = null;

		try {
			String domain = "13.209.126.84";
			if (activeProfile.equals("local")) {
				domain = "localhost";
			}
			cookie = new Cookie(key, value);
			cookie.setPath("/");
			cookie.setDomain(domain);
			cookie.setMaxAge(-1);
			res.addCookie(cookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}