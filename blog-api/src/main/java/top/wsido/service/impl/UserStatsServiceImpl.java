package top.wsido.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import top.wsido.entity.User;
import top.wsido.mapper.BlogMapper;
import top.wsido.mapper.CommentMapper;
import top.wsido.mapper.UserMapper;
import top.wsido.model.vo.Result;
import top.wsido.model.vo.UserStatsVO;
import top.wsido.service.UserStatsService;

@Service
public class UserStatsServiceImpl implements UserStatsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private CommentMapper commentMapper;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String username = authentication.getName();
        return userMapper.findByUsername(username);
    }

    @Override
    public Result getUserStats() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        Long userId = currentUser.getId();

        Integer blogCount = blogMapper.countBlogByUserId(userId);
        Integer viewCount = blogMapper.sumViewsByUserId(userId);
        Integer commentCount = commentMapper.countCommentsByUserId(userId);

        UserStatsVO statsVO = new UserStatsVO();
        statsVO.setBlogCount(blogCount == null ? 0 : blogCount); // Mapper count方法通常不会返回null，但防御一下
        statsVO.setViewCount(viewCount == null ? 0 : viewCount); // XML中COALESCE处理了，这里也防御一下
        statsVO.setCommentCount(commentCount == null ? 0 : commentCount);

        return Result.ok("获取成功", statsVO);
    }
} 