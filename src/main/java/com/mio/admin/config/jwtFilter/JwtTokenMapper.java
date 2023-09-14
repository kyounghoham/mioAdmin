package com.mio.admin.config.jwtFilter;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;

@Mapper
public interface JwtTokenMapper {
    public int deleteRefreshTokenByUserid(String user_id);

    public int deleteRefreshTokenByToken(String refreshToken);

    public int saveRefreshToken(@Param("user_id") String user_id,
                                @Param("refreshToken") String refreshToken,
                                @Param("expireTime") Timestamp expireTime);

    public int getRefreshTokenCnt(String refreshToken);
}
