//package com.mio.admin.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
//import org.springframework.boot.autoconfigure.security.servlet.StaticResourceRequest.StaticResourceRequestMatcher;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//
//import com.mio.admin.config.jwtFilter.JwtExceptionFilter;
//import com.mio.admin.config.jwtFilter.JwtRequestFilter;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class WebSecurityConfig22 extends WebSecurityConfigurerAdapter {
//
//	@Autowired
//	private JwtRequestFilter jwtRequestFilter;
//
//	@Autowired
//	private JwtExceptionFilter jwtExceptionFilter;
//	
//	@Override
//	public void configure(WebSecurity web) throws Exception {
//		web.ignoring().mvcMatchers("/bootstrap/**", "/favicon.ico");
//	}
//	
//	@Override
//	protected void configure(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity.httpBasic().disable()
//					.csrf().disable()
////					.formLogin().loginPage("/login/login")
//					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt token으로 인증하므로 세션은 필요없으므로 생성안함.
//					// dont authenticate this particular request
//					.and().authorizeRequests() // 다음 리퀘스트에 대한 사용권한 체크
//					.antMatchers("/login/*", "/error/*").permitAll() // 가입 및 인증 주소는 누구나 접근가능
//					// all other requests need to be authenticated
//					.anyRequest().authenticated()
//					// .anyRequest().permitAll()
//					.and()
//					// make sure we use stateless session; session won't be used to
//					// store user's state.
//					.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)	// jwt token 필터를 id/password 인증 필터 전에 넣는다
//					.addFilterBefore(jwtExceptionFilter, JwtRequestFilter.class)					// jwt Exception 을 처리 하기 위해 jewFilter 전에 추가 한다.
//					.exceptionHandling().accessDeniedPage("/error/404");
//
//	}
//	
//}