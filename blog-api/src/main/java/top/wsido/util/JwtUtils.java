package top.wsido.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;

/**
 * @Description: JWT工具类
 * @Author: wsido
 * @Date: 2020-09-02
 */
@Component
public class JwtUtils {
	private static long expireTime;
	private static String secretKey;

	@Value("${token.secretKey}")
	public void setSecretKey(String secretKey) {
		JwtUtils.secretKey = secretKey;
	}

	@Value("${token.expireTime}")
	public void setExpireTime(long expireTime) {
		JwtUtils.expireTime = expireTime;
	}

	/**
	 * 判断token是否存在
	 *
	 * @param token
	 * @return
	 */
	public static boolean judgeTokenIsExist(String token) {
		return token != null && !"".equals(token) && !"null".equals(token);
	}

	/**
	 * 生成token
	 *
	 * @param subject
	 * @return
	 */
	public static String generateToken(String subject) {
		String jwt = Jwts.builder()
				.setSubject(subject)
				.setExpiration(new Date(System.currentTimeMillis() + expireTime))
				.signWith(SignatureAlgorithm.HS512, secretKey)
				.compact();
		return jwt;
	}

	/**
	 * 生成带角色权限的token
	 *
	 * @param subject
	 * @param authorities
	 * @return
	 */
	public static String generateToken(String subject, Collection<? extends GrantedAuthority> authorities) {
		StringBuilder sb = new StringBuilder();
		for (GrantedAuthority authority : authorities) {
			sb.append(authority.getAuthority()).append(",");
		}
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		String jwt = Jwts.builder()
				.setSubject(subject)
				.claim("authorities", sb.toString())
				.setExpiration(new Date(System.currentTimeMillis() + expireTime))
				.signWith(SignatureAlgorithm.HS512, secretKey)
				.compact();
		return jwt;
	}

	/**
	 * 生成自定义过期时间token
	 *
	 * @param subject
	 * @param expireTime
	 * @return
	 */
	public static String generateToken(String subject, long expireTime) {
		String jwt = Jwts.builder()
				.setSubject(subject)
				.setExpiration(new Date(System.currentTimeMillis() + expireTime))
				.signWith(SignatureAlgorithm.HS512, secretKey)
				.compact();
		return jwt;
	}


	/**
	 * 获取tokenBody同时校验token是否有效（无效则会抛出异常）
	 *
	 * @param token
	 * @return
	 */
	public static Claims getTokenBody(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}
		Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
		return claims;
	}
} 