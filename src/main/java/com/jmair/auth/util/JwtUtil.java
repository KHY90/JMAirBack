package com.jmair.auth.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jmair.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${spring.jwt.secret-key}")
	private String secretKey;

	private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 15;
	private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7;

	public String generateAccessToken(User user) {
		return Jwts.builder()
			.setSubject(user.getUserLogin())
			.claim("role", user.getUserGrade().toString())
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
	}

	public String generateRefreshToken(User user) {
		return Jwts.builder()
			.setSubject(user.getUserLogin())
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
	}

	public String validateAndExtractUserLogin(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
			.build()
			.parseClaimsJws(token)
			.getBody();
		return claims.getSubject();
	}
}
