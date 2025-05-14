package top.wsido.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.web.client.RestTemplate;

/**
 * @Description: Redis序列化配置
 */
@Configuration
public class RedisSerializeConfig {

	/**
	 * 使用JSON序列化方式
	 *
	 * @param redisConnectionFactory
	 * @return
	 */
	@Bean
	public RedisTemplate<Object, Object> jsonRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		template.setDefaultSerializer(serializer);
		return template;
	}
	
	/**
	 * 创建RestTemplate Bean
	 * 
	 * @return RestTemplate实例
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
