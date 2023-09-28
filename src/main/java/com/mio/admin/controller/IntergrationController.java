package com.mio.admin.controller;

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

import com.mio.admin.service.IntergrationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/intergration")
public class IntergrationController {

	private final IntergrationService intergrationService;

	@RequestMapping("/index")
	public String index(@RequestParam Map<String, Object> param, Model model) {

		model.addAttribute("params", param);

		return "intergration/index";
	}

	@RequestMapping(value="/startCrawling", method = { RequestMethod.GET, RequestMethod.POST})
	public void startCrawling(@RequestBody Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		intergrationService.startCrawling(param, request, response);
	}

	@RequestMapping(value= "/xlsDownload", method = { RequestMethod.GET, RequestMethod.POST})
	public void xlsDownload(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		intergrationService.xlsDownload(param, request, response, model);
	}
}
