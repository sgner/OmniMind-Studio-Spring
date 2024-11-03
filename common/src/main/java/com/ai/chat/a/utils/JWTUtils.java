package com.ai.chat.a.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JWTUtils {
    public static String createJWT(long ttl, String secretKey, Map<String,Object> claims){
        long expMillis = System.currentTimeMillis() + ttl;
        Date exp = new Date(expMillis);
        JwtBuilder builder = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .setClaims(claims).setExpiration(exp);
        return builder.compact();
    }
    public static Claims parseJwt (String secretKey,String JWT){
       return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(JWT).getBody();
    }
}
