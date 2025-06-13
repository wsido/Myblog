package top.wsido.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import top.wsido.interceptor.AccessLimitInterceptor;

import java.io.File;

/**
 * @Description: 配置CORS跨域支持、拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	AccessLimitInterceptor accessLimitInterceptor;

	/**
	 * 跨域请求
	 *
	 * @param registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("*")
				.allowedHeaders("*")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
				.maxAge(3600);
	}

	/**
	 * 请求拦截器
	 *
	 * @param registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(accessLimitInterceptor);
	}

	/**
	 * 本地静态资源路径映射
	 *
	 * @param registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 配置上传文件夹的访问路径
		String uploadPath = System.getProperty("user.dir") + File.separator + "upload";
		registry.addResourceHandler("/upload/**")
				.addResourceLocations("file:" + uploadPath + File.separator);

		// 配置封面图文件夹的访问路径
		String coversPath = System.getProperty("user.dir") + File.separator + "covers";
		registry.addResourceHandler("/covers/**")
				.addResourceLocations("file:" + coversPath + File.separator);
	}
}
