package com.mio.admin.config.jwtFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mio.admin.common.CookieUtils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

@Component
public class JwtTokenUtil implements Serializable {

	private final CookieUtils cookieUtils = new CookieUtils();
	
    @Value("spring.jwt.secret")
    private String secretKey;

    private long accessTokenValidMilisecond = 1000L * 60 * 60 * 48; // 2일간 토큰 유효
    private long refreshTokenValidMilisecond = 1000L * 60 * 60 * 24 * 15; // 15일만 토큰 유효

    private HashMap<String, Boolean> refreshTokenList = new HashMap<>();

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // Jwt 토큰 생성
    public String createAccessToken(String user_id, Map<String, Object> tokenData) {
        Claims claims = Jwts.claims().setSubject(user_id);
        claims.put("tokenData", tokenData);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // 데이터
                .setIssuedAt(now) // 토큰 발행일자
                .setExpiration(new Date(now.getTime() + accessTokenValidMilisecond)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
                .compact();
    }

    // Jwt 토큰 생성
    public String createRefreshToken(String user_id, Map<String, Object> tokenData) {
        Claims claims = Jwts.claims().setSubject(user_id);
        claims.put("tokenData", tokenData);

        Date now = new Date();
        String refreshToken = Jwts.builder()
                .setClaims(claims) // 데이터
                .setIssuedAt(now) // 토큰 발행일자
                .setExpiration(new Date(now.getTime() + refreshTokenValidMilisecond)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
                .compact();

        jwtUserDetailsService.saveRefreshToken(
                user_id,
                refreshToken,
                new Timestamp(now.getTime() + refreshTokenValidMilisecond)
        );

        return refreshToken;
    }

    // Jwt 토큰으로 인증 정보를 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Jwt 토큰에서 회원 구별 정보 추출
    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    // 쿠키에서 가져옴 JWT토큰
    // Request의 Header에서 token 파싱 : "X-AUTH-TOKEN: jwt토큰"
    public String resolveToken(HttpServletRequest request) {
    	String token = cookieUtils.getCookie(request, "X-AUTH-TOKEN");
        if (token == null) {
            // 쿠키에서 토큰을 찾지 못한 경우 헤더에서 직접 가져옵니다.
            token = request.getHeader("X-AUTH-TOKEN");
        }
        return token;
    }

    // Jwt 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
        return !claims.getBody().getExpiration().before(new Date());
    }
    
    // Jwt 토큰의 권한 조회
    public String getAuthCd(String jwtToken) {
    	Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
    	return (String) claims.getBody().get("authCd");
    }

    public Map<String, Object> getTokenData(String jwtToken) {
        ObjectMapper objectMapper = new ObjectMapper();
        Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
        return objectMapper.convertValue(claims.getBody().get("tokenData"), Map.class);
    }

    public void deleteRefreshToken(String refreshToken) {
        jwtUserDetailsService.deleteRefreshTokenByToken(refreshToken);
    }

    public void removeUserCache(String username) {
    	jwtUserDetailsService.removeUserCache(username);
    }
    
    public void updateUserCache(HttpServletRequest request) {
    	String token = this.resolveToken((HttpServletRequest) request);
        if (token != null && this.validateToken(token)) {
        	jwtUserDetailsService.removeUserCache(this.getUserPk(token));
            Authentication auth = this.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    public boolean loadRefreshToken(String refreshToken) {
        int result = 0;

        try {
            result = jwtUserDetailsService.getRefreshTokenCnt(refreshToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result > 0;
    }
    
    public boolean checkRefreshToken(String jwtToken) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
        Date now = new Date();

        return !now.before(
                new Date(claims.getBody().getExpiration().getTime() - accessTokenValidMilisecond)
        );
    }
}