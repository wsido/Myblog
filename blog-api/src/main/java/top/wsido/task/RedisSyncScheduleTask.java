package top.wsido.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.wsido.constant.RedisKeyConstants;
import top.wsido.service.BlogService;
import top.wsido.service.RedisService;

import java.util.Map;
import java.util.Set;

/**
 * @Description: Redis相关定时任务
 * @Author: wsido
 * @Date: 2020-11-02
 */
@Component
public class RedisSyncScheduleTask {
	@Autowired
	RedisService redisService;
	@Autowired
	BlogService blogService;

	/**
	 * 从Redis同步博客文章浏览量到数据库
	 */
	public void syncBlogViewsToDatabase() {
		String redisKey = RedisKeyConstants.BLOG_VIEWS_MAP;
		Map blogViewsMap = redisService.getMapByHash(redisKey);
		Set<Integer> keys = blogViewsMap.keySet();
		for (Integer key : keys) {
			Integer views = (Integer) blogViewsMap.get(key);
			blogService.updateViews(key.longValue(), views);
		}
	}
}
