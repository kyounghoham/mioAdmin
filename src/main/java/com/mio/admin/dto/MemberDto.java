package com.mio.admin.dto;

import lombok.Data;

@Data
public class MemberDto {
	private int idx;
	private String id;
	private String password;
	private String name;
	private String email;
}
