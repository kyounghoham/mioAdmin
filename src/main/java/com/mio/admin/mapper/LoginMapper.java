package com.mio.admin.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.mio.admin.dto.MemberDto;

@Mapper
public interface LoginMapper {

	public Map<String, Object> selectOne(Map<String, Object> param);

	public MemberDto selectUserInfo(String id);
	
	public int login(String id, String pwd);

}
