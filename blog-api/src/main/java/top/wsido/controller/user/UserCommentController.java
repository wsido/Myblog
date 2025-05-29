package top.wsido.controller.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import top.wsido.annotation.OperationLogger;
import top.wsido.entity.Comment;
import top.wsido.entity.User;
import top.wsido.model.vo.Result;
import top.wsido.service.CommentService;
import top.wsido.service.UserService;

import java.util.List;

/**
 * @Description: 用户评论管理
 * @Author: wsido
 * @Date: 2025-05-23
 */
@RestController
@RequestMapping("/user")
public class UserCommentController {
    
    @Autowired
    CommentService commentService;
    
    @Autowired
    UserService userService;

    /**
     * 获取当前用户的评论列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param page     页面类型 (0博客, 1关于我, 2友人帐) - 可选
     * @param blogId   博客ID (如果page=0) - 可选
     * @return 评论列表和分页信息
     */
    @GetMapping("/comment/list")
    public Result getCommentList(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Long blogId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未找到");
        }
        
        String orderBy = "create_time desc";
        PageHelper.startPage(pageNum, pageSize, orderBy);
        List<Comment> comments = commentService.getListByNicknameAndPageAndBlogId(user.getNickname(), page, blogId);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        
        return Result.ok("获取成功", pageInfo);
    }
    
    /**
     * 更新评论公开状态
     *
     * @param id        评论id
     * @param published 是否公开
     * @return
     */
    @OperationLogger("更新评论公开状态")
    @PutMapping("/comment/published")
    public Result updatePublished(@RequestParam Long id, @RequestParam Boolean published) {
        // 从SpringSecurity上下文获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未找到");
        }
        
        // 验证评论是否属于当前用户
        Comment comment = commentService.getCommentById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查评论昵称与用户昵称是否匹配
        if (!user.getNickname().equals(comment.getNickname())) {
            return Result.error("无权操作此评论");
        }
        
        commentService.updateCommentPublishedById(id, published);
        return Result.ok("操作成功");
    }
    
    /**
     * 更新评论接收邮件提醒状态
     *
     * @param id     评论id
     * @param notice 是否接收提醒
     * @return
     */
    @OperationLogger("更新评论邮件提醒状态")
    @PutMapping("/comment/notice")
    public Result updateNotice(@RequestParam Long id, @RequestParam Boolean notice) {
        // 从SpringSecurity上下文获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未找到");
        }
        
        // 验证评论是否属于当前用户
        Comment comment = commentService.getCommentById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查评论昵称与用户昵称是否匹配
        if (!user.getNickname().equals(comment.getNickname())) {
            return Result.error("无权操作此评论");
        }
        
        commentService.updateCommentNoticeById(id, notice);
        return Result.ok("操作成功");
    }
    
    /**
     * 删除评论
     *
     * @param id 评论id
     * @return
     */
    @OperationLogger("删除评论")
    @DeleteMapping("/comment/{id}")
    public Result deleteComment(@PathVariable Long id) {
        // 从SpringSecurity上下文获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未找到");
        }
        
        // 验证评论是否属于当前用户
        Comment comment = commentService.getCommentById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查评论昵称与用户昵称是否匹配
        if (!user.getNickname().equals(comment.getNickname())) {
            return Result.error("无权操作此评论");
        }
        
        commentService.deleteCommentById(id);
        return Result.ok("删除成功");
    }
    
    /**
     * 修改评论
     *
     * @param id      评论id
     * @param comment 评论实体
     * @return
     */
    @OperationLogger("修改评论")
    @PutMapping("/comment/{id}")
    public Result updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        // 从SpringSecurity上下文获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未找到");
        }
        
        // 验证评论是否属于当前用户
        Comment existingComment = commentService.getCommentById(id);
        if (existingComment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查评论昵称与用户昵称是否匹配
        if (!user.getNickname().equals(existingComment.getNickname())) {
            return Result.error("无权操作此评论");
        }
        
        // 设置ID确保更新正确的评论
        comment.setId(id);
        
        // 保持某些属性不变
        comment.setNickname(existingComment.getNickname());
        comment.setEmail(existingComment.getEmail());
        comment.setBlog(existingComment.getBlog());
        comment.setParentCommentId(existingComment.getParentCommentId());
        comment.setPage(existingComment.getPage());
        comment.setAdminComment(existingComment.getAdminComment());
        comment.setCreateTime(existingComment.getCreateTime());
        
        commentService.updateComment(comment);
        return Result.ok("修改成功");
    }
} 