package top.wsido.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import top.wsido.annotation.OperationLogger;
import top.wsido.entity.Blog;
import top.wsido.entity.User;
import top.wsido.model.dto.BlogVisibility;
import top.wsido.model.vo.Result;
import top.wsido.service.BlogService;
import top.wsido.service.UserService;
import top.wsido.util.JwtUtils;

/**
 * @Description: 用户博客管理API
 * @Author: wsido
 */
@RestController
@RequestMapping("/user/blog")
public class UserBlogController {
    @Autowired
    private BlogService blogService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户的博客列表
     *
     * @param jwt JWT令牌
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 博客列表
     */
    @GetMapping("/list")
    public Result list(@RequestHeader("Authorization") String jwt,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        // 计算起始索引
        int start = (pageNum - 1) * pageSize;
        
        // 获取用户的博客列表
        List<Blog> blogs = blogService.getBlogListByUserId(user.getId(), start, pageSize);
        int total = blogService.countBlogByUserId(user.getId());
        
        Map<String, Object> map = new HashMap<>();
        map.put("blogs", blogs);
        map.put("total", total);
        return Result.ok("获取成功", map);
    }
    
    /**
     * 获取博客详情
     *
     * @param jwt JWT令牌
     * @param id 博客ID
     * @return 博客详情
     */
    @GetMapping("/{id}")
    public Result getBlog(@RequestHeader("Authorization") String jwt, @PathVariable Long id) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        
        // 获取博客
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        
        // 验证当前用户是否有权限查看
        if (!blog.getUser().getId().equals(user.getId())) {
            return Result.error("无权查看该博客");
        }
        
        return Result.ok("获取成功", blog);
    }
    
    /**
     * 更新博客可见性
     *
     * @param jwt JWT令牌
     * @param id 博客ID
     * @param bv 可见性
     * @return 更新结果
     */
    @OperationLogger("更新博客可见性")
    @PutMapping("/{id}/visibility")
    public Result updateVisibility(@RequestHeader("Authorization") String jwt,
                                   @PathVariable Long id,
                                   @RequestBody BlogVisibility bv) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        
        // 获取博客
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        
        // 验证当前用户是否有权限修改
        if (!blog.getUser().getId().equals(user.getId())) {
            return Result.error("无权修改该博客");
        }
        
        // 更新博客可见性
        blogService.updateBlogVisibilityById(id, bv);
        return Result.ok("更新成功");
    }
    
    /**
     * 删除博客
     *
     * @param jwt JWT令牌
     * @param id 博客ID
     * @return 删除结果
     */
    @OperationLogger("删除博客")
    @DeleteMapping("/{id}")
    public Result delete(@RequestHeader("Authorization") String jwt, @PathVariable Long id) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        
        // 获取博客
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        
        // 验证当前用户是否有权限删除
        if (!blog.getUser().getId().equals(user.getId())) {
            return Result.error("无权删除该博客");
        }
        
        // 删除博客
        blogService.deleteBlogById(id);
        return Result.ok("删除成功");
    }
} 