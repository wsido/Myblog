package top.wsido.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Claims;
import top.wsido.model.vo.Result;
import top.wsido.util.JacksonUtils;
import top.wsido.util.JwtUtils;

/**
 * @Description: JWT请求过滤器
 */
public class JwtFilter extends GenericFilterBean {
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String requestURI = request.getRequestURI();

		String jwt = request.getHeader("Authorization");
		// 始终打印日志，以便观察所有请求的JWT情况
		System.out.println("JwtFilter: Processing URI = " + requestURI + ", JWT in Header = " + (jwt != null && jwt.startsWith("Bearer ") ? "Present" : "Absent or malformed"));
		
		if (JwtUtils.judgeTokenIsExist(jwt)) {
			try {
				Claims claims = JwtUtils.getTokenBody(jwt);
				String username = claims.getSubject();
				System.out.println("JwtFilter: Username (subject) from token = [" + username + "]");

				Object authoritiesClaim = claims.get("authorities");
				System.out.println("JwtFilter: 'authorities' claim from token = [" + authoritiesClaim + "] (type: " + (authoritiesClaim != null ? authoritiesClaim.getClass().getName() : "null") + ")");

				if (authoritiesClaim == null) {
				    System.out.println("JwtFilter: 'authorities' claim is missing in token for URI: " + requestURI);
				    // 对于缺失authorities的情况，可以选择不抛出异常并继续，让后续的授权逻辑处理
                    // 或者如果严格要求，则抛出异常或返回错误
                    // throw new Exception("Missing 'authorities' claim in token");
				}

                // 只有当authoritiesClaim存在时才进行解析
                if (authoritiesClaim instanceof String) {
                    String authoritiesString = (String) authoritiesClaim;
                    System.out.println("JwtFilter: Authorities string to be parsed = [" + authoritiesString + "]");
                    List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesString);
                    System.out.println("JwtFilter: Parsed GrantedAuthorities = [" + authorities + "]");

                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(token);
                    System.out.println("JwtFilter: Authentication set in SecurityContext for user: [" + username + "] with authorities: [" + authorities + "] for URI: " + requestURI);
                } else if (authoritiesClaim != null) {
                     System.out.println("JwtFilter: 'authorities' claim is not a String: " + authoritiesClaim.getClass().getName() + " for URI: " + requestURI);
                     // 根据需要处理非字符串类型的authorities，或记录错误
                }
                // 如果authoritiesClaim为null，则不会设置authentication，用户将是匿名的或未认证的

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("JwtFilter: Token parsing/processing failed for URI: " + requestURI + " - " + e.getMessage());
				// 清除可能存在的无效认证信息
				SecurityContextHolder.clearContext(); 
				
				// 通常不应由JwtFilter直接返回HTTP响应，除非是致命的、不可恢复的token错误
				// 更好的做法是让请求继续，由后续的AccessDeniedHandler或AuthenticationEntryPoint处理
				// 如果确实要在这里返回，例如对于特定路径或特定错误：
				// response.setContentType("application/json;charset=utf-8");
				// Result result = Result.create(401, "Unauthorized: Invalid Token"); // 401更合适
				// PrintWriter out = response.getWriter();
				// out.write(JacksonUtils.writeValueAsString(result));
				// out.flush();
				// out.close();
				// return; // 如果返回响应，则需要return
			}
		} else {
			System.out.println("JwtFilter: No valid JWT provided in Authorization header for URI: " + requestURI);
            // 如果没有提供token，也清除一下上下文，以防万一
            // SecurityContextHolder.clearContext(); // 可选，取决于后续过滤器行为
		}
		
		filterChain.doFilter(servletRequest, servletResponse);
	}
}