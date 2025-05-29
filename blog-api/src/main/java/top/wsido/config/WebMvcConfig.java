package top.wsido.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * @Description: 配置MVC
 * @Author: wsido
 * @Date: 2020-07-22
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Value("${wsido.upload.access-path:/upload/**}")
	private String accessPath;
	
	@Value("${wsido.upload.word-images:/upload/word-images}")
	private String wordImagesPath;

	/**
	 * 配置静态资源文件路径
	 *
	 * @param registry 资源处理器注册表
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 配置上传文件夹的访问路径
		String uploadPath = System.getProperty("user.dir") + File.separator + "upload";
		registry.addResourceHandler("/upload/**")
				.addResourceLocations("file:" + uploadPath + File.separator);
	}
} 