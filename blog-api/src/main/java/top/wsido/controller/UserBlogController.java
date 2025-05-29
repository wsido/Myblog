package top.wsido.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import top.wsido.annotation.OperationLogger;
import top.wsido.entity.Blog;
import top.wsido.entity.Category;
import top.wsido.entity.Tag;
import top.wsido.entity.User;
import top.wsido.model.dto.BlogVisibility;
import top.wsido.model.vo.Result;
import top.wsido.service.BlogService;
import top.wsido.service.CategoryService;
import top.wsido.service.TagService;
import top.wsido.service.UserService;
import top.wsido.util.JwtUtils;
import top.wsido.util.StringUtils;

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
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private TagService tagService;
    
    /**
     * 获取用户的博客列表
     *
     * @param jwt JWT令牌
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param title 按标题模糊查询
     * @param categoryId 按分类id查询
     * @return 博客列表
     */
    @GetMapping("/list")
    public Result list(@RequestHeader("Authorization") String jwt,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(defaultValue = "") String title,
                       @RequestParam(required = false) Integer categoryId) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        // 计算起始索引
        int start = (pageNum - 1) * pageSize;
        
        // 获取用户的博客列表，增加按标题和分类ID过滤
        List<Blog> blogs = blogService.getBlogListByTitleAndCategoryIdAndUserId(title, categoryId, user.getId(), start, pageSize);
        int total = blogService.countBlogByTitleAndCategoryIdAndUserId(title, categoryId, user.getId());
        
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
    
    /**
     * 创建博客
     *
     * @param jwt JWT令牌
     * @param blog 博客数据
     * @return 创建结果
     */
    @OperationLogger("创建博客")
    @PostMapping
    public Result createBlog(@RequestHeader("Authorization") String jwt,
                             @RequestBody top.wsido.model.dto.Blog blog) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未登录或用户不存在");
        }

        // 验证普通字段
        if (StringUtils.isEmpty(blog.getTitle(), blog.getFirstPicture(), blog.getContent())
                || blog.getWords() == null || blog.getWords() < 0) {
            return Result.error("参数有误");
        }
        
        // 如果描述为空，自动生成
        if (StringUtils.isEmpty(blog.getDescription())) {
            // 从内容中提取前100个字符作为描述（去掉markdown标记）
            String content = blog.getContent().replaceAll("\\s*[#*`~>]+\\s*", "");
            blog.setDescription(content.length() > 100 ? content.substring(0, 100) : content);
        }

        // 处理分类
        Object cate = blog.getCate();
        if (cate == null) {
            return Result.error("分类不能为空");
        }
        if (cate instanceof Integer) { // 选择了已存在的分类
            Category c = categoryService.getCategoryById(((Integer) cate).longValue());
            blog.setCategory(c);
        } else if (cate instanceof String) { // 添加新分类
            // 查询分类是否已存在
            Category category = categoryService.getCategoryByName((String) cate);
            if (category != null) {
                return Result.error("不可添加已存在的分类");
            }
            Category c = new Category();
            c.setName((String) cate);
            categoryService.saveCategory(c);
            blog.setCategory(c);
        } else {
            return Result.error("分类不正确");
        }

        // 处理标签
        List<Object> tagList = blog.getTagList();
        List<Tag> tags = new ArrayList<>();
        for (Object t : tagList) {
            if (t instanceof Integer) { // 选择了已存在的标签
                Tag tag = tagService.getTagById(((Integer) t).longValue());
                tags.add(tag);
            } else if (t instanceof String) { // 添加新标签
                // 查询标签是否已存在
                Tag tag1 = tagService.getTagByName((String) t);
                if (tag1 != null) {
                    return Result.error("不可添加已存在的标签");
                }
                Tag tag = new Tag();
                tag.setName((String) t);
                tagService.saveTag(tag);
                tags.add(tag);
            } else {
                return Result.error("标签不正确");
            }
        }

        // 设置时间和用户
        Date date = new Date();
        if (blog.getReadTime() == null || blog.getReadTime() < 0) {
            blog.setReadTime((int) Math.round(blog.getWords() / 200.0)); // 粗略计算阅读时长
        }
        if (blog.getViews() == null || blog.getViews() < 0) {
            blog.setViews(0);
        }
        blog.setCreateTime(date);
        blog.setUpdateTime(date);
        blog.setUser(user); // 设置文章作者为当前用户

        try {
            // 保存博客
            blogService.saveBlog(blog);
            // 关联博客和标签(维护 blog_tag 表)
            for (Tag t : tags) {
                blogService.saveBlogTag(blog.getId(), t.getId());
            }
            return Result.ok("博客创建成功");
        } catch (Exception e) {
            return Result.error("博客创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新博客
     *
     * @param jwt JWT令牌
     * @param id 博客ID
     * @param blog 博客数据
     * @return 更新结果
     */
    @OperationLogger("更新博客")
    @PutMapping("/{id}")
    public Result updateBlog(@RequestHeader("Authorization") String jwt,
                             @PathVariable Long id,
                             @RequestBody top.wsido.model.dto.Blog blog) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未登录或用户不存在");
        }
        
        // 检查博客是否存在且属于当前用户
        Blog existingBlog = blogService.getBlogById(id);
        if (existingBlog == null) {
            return Result.error("博客不存在");
        }
        if (!existingBlog.getUser().getId().equals(user.getId())) {
            return Result.error("无权修改该博客");
        }
        
        // 设置ID
        blog.setId(id);
        
        // 验证普通字段
        if (StringUtils.isEmpty(blog.getTitle(), blog.getFirstPicture(), blog.getContent())
                || blog.getWords() == null || blog.getWords() < 0) {
            return Result.error("参数有误");
        }
        
        // 如果描述为空，自动生成
        if (StringUtils.isEmpty(blog.getDescription())) {
            // 从内容中提取前100个字符作为描述（去掉markdown标记）
            String content = blog.getContent().replaceAll("\\s*[#*`~>]+\\s*", "");
            blog.setDescription(content.length() > 100 ? content.substring(0, 100) : content);
        }

        // 处理分类
        Object cate = blog.getCate();
        if (cate == null) {
            return Result.error("分类不能为空");
        }
        if (cate instanceof Integer) { // 选择了已存在的分类
            Category c = categoryService.getCategoryById(((Integer) cate).longValue());
            blog.setCategory(c);
        } else if (cate instanceof String) { // 添加新分类
            // 查询分类是否已存在
            Category category = categoryService.getCategoryByName((String) cate);
            if (category != null) {
                return Result.error("不可添加已存在的分类");
            }
            Category c = new Category();
            c.setName((String) cate);
            categoryService.saveCategory(c);
            blog.setCategory(c);
        } else {
            return Result.error("分类不正确");
        }

        // 处理标签
        List<Object> tagList = blog.getTagList();
        List<Tag> tags = new ArrayList<>();
        for (Object t : tagList) {
            if (t instanceof Integer) { // 选择了已存在的标签
                Tag tag = tagService.getTagById(((Integer) t).longValue());
                tags.add(tag);
            } else if (t instanceof String) { // 添加新标签
                // 查询标签是否已存在
                Tag tag1 = tagService.getTagByName((String) t);
                if (tag1 != null) {
                    return Result.error("不可添加已存在的标签");
                }
                Tag tag = new Tag();
                tag.setName((String) t);
                tagService.saveTag(tag);
                tags.add(tag);
            } else {
                return Result.error("标签不正确");
            }
        }

        // 设置更新时间和用户
        blog.setUpdateTime(new Date());
        blog.setUser(user);
        // 保留创建时间
        blog.setCreateTime(existingBlog.getCreateTime());
        
        if (blog.getReadTime() == null || blog.getReadTime() < 0) {
            blog.setReadTime((int) Math.round(blog.getWords() / 200.0)); // 粗略计算阅读时长
        }
        
        try {
            // 更新博客
            blogService.updateBlog(blog);
            // 关联博客和标签(维护 blog_tag 表)
            blogService.deleteBlogTagByBlogId(blog.getId());
            for (Tag t : tags) {
                blogService.saveBlogTag(blog.getId(), t.getId());
            }
            return Result.ok("博客更新成功");
        } catch (Exception e) {
            return Result.error("博客更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分类和标签列表
     *
     * @return 分类和标签列表
     */
    @GetMapping("/categoryAndTag")
    public Result categoryAndTag(@RequestHeader("Authorization") String jwt) {
        // 获取当前登录用户
        String username = JwtUtils.getTokenBody(jwt).getSubject();
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        // 获取所有分类和标签
        List<Category> categories = categoryService.getCategoryList();
        List<Tag> tags = tagService.getTagList();
        
        Map<String, Object> map = new HashMap<>(4);
        map.put("categories", categories);
        map.put("tags", tags);
        return Result.ok("获取成功", map);
    }
} 