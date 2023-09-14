package com.mio.admin.config.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import ch.qos.logback.classic.Logger;

/**
 * @FileName: GlobalExceptionHandler.java
 * @Project : admin
 * @Date    : 2023. 9. 9.
 * @작성자	: hamkh
 * @변경이력 	: 
 * @설명     : 글로벌 예외에 대한 처리
 */
@ControllerAdvice
@Controller
public class GlobalExceptionHandler {
	
	protected final Logger logger = (Logger) LoggerFactory.getLogger(getClass());

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public void noHandlerFoundException(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
		e.printStackTrace();
		response.sendRedirect("/error/404");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public void handleException(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
		e.printStackTrace();
		response.sendRedirect("/error/500");
	}
	
}