package com.mio.admin.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mio.admin.common.CommonUtils;
import com.mio.admin.common.CookieUtils;
import com.mio.admin.config.jwtFilter.JwtTokenUtil;
import com.mio.admin.dto.MemberDto;
import com.mio.admin.service.IntergrationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/intergration")
public class IntergrationController {

	private final IntergrationService intergrationService;
	
	private final JwtTokenUtil jwtTokenUtil;
	
	private final CookieUtils cookieUtils;

	@RequestMapping("/index")
	public String index(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		refreshLoginSession(request, response);
		model.addAttribute("params", param);

		return "intergration/index";
	}

	@RequestMapping(value="/startCrawling", method = { RequestMethod.GET, RequestMethod.POST})
	public void startCrawling(@RequestBody Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		refreshLoginSession(request, response);
		intergrationService.startCrawling(param, request, response);
	}

	@RequestMapping(value= "/xlsDownload", method = { RequestMethod.GET, RequestMethod.POST})
	public void xlsDownload(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		refreshLoginSession(request, response);
		intergrationService.xlsDownload(param, request, response, model);
	}
	
	private void refreshLoginSession(HttpServletRequest request, HttpServletResponse response) throws Exception {
		MemberDto userInfo = CommonUtils.getLoginMember();
		if (userInfo != null) {
			// jwt 토큰생성
			Map<String, Object> tokenData = new HashMap<>();
			String id = userInfo.getEmail();
			tokenData.put("idx", userInfo.getIdx());
			tokenData.put("id", userInfo.getId());
			tokenData.put("name", userInfo.getName());
			tokenData.put("email", userInfo.getEmail());
			
			String accessToken = jwtTokenUtil.createAccessToken(id, tokenData);
			
			// 로그인 성공하면 쿠키에 JWT토큰 저장
			cookieUtils.setCookie(response, "X-AUTH-TOKEN", accessToken);
		}
	}
}
