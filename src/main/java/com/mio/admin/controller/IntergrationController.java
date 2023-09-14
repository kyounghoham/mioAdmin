package com.mio.admin.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/intergration")
public class IntergrationController {

	@RequestMapping("/index")
	public String index(@RequestParam Map<String, Object> param, Model model) {

		model.addAttribute("params", param);

		return "intergration/index";
	}

}
