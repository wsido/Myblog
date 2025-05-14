package top.wsido.service;

import top.wsido.model.vo.Result;

public interface UserStatsService {
    /**
     * 获取当前登录用户的统计数据
     * (博客数量、博客总访问量、收到的评论数)
     * @return 包含统计数据的Result对象
     */
    Result getUserStats();
} 