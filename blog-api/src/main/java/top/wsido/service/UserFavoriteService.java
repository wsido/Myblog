package top.wsido.service;

import top.wsido.model.vo.Result;

public interface UserFavoriteService {
    /**
     * 获取当前登录用户的收藏列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果，包含收藏的博客列表和总数
     */
    Result getFavorites(Integer pageNum, Integer pageSize);

    /**
     * 添加博客到当前登录用户的收藏
     * @param blogId 要收藏的博客ID
     * @return 操作结果
     */
    Result addFavorite(Long blogId);

    /**
     * 从当前登录用户的收藏中移除博客
     * @param blogId 要移除的博客ID
     * @return 操作结果
     */
    Result removeFavorite(Long blogId);

    /**
     * 检查当前登录用户是否已收藏某博客
     * @param blogId 博客ID
     * @return 操作结果，包含isFavorite布尔值
     */
    Result checkFavoriteStatus(Long blogId);
} 