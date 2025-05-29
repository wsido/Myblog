package top.wsido.controller.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication; // No longer needed directly here
// import org.springframework.security.core.context.SecurityContextHolder; // No longer needed directly here
import org.springframework.web.bind.annotation.*;
import top.wsido.model.dto.Blog; // Changed from BlogAdminView
import top.wsido.model.vo.PageResult;
import top.wsido.model.vo.Result;
import top.wsido.service.BlogService;
import top.wsido.service.CategoryService;
import top.wsido.service.TagService;
import top.wsido.util.SecurityUtils; // Changed from UserUtils
import top.wsido.entity.Category; // Add this import
import top.wsido.entity.Tag; // Add this import

import java.util.ArrayList; // Add this import for ArrayList
import java.util.HashMap; // Added for categories-tags
import java.util.List; // Added for List<Blog>
import java.util.Map;
import java.util.stream.Collectors; //确保导入

/**
 * @Description: 用户专属的内容管理 Controller (博客管理)
 */
@RestController
@RequestMapping("/api/user/management")
public class UserBlogManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserBlogManagementController.class); // Add logger instance

    @Autowired
    private BlogService blogService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private SecurityUtils securityUtils; // Added injection

    /**
     * 获取当前登录用户自己的博客列表 (分页，可带查询条件)
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param title    博客标题 (可选查询条件)
     * @param categoryId 分类ID (可选查询条件)
     * @return
     */
    @GetMapping("/blogs")
    public ResponseEntity<?> getMyBlogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String title,
                                        @RequestParam(required = false) Long categoryId) { // categoryId is Long here
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Result.error("用户未登录或无法识别用户ID"));
        }

        // 计算起始索引和每页数量
        int start = (pageNum - 1) * pageSize;
        
        Integer categoryIdInt = (categoryId != null) ? categoryId.intValue() : null;
        // 确保与Service接口定义一致，传递3个必要参数
        List<top.wsido.entity.Blog> blogs = blogService.getBlogListByTitleAndCategoryIdAndUserId(title, categoryIdInt, userId, start, pageSize);
        int total = blogService.countBlogByTitleAndCategoryIdAndUserId(title, categoryIdInt, userId);

        PageResult<top.wsido.entity.Blog> pageResult = new PageResult<>((int) Math.ceil((double) total / pageSize), blogs);

        return ResponseEntity.ok(Result.ok("获取我的博客列表成功", pageResult));
    }

    /**
     * 当前登录用户创建新博客
     *
     * @param blogRequest 博客内容 DTO (使用 dto.Blog)
     * @return
     */
    @PostMapping("/blogs")
    public ResponseEntity<?> createMyBlog(@RequestBody Blog blogRequest) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Result.error("用户未登录或无法识别用户ID"));
        }

        // 兼容处理：将categoryId映射为cate
        if (blogRequest.getCate() == null && blogRequest.getCategoryId() != null) {
            blogRequest.setCate(blogRequest.getCategoryId());
            logger.info("将categoryId映射为cate: {}", blogRequest.getCategoryId());
        }

        // 兼容处理：将tagIds映射为tagList
        if ((blogRequest.getTagList() == null || blogRequest.getTagList().isEmpty()) && 
            blogRequest.getTagIds() != null && !blogRequest.getTagIds().isEmpty()) {
            // 将List<Integer>转换为List<Object>
            List<Object> tagObjects = new ArrayList<>(blogRequest.getTagIds());
            blogRequest.setTagList(tagObjects);
            logger.info("将tagIds映射为tagList: {}", blogRequest.getTagIds());
        }

        // Validate basic fields (from UserBlogController)
        if (top.wsido.util.StringUtils.isEmpty(blogRequest.getTitle(), blogRequest.getFirstPicture(), blogRequest.getContent())
                || blogRequest.getWords() == null || blogRequest.getWords() < 0) {
            return ResponseEntity.status(400).body(Result.error("参数有误: 标题、首图、内容、字数不能为空"));
        }
        
        // Auto-generate description if empty (from UserBlogController)
        if (top.wsido.util.StringUtils.isEmpty(blogRequest.getDescription())) {
            String contentText = blogRequest.getContent().replaceAll("\\\\s*[#*`~>]+\\\\s*", "");
            blogRequest.setDescription(contentText.length() > 100 ? contentText.substring(0, 100) : contentText);
        }

        // Process Category (from UserBlogController & BlogAdminController logic)
        Object cateObj = blogRequest.getCate();
        if (cateObj == null) {
            return ResponseEntity.status(400).body(Result.error("分类不能为空"));
        }
        Category categoryEntity;
        if (cateObj instanceof Integer) {
            categoryEntity = categoryService.getCategoryById(((Integer) cateObj).longValue());
            if (categoryEntity == null) {
                return ResponseEntity.status(400).body(Result.error("选择的分类不存在"));
            }
        } else if (cateObj instanceof String) {
            String categoryName = (String) cateObj;
            if (top.wsido.util.StringUtils.isEmpty(categoryName)) {
                 return ResponseEntity.status(400).body(Result.error("分类名称不能为空"));
            }
            categoryEntity = categoryService.getCategoryByName(categoryName);
            if (categoryEntity == null) { // Create new if not exists
                categoryEntity = new Category();
                categoryEntity.setName(categoryName);
                // Assuming user cannot create new categories here, or add specific logic if they can
                // For now, if it's a string and not found, let's treat as error unless explicitly allowed to create
                // For simplicity, let's assume user can only select existing ones or service layer handles creation
                // Re-evaluating: User should be able to create on the fly IF BlogAdminController allows it.
                // Let's mirror BlogAdminController: if it's a string, try to save as new category.
                // BUT, users (ROLE_user) usually don't have rights to create categories.
                // Let's assume for /api/user/management, they can only pick existing.
                // If admin uses this endpoint, then it's a different story.
                // Given the error "category_id cannot be null", it means it's NOT finding/setting it.

                // For now, let's stick to user selecting existing categories for this endpoint.
                // If string is provided and not found, it's an error for the user.
                // Admin should use /admin/blog endpoint which has more robust category creation.
                return ResponseEntity.status(400).body(Result.error("分类名称 '" + categoryName + "' 不存在，请选择已有分类或联系管理员创建。"));
            }
        } else {
            return ResponseEntity.status(400).body(Result.error("分类参数类型不正确"));
        }
        blogRequest.setCategory(categoryEntity);
        if (categoryEntity != null) {
            blogRequest.setCategoryId(categoryEntity.getId().intValue());
        }


        // Process Tags (from UserBlogController & BlogAdminController logic)
        List<Object> tagObjList = blogRequest.getTagList();
        List<Tag> tagEntities = new ArrayList<>();
        if (tagObjList != null) {
            for (Object tObj : tagObjList) {
                Tag tagEntity;
                if (tObj instanceof Integer) {
                    tagEntity = tagService.getTagById(((Integer) tObj).longValue());
                    if (tagEntity == null) {
                        return ResponseEntity.status(400).body(Result.error("选择的标签ID: " + tObj + " 不存在"));
                    }
                } else if (tObj instanceof String) {
                    // Similar to categories, assume users select existing tags for this specific endpoint
                    String tagName = (String) tObj;
                     if (top.wsido.util.StringUtils.isEmpty(tagName)) {
                        return ResponseEntity.status(400).body(Result.error("标签名称不能为空"));
                    }
                    tagEntity = tagService.getTagByName(tagName);
                    if (tagEntity == null) {
                         return ResponseEntity.status(400).body(Result.error("标签名称 '" + tagName + "' 不存在，请选择已有标签或联系管理员创建。"));
                    }
                } else {
                    return ResponseEntity.status(400).body(Result.error("标签参数类型不正确"));
                }
                tagEntities.add(tagEntity);
            }
        }
        blogRequest.setTags(tagEntities); // Set the list of Tag entities

        // User is already set by BlogServiceImpl#saveBlog using SecurityUtils
        // Times are set by BlogServiceImpl

        try {
            blogService.saveBlog(blogRequest); // DTO now has Category entity and List<Tag> entities
            
            // After saveBlog, blogRequest.getId() should be populated.
            // Then save blog_tag associations
            if (blogRequest.getId() != null && !tagEntities.isEmpty()) {
                blogService.deleteBlogTagByBlogId(blogRequest.getId()); // Clear old tags first if any (though for new blog, not strictly needed)
                for (Tag t : tagEntities) {
                    blogService.saveBlogTag(blogRequest.getId(), t.getId());
                }
            }
            return ResponseEntity.ok(Result.ok("博客创建成功"));
        } catch (Exception e) {
            logger.error("Error creating blog for user: {}", userId, e); // Add logging
            return ResponseEntity.status(500).body(Result.error("博客创建失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户自己的指定博客详情 (供编辑页使用)
     *
     * @param blogId 博客ID
     * @return
     */
    @GetMapping("/blogs/{blogId}")
    public ResponseEntity<?> getMyBlogDetail(@PathVariable Long blogId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Result.error("用户未登录或无法识别用户ID"));
        }

        // TODO: BlogService需要一个新方法 getMyBlogById(Long blogId, Long userId) 
        //       该方法会校验博客是否属于该用户。
        // 现有的 blogService.getBlogById(Long id) 不校验用户。
        // 临时使用现有方法，并添加手动校验（理想情况下应在Service层完成）
        top.wsido.entity.Blog blogEntity = blogService.getBlogById(blogId);

        if (blogEntity == null) {
            return ResponseEntity.status(404).body(Result.error("博客不存在"));
        }
        // 假设 top.wsido.entity.Blog 有 getUserId() 方法
        if (blogEntity.getUser() == null || !userId.equals(blogEntity.getUser().getId())) {
             return ResponseEntity.status(403).body(Result.error("无权访问此博客的详情"));
        }
        // 返回DTO而不是Entity，或者Service层返回处理好的VO
        return ResponseEntity.ok(Result.ok("获取我的博客详情成功", blogEntity)); // 应该转换为DTO/VO
    }

    /**
     * 当前登录用户更新自己的指定博客
     *
     * @param blogId        博客ID
     * @param blogRequest 博客更新内容 DTO (使用 dto.Blog)
     * @return
     */
    @PutMapping("/blogs/{blogId}")
    public ResponseEntity<?> updateMyBlog(@PathVariable Long blogId, @RequestBody Blog blogRequest) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Result.error("用户未登录或无法识别用户ID"));
        }

        // Ensure DTO's ID is consistent with path variable
        if (blogRequest.getId() == null) {
            blogRequest.setId(blogId);
        } else if (!blogRequest.getId().equals(blogId)) {
            return ResponseEntity.status(400).body(Result.error("路径ID与请求体ID不匹配"));
        }
        
        // 兼容处理：将categoryId映射为cate
        if (blogRequest.getCate() == null && blogRequest.getCategoryId() != null) {
            blogRequest.setCate(blogRequest.getCategoryId());
            logger.info("更新博客：将categoryId映射为cate: {}", blogRequest.getCategoryId());
        }

        // 兼容处理：将tagIds映射为tagList
        if ((blogRequest.getTagList() == null || blogRequest.getTagList().isEmpty()) && 
            blogRequest.getTagIds() != null && !blogRequest.getTagIds().isEmpty()) {
            // 将List<Integer>转换为List<Object>
            List<Object> tagObjects = new ArrayList<>(blogRequest.getTagIds());
            blogRequest.setTagList(tagObjects);
            logger.info("更新博客：将tagIds映射为tagList: {}", blogRequest.getTagIds());
        }
        
        // Validate basic fields
        if (top.wsido.util.StringUtils.isEmpty(blogRequest.getTitle(), blogRequest.getFirstPicture(), blogRequest.getContent())
                || blogRequest.getWords() == null || blogRequest.getWords() < 0) {
            return ResponseEntity.status(400).body(Result.error("参数有误: 标题、首图、内容、字数不能为空"));
        }

        // Auto-generate description if empty
        if (top.wsido.util.StringUtils.isEmpty(blogRequest.getDescription())) {
            String contentText = blogRequest.getContent().replaceAll("\\\\s*[#*`~>]+\\\\s*", "");
            blogRequest.setDescription(contentText.length() > 100 ? contentText.substring(0, 100) : contentText);
        }

        // Process Category
        Object cateObj = blogRequest.getCate();
        if (cateObj == null) {
            return ResponseEntity.status(400).body(Result.error("分类不能为空"));
        }
        Category categoryEntity;
        if (cateObj instanceof Integer) {
            categoryEntity = categoryService.getCategoryById(((Integer) cateObj).longValue());
            if (categoryEntity == null) {
                return ResponseEntity.status(400).body(Result.error("选择的分类不存在"));
            }
        } else if (cateObj instanceof String) {
            String categoryName = (String) cateObj;
             if (top.wsido.util.StringUtils.isEmpty(categoryName)) {
                 return ResponseEntity.status(400).body(Result.error("分类名称不能为空"));
            }
            categoryEntity = categoryService.getCategoryByName(categoryName);
            if (categoryEntity == null) {
                 return ResponseEntity.status(400).body(Result.error("分类名称 '" + categoryName + "' 不存在，请选择已有分类或联系管理员创建。"));
            }
        } else {
            return ResponseEntity.status(400).body(Result.error("分类参数类型不正确"));
        }
        blogRequest.setCategory(categoryEntity);

        // Process Tags
        List<Object> tagObjList = blogRequest.getTagList();
        List<Tag> tagEntities = new ArrayList<>();
        if (tagObjList != null) {
            for (Object tObj : tagObjList) {
                Tag tagEntity;
                if (tObj instanceof Integer) {
                    tagEntity = tagService.getTagById(((Integer) tObj).longValue());
                     if (tagEntity == null) {
                        return ResponseEntity.status(400).body(Result.error("选择的标签ID: " + tObj + " 不存在"));
                    }
                } else if (tObj instanceof String) {
                    String tagName = (String) tObj;
                    if (top.wsido.util.StringUtils.isEmpty(tagName)) {
                        return ResponseEntity.status(400).body(Result.error("标签名称不能为空"));
                    }
                    tagEntity = tagService.getTagByName(tagName);
                    if (tagEntity == null) {
                        return ResponseEntity.status(400).body(Result.error("标签名称 '" + tagName + "' 不存在，请选择已有标签或联系管理员创建。"));
                    }
                } else {
                    return ResponseEntity.status(400).body(Result.error("标签参数类型不正确"));
                }
                tagEntities.add(tagEntity);
            }
        }
        blogRequest.setTags(tagEntities);
        
        // User & Times are handled by BlogServiceImpl#updateBlog

        try {
            // Service layer should verify that the blogId belongs to the current userId before updating
            blogService.updateBlog(blogRequest); // DTO has Category entity and List<Tag> entities

            // After updateBlog, update blog_tag associations
            if (!tagEntities.isEmpty()) {
                blogService.deleteBlogTagByBlogId(blogRequest.getId()); // Clear old tags
                for (Tag t : tagEntities) {
                    blogService.saveBlogTag(blogRequest.getId(), t.getId());
                }
            } else { // If new tagList is empty, ensure all old tags are removed
                blogService.deleteBlogTagByBlogId(blogRequest.getId());
            }
            return ResponseEntity.ok(Result.ok("博客更新成功"));
        } catch (Exception e) {
            logger.error("Error updating blog ID {} for user: {}", blogId, userId, e); // Add logging
            return ResponseEntity.status(500).body(Result.error("博客更新失败: " + e.getMessage()));
        }
    }

    /**
     * 当前登录用户删除自己的指定博客
     *
     * @param blogId 博客ID
     * @return
     */
    @DeleteMapping("/blogs/{blogId}")
    public ResponseEntity<?> deleteMyBlog(@PathVariable Long blogId) {
        Long userId = securityUtils.getCurrentUserId();
        logger.info("[deleteMyBlog] Attempting to delete blog. Current UserID: {}, BlogID to delete: {}", userId, blogId);

        if (userId == null) {
            logger.warn("[deleteMyBlog] Current UserID is null. Aborting deletion.");
            return ResponseEntity.status(401).body(Result.error("用户未登录或无法识别用户ID"));
        }

        top.wsido.entity.Blog blogEntity = blogService.getBlogById(blogId);
        if (blogEntity == null) {
            logger.info("[deleteMyBlog] Blog with ID: {} does not exist. Assuming deleted or returning 404.", blogId);
            return ResponseEntity.ok(Result.ok("博客已不存在"));
        }

        Long blogAuthorId = (blogEntity.getUser() != null) ? blogEntity.getUser().getId() : null;
        String blogAuthorUsername = (blogEntity.getUser() != null) ? blogEntity.getUser().getUsername() : "[User object is null]";
        logger.info("[deleteMyBlog] Blog Author ID: {}, Blog Author Username: {}", blogAuthorId, blogAuthorUsername);

        if (blogAuthorId == null || !userId.equals(blogAuthorId)) {
            logger.warn("[deleteMyBlog] Permission denied. Current UserID: {} does not match Blog Author ID: {}.", userId, blogAuthorId);
            return ResponseEntity.status(403).body(Result.error("无权删除此博客"));
        }

        try {
            blogService.deleteBlogById(blogId);
            return ResponseEntity.ok(Result.ok("博客删除成功"));
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.status(500).body(Result.error("博客删除失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户自己的博客ID和标题列表 (供评论筛选等使用)
     * @return Result
     */
    @GetMapping("/myBlogIdAndTitle")
    public Result getMyBlogIdAndTitleList() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            // This should ideally not happen if Spring Security is configured correctly
            // and this endpoint is protected.
            return Result.error(401, "用户未登录或无法识别用户ID");
        }
        try {
            List<Map<String, Object>> blogIdAndTitleList = blogService.getBlogIdAndTitleListByUserId(userId);
            return Result.ok("获取我的博客ID和标题列表成功", blogIdAndTitleList);
        } catch (Exception e) {
            // Consider logging the exception e.g., log.error("Error fetching blog ID and title list for user: {}", userId, e);
            return Result.error(500, "获取博客列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有分类和标签列表 (供写博客/编辑博客时选择)
     *
     * @return
     */
    @GetMapping("/categories-tags")
    public ResponseEntity<?> getAllCategoriesAndTags() {
        // 这个接口用户和管理员都可以调用，数据是公共的
        // Service层直接调用现有的 categoryService.getAllCategories() 和 tagService.getAllTags() 即可
        // 假设它们返回 List<Category> 和 List<Tag> (实体或VO)
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("categories", categoryService.getCategoryList()); // 假设有getCategoryList()或类似方法
            map.put("tags", tagService.getTagList()); // 假设有getTagList()或类似方法
            return ResponseEntity.ok(Result.ok("获取分类和标签成功", map));
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.status(500).body(Result.error("获取分类和标签失败: " + e.getMessage()));
        }
    }
} 