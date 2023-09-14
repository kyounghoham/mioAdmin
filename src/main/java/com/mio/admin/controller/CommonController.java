package com.mio.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mio.admin.common.CommonUtils;
import com.mio.admin.dto.MemberDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CommonController {

	@RequestMapping("/")
	public String index(Model model) {
		MemberDto member = CommonUtils.getLoginMember();
		String redirectUrl = (member == null) ? "/login/login" : "/main/index";
		return "redirect:" + redirectUrl;
	}

}
