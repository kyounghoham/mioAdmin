package com.mio.admin.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mio.admin.common.CommonUtils;
import com.mio.admin.common.CookieUtils;
import com.mio.admin.config.jwtFilter.JwtTokenUtil;
import com.mio.admin.dto.MemberDto;
import com.mio.admin.service.LoginService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {

	private final LoginService loginService;

	private final JwtTokenUtil jwtTokenUtil;
	
	private final CookieUtils cookieUtils;
	
	@RequestMapping(value = "/login", method = { RequestMethod.GET, RequestMethod.POST })
	public String login(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, Model model) {
		model.addAttribute("params", param);
		return "login/login";
	}

	@RequestMapping(value = "/loginSubmit", method = { RequestMethod.GET, RequestMethod.POST })
	public String loginSubmit(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model) throws Exception {
		String id = CommonUtils.getParameter(param, "id", "");
		String pwd = CommonUtils.getParameter(param, "pwd", "");
		String redirectUrl = "/login/login";
		String accessToken = "";
		String refreshToken = "";
		
		boolean isLogin = false;
		
		isLogin = loginService.login(id, pwd);

		if(isLogin) {
			MemberDto userInfo = loginService.selectUserInfo(id);
			if (userInfo != null) {
				// jwt 토큰생성
				Map<String, Object> tokenData = new HashMap<>();
				
				tokenData.put("idx", userInfo.getIdx());
				tokenData.put("id", userInfo.getId());
				tokenData.put("name", userInfo.getName());
				tokenData.put("email", userInfo.getEmail());
				
				accessToken = jwtTokenUtil.createAccessToken(id, tokenData);
				refreshToken = jwtTokenUtil.createRefreshToken(id, tokenData);
				
				// 로그인 성공하면 쿠키에 JWT토큰 저장
				cookieUtils.setCookie(response, "X-AUTH-TOKEN", accessToken);
				
				redirectUrl = "/main/index";
			} else {
				redirectAttributes.addFlashAttribute("message", "아이디 또는 비밀번호를 확인해주세요.");
			}
		} else {
			redirectAttributes.addFlashAttribute("message", "아이디 또는 비밀번호를 확인해주세요.");
		}

		return "redirect:" + redirectUrl;
	}

}
