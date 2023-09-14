package com.mio.admin.config.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @FileName: ExceptionController.java
 * @Project : admin
 * @Date    : 2023. 9. 9.
 * @작성자	: hamkh
 * @변경이력 	:
 * @설명     : 예외발생에 대해 처리할 컨트롤러
 */
@Controller
public class ExceptionController {
	
	@RequestMapping("/error/403")
	public String error403(HttpServletRequest request, HttpServletResponse response, ModelMap model) {		
		return "error/403";
	}
	
	@RequestMapping("/error/404")
	public String error404(HttpServletRequest request, HttpServletResponse response, ModelMap model) {		
		return "error/404";
	}
	
	@RequestMapping("/error/500")
	public String error500(HttpServletRequest request, HttpServletResponse response, ModelMap model) {		
		return "error/500";
	}
	
}