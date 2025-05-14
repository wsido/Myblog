package top.wsido.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import top.wsido.entity.Blog;
import top.wsido.entity.User;
import top.wsido.entity.UserBlogFavorite;
import top.wsido.mapper.UserBlogFavoriteMapper;
import top.wsido.mapper.UserMapper;
import top.wsido.model.vo.Result;
import top.wsido.service.UserFavoriteService;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    @Autowired
    private UserBlogFavoriteMapper userBlogFavoriteMapper;

    @Autowired
    private UserMapper userMapper;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String username = authentication.getName();
        return userMapper.findByUsername(username);
    }

    @Override
    public Result getFavorites(Integer pageNum, Integer pageSize) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Blog> favoriteBlogs = userBlogFavoriteMapper.findFavoriteBlogsByUserId(currentUser.getId());
        PageInfo<Blog> pageInfo = new PageInfo<>(favoriteBlogs);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        
        return Result.ok("获取成功", data);
    }

    @Transactional
    @Override
    public Result addFavorite(Long blogId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        UserBlogFavorite existingFavorite = userBlogFavoriteMapper.findByUserIdAndBlogId(currentUser.getId(), blogId);
        if (existingFavorite != null) {
            return Result.error("您已收藏该博客");
        }

        UserBlogFavorite newFavorite = new UserBlogFavorite();
        newFavorite.setUserId(currentUser.getId());
        newFavorite.setBlogId(blogId);
        newFavorite.setCreateTime(new Date());
        
        if (userBlogFavoriteMapper.insertFavorite(newFavorite) > 0) {
            return Result.ok("收藏成功");
        } else {
            return Result.error("收藏失败"); // 或者抛出异常由全局异常处理器处理
        }
    }

    @Transactional
    @Override
    public Result removeFavorite(Long blogId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        UserBlogFavorite existingFavorite = userBlogFavoriteMapper.findByUserIdAndBlogId(currentUser.getId(), blogId);
        if (existingFavorite == null) {
            return Result.error("您还未收藏该博客");
        }

        if (userBlogFavoriteMapper.deleteByUserIdAndBlogId(currentUser.getId(), blogId) > 0) {
            return Result.ok("取消收藏成功");
        } else {
            return Result.error("取消收藏失败"); // 或者抛出异常
        }
    }

    @Override
    public Result checkFavoriteStatus(Long blogId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Map<String, Boolean> data = new HashMap<>();
            data.put("isFavorite", false);
            return Result.ok("用户未登录，默认为未收藏", data);
        }

        UserBlogFavorite favorite = userBlogFavoriteMapper.findByUserIdAndBlogId(currentUser.getId(), blogId);
        Map<String, Boolean> data = new HashMap<>();
        data.put("isFavorite", favorite != null);
        return Result.ok("获取成功", data);
    }
} 