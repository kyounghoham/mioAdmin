package com.mio.admin.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mio.admin.service.RankingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ranking")
public class RankingController {

	private final RankingService rankingService;

	@RequestMapping("/index")
	public String index(@RequestParam Map<String, Object> param, Model model) {

		model.addAttribute("params", param);

		return "ranking/index";
	}

	@RequestMapping("/startCrawling")
	public void startCrawling(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		rankingService.startCrawling(param, request, response);
	}

	@RequestMapping(value= "/xlsDownload", method = { RequestMethod.GET, RequestMethod.POST})
	public void xlsDownload(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		this.rankingService.xlsDownload(param, request, response, model);
//		return new ModelAndView("excelDownload");
	}
}




