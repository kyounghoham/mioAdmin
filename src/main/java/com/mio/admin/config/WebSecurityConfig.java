package com.mio.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mio.admin.config.jwtFilter.JwtExceptionFilter;
import com.mio.admin.config.jwtFilter.JwtRequestFilter;

@Configuration
public class WebSecurityConfig {

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	private JwtExceptionFilter jwtExceptionFilter;

	@Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/bootstrap/**", "/favicon.ico");
    }
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.httpBasic().disable()
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt token으로 인증하므로 세션은 필요없으므로 생성안함.
			.and().authorizeRequests() 						 // 다음 리퀘스트에 대한 사용권한 체크
			.antMatchers("/login/*", "/error/*").permitAll() // 누구나 접근가능
			.anyRequest().authenticated()					 // permitAll 제외하고 인증 요구 
			.and()
			.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)	// jwt token 필터를 id/password 인증 필터 전에 넣는다
			.addFilterBefore(jwtExceptionFilter, JwtRequestFilter.class)					// jwt Exception 을 처리 하기 위해 jewFilter 전에 추가 한다.
			.exceptionHandling().accessDeniedPage("/error/404");
		
        return http.build();
    }

}