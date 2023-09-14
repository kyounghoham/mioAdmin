package com.mio.admin.config.jwtFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Jwt 필터 제외 url(하위 폴더, 파일도 함께 적용)
    private static final List<String> EXCLUDE_URL =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "/login/login",
                            "/bootstrap",
                            "/error",
                            "/favicon.ico"
                    ));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = jwtTokenUtil.resolveToken((HttpServletRequest) request);
        try {
        	if (token != null && jwtTokenUtil.validateToken(token)) {
        		Authentication auth = jwtTokenUtil.getAuthentication(token);
        		SecurityContextHolder.getContext().setAuthentication(auth);
        	} else {
        		// 로그인 정보가 없으면 login 화면으로 이동
        		response.sendRedirect("/login/login");
        		return; // 필터 체인을 중단
        	}
        } catch(Exception e) {
        	response.sendRedirect("/login/login");
    		return; // 필터 체인을 중단
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return EXCLUDE_URL.stream().anyMatch(exclude -> request.getServletPath().toLowerCase().startsWith(exclude.toLowerCase()));
    }
}