package com.mio.admin.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.mio.admin.dto.MemberDto;
import com.mio.admin.mapper.LoginMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

	private final LoginMapper loginMapper;

	public Map<String, Object> selectOne(Map<String, Object> param) {
		return loginMapper.selectOne(param);
	}
	
	public MemberDto selectUserInfo(String id) {
		return loginMapper.selectUserInfo(id);
	}
	
	public boolean login(String id, String pwd) {
		return loginMapper.login(id, pwd) > 0;
	}

}
