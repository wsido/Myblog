package top.wsido.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import top.wsido.entity.Blog;
import top.wsido.entity.UserBlogFavorite;

@Mapper
@Repository
public interface UserBlogFavoriteMapper {
    /**
     * 根据用户ID分页查询收藏的博客列表
     * (Service层应在此方法调用前使用PageHelper.startPage)
     * @param userId 用户ID
     * @return 收藏的博客列表
     */
    List<Blog> findFavoriteBlogsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和博客ID查询收藏记录
     * @param userId 用户ID
     * @param blogId 博客ID
     * @return 收藏记录，如果不存在则返回null
     */
    UserBlogFavorite findByUserIdAndBlogId(@Param("userId") Long userId, @Param("blogId") Long blogId);
    
    /**
     * 新增收藏记录
     * @param favorite 收藏记录实体
     * @return 影响行数
     */
    int insertFavorite(UserBlogFavorite favorite);
    
    /**
     * 根据用户ID和博客ID删除收藏记录
     * @param userId 用户ID
     * @param blogId 博客ID
     * @return 影响行数
     */
    int deleteByUserIdAndBlogId(@Param("userId") Long userId, @Param("blogId") Long blogId);
} 