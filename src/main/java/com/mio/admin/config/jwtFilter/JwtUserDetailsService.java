package com.mio.admin.config.jwtFilter;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mio.admin.dto.MemberDto;
import com.mio.admin.mapper.LoginMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {
	private final LoginMapper loginMapper;
	private final JwtTokenMapper jwtTokenMapper;
	private final Map<String, JwtUserDetails> userCache = new ConcurrentHashMap<>();

	@Override
	public JwtUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		JwtUserDetails cachedUser = userCache.get(username);
		if (cachedUser != null) {
			return cachedUser;
		}

		MemberDto member = loginMapper.selectUserInfo(username);
		if (member != null) {
			JwtUserDetails userDetails = new JwtUserDetails(member);
			userCache.put(username, userDetails);
			return new JwtUserDetails(member);
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}

	public int saveRefreshToken(String user_id, String refreshToken, Timestamp expireTime) {
		int result = 0;

		try {
			// user_id로 기존 발급된 리프레쉬 토큰이 있다면 모두 삭제
//			jwtTokenMapper.deleteRefreshTokenByUserid(user_id);

			// 신규 리프레쉬 토큰 발급
//			result = jwtTokenMapper.saveRefreshToken(user_id, refreshToken, expireTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int getRefreshTokenCnt(String refreshToken) {
		return jwtTokenMapper.getRefreshTokenCnt(refreshToken);
	}

	public int deleteRefreshTokenByToken(String refreshToken) {
		return jwtTokenMapper.deleteRefreshTokenByToken(refreshToken);
	}

	public void removeUserCache(String username) {
		userCache.remove(username);
	}
}