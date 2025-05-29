package top.wsido.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import top.wsido.service.LoginLogService;
import top.wsido.service.impl.UserServiceImpl;

/**
 * @Description: Spring Security配置类
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	UserServiceImpl userService;
	@Autowired
	LoginLogService loginLogService;
	@Autowired
	MyAuthenticationEntryPoint myAuthenticationEntryPoint;

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				//禁用 csrf 防御
				.csrf().disable()
				//开启跨域支持
				.cors().and()
				//基于Token，不创建会话
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.authorizeRequests()
				//放行获取网页标题后缀的请求
				.antMatchers("/admin/webTitleSuffix").permitAll()
				//放行用户注册接口
				.antMatchers("/user/register").permitAll()
				//放行测试接口
				.antMatchers("/user/test").permitAll()

				// 用户专属内容管理接口
				// Specific rule for user categories and tags GET request
				.antMatchers(HttpMethod.GET, "/api/user/management/categories-tags").hasAnyAuthority("ROLE_admin", "ROLE_user")
				// General rule for other user management APIs (e.g., POST, PUT, DELETE for blogs, moments, comments by user)
				.antMatchers("/api/user/management/**").hasAnyAuthority("ROLE_admin", "ROLE_user")

				// --- Admin specific configurations START ---
				// Publicly accessible admin utility
				.antMatchers("/admin/webTitleSuffix").permitAll()

				// Admin access to user information and dashboard (also accessible by users/visitors for their own info)
				.antMatchers(HttpMethod.GET, "/admin/user").hasAnyAuthority("ROLE_admin", "ROLE_user", "ROLE_visitor")
				.antMatchers(HttpMethod.GET, "/admin/dashboard").hasAnyAuthority("ROLE_admin", "ROLE_user", "ROLE_visitor")
				
				// Admin management of all categories and tags (CMS admin panel specific)
				.antMatchers(HttpMethod.GET, "/admin/categoryAndTag").hasAnyAuthority("ROLE_admin", "ROLE_user") // ROLE_user for admin writing their own blog using shared component
				.antMatchers(HttpMethod.POST, "/admin/category", "/admin/tag").hasAuthority("ROLE_admin")
				.antMatchers(HttpMethod.PUT, "/admin/category", "/admin/tag").hasAuthority("ROLE_admin")
				.antMatchers(HttpMethod.DELETE, "/admin/category/**", "/admin/tag/**").hasAuthority("ROLE_admin")
				.antMatchers(HttpMethod.GET, "/admin/categories", "/admin/tags").hasAuthority("ROLE_admin")


				// Admin management of all blogs
				.antMatchers(HttpMethod.GET, "/admin/blogs", "/admin/blogIdAndTitle").hasAnyAuthority("ROLE_admin", "ROLE_user") // ROLE_user for admin using shared components
				.antMatchers(HttpMethod.POST, "/admin/blog").hasAuthority("ROLE_admin")
				.antMatchers(HttpMethod.PUT, "/admin/blog/**").hasAuthority("ROLE_admin") // Covers /admin/blog/visibility, /admin/blog/top etc.
				.antMatchers(HttpMethod.DELETE, "/admin/blog/**").hasAuthority("ROLE_admin")
				.antMatchers(HttpMethod.GET, "/admin/blog/**").hasAuthority("ROLE_admin") // For specific blog details by admin


				// Admin management of all moments
				.antMatchers(HttpMethod.GET, "/admin/moments").hasAuthority("ROLE_admin")
				.antMatchers("/admin/moment/**").hasAuthority("ROLE_admin") // Covers POST, PUT, DELETE for moments by admin

				// Admin management of all comments
				.antMatchers(HttpMethod.GET, "/admin/comments").hasAuthority("ROLE_admin")
				.antMatchers("/admin/comment/**").hasAuthority("ROLE_admin") // Covers POST, PUT, DELETE for comments by admin

				// Admin management of friend links
				.antMatchers("/admin/friends/**").hasAuthority("ROLE_admin")
				.antMatchers("/admin/friend/**").hasAuthority("ROLE_admin")
				.antMatchers("/admin/friendInfo/**").hasAuthority("ROLE_admin")
				
				// Admin management of schedule jobs
				.antMatchers("/admin/jobs/**").hasAuthority("ROLE_admin")
				.antMatchers("/admin/job/**").hasAuthority("ROLE_admin")

				// Admin management of site settings, etc. (add more specific admin paths here)
				// .antMatchers("/admin/site-settings").hasAuthority("ROLE_admin")

				// General fallback for any other /admin/** GET requests (e.g., admin profile page if it's a GET)
				.antMatchers(HttpMethod.GET, "/admin/**").hasAnyAuthority("ROLE_admin", "ROLE_visitor")
				// General fallback for any other /admin/** non-GET requests
				.antMatchers("/admin/**").hasAuthority("ROLE_admin")
				// --- Admin specific configurations END ---

				// User specific functionalities (excluding /user/register which is permitAll)
				.antMatchers("/user/**").hasAnyAuthority("ROLE_admin", "ROLE_user")
				//其它路径全部放行
				.anyRequest().permitAll()
				.and()
				//自定义JWT过滤器
				.addFilterBefore(new JwtLoginFilter("/admin/login", authenticationManager(), loginLogService), UsernamePasswordAuthenticationFilter.class)
				//添加普通用户登录过滤器
				.addFilterBefore(new JwtLoginFilter("/user/login", authenticationManager(), loginLogService), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)
				//未登录时，返回json，在前端执行重定向
				.exceptionHandling().authenticationEntryPoint(myAuthenticationEntryPoint);
	}
}
