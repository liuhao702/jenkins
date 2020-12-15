package com.lc.bxm.common.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class TokenUtil {
	
	    // 私钥
		public final static String sercetKey = "InMySchoolOnline";

		//代表token的有效时间 
		//设置token失效时间 Long 类型（60000L=1分钟）
		//tokenTime=600000L
		public final static Long keeptime =600000L;

	/**
           * 初始化生成token的参数
     * @param userId
     * @return String
     */
    public static String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>(1);
        claims.put("sub", userId);
        return generateToken(claims,keeptime);
    }
 
    /**
             *  生成token
     * @param claims
     * @return String
     */
    private static String generateToken(Map<String, Object> claims,Long ttlMillis) {
    	// 生成JWT的时间
    	long nowMillis = System.currentTimeMillis();
    	long expMillis = nowMillis + ttlMillis;
		Date exp = new Date(expMillis);
//		System.err.println("过期时间"+exp);
//		System.err.println("签发时间"+new Date());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(exp)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, sercetKey)
                .compact();
    }
    //刷新token
    public static String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = Jwts.parser()
                    .setSigningKey(sercetKey)
                    .parseClaimsJws(token)
                    .getBody();
            refreshedToken = generateToken(claims,keeptime);
        } catch (Exception e) {
        	e.printStackTrace();
            refreshedToken = null;
        }
        return refreshedToken;
    }
    //校验token
    public static String verifyToken(String token) {
        String result = "";
        @SuppressWarnings("unused")
		Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(sercetKey)
                    .parseClaimsJws(token)
                    .getBody();
            result = "4001";
        } catch (Exception e) {
            result = "4005";
        }
        return result;
    }
}
