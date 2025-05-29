package top.wsido.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import top.wsido.annotation.OperationLogger;
import top.wsido.entity.Blog;
import top.wsido.entity.Category;
import top.wsido.entity.Tag;
import top.wsido.model.dto.BlogVisibility;
import top.wsido.model.vo.Result;
import top.wsido.service.BlogService;
import top.wsido.service.CategoryService;
import top.wsido.service.CommentService;
import top.wsido.service.TagService;
import top.wsido.util.SecurityUtils;
import top.wsido.util.StringUtils;
import top.wsido.entity.User;
import top.wsido.exception.PersistenceException;

/**
 * @Description: 博客文章后台管理
 * @Author: wsido
 * @Date: 2020-07-29
 */
@RestController
@RequestMapping("/admin")
public class BlogAdminController {
	private static final Logger logger = LoggerFactory.getLogger(BlogAdminController.class);

	@Autowired
	private BlogService blogService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private TagService tagService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private SecurityUtils securityUtils;

	/**
	 * 获取博客文章列表
	 *
	 * @param title      按标题模糊查询
	 * @param categoryId 按分类id查询
	 * @param pageNum    页码
	 * @param pageSize   每页个数
	 * @return
	 */
	@GetMapping("/blogs")
	public Result blogs(@RequestParam(defaultValue = "") String title,
	                    @RequestParam(defaultValue = "") Integer categoryId,
	                    @RequestParam(defaultValue = "1") Integer pageNum,
	                    @RequestParam(defaultValue = "10") Integer pageSize) {
		String orderBy = "create_time desc";
		PageHelper.startPage(pageNum, pageSize, orderBy);
		PageInfo<Blog> pageInfo = new PageInfo<>(blogService.getListByTitleAndCategoryId(title, categoryId));
		List<Category> categories = categoryService.getCategoryList();
		Map<String, Object> map = new HashMap<>(4);
		map.put("blogs", pageInfo);
		map.put("categories", categories);
		return Result.ok("请求成功", map);
	}

	/**
	 * 删除博客文章、删除博客文章下的所有评论、同时维护 blog_tag 表
	 *
	 * @param id 文章id
	 * @return
	 */
	@OperationLogger("删除博客")
	@DeleteMapping("/blog")
	public Result delete(@RequestParam Long id) {
		blogService.deleteBlogTagByBlogId(id);
		blogService.deleteBlogById(id);
		commentService.deleteCommentsByBlogId(id);
		return Result.ok("删除成功");
	}

	/**
	 * 获取分类列表和标签列表
	 *
	 * @return
	 */
	@GetMapping("/categoryAndTag")
	public Result categoryAndTag() {
		User currentUser = securityUtils.getCurrentUser();
		logger.info("[BlogAdminController.categoryAndTag] Current user from SecurityUtils: Username: {}, Type: {}", 
		            (currentUser != null ? currentUser.getUsername() : "null"), 
		            (currentUser != null ? currentUser.getType() : "null"));

		List<Category> categories = categoryService.getCategoryList();
		List<Tag> tags = tagService.getTagList();

		// 添加日志开始
		logger.info("[BlogAdminController.categoryAndTag] Fetched categories size: {}", (categories != null ? categories.size() : "null"));
		if (categories != null && !categories.isEmpty()) {
		    for (Category cat : categories) {
		        logger.info("[BlogAdminController.categoryAndTag] Category: ID={}, Name={}", cat.getId(), cat.getName());
		    }
		} else {
		    logger.info("[BlogAdminController.categoryAndTag] Categories list is null or empty.");
		}

		logger.info("[BlogAdminController.categoryAndTag] Fetched tags size: {}", (tags != null ? tags.size() : "null"));
		if (tags != null && !tags.isEmpty()) {
		    for (Tag tag : tags) {
		        logger.info("[BlogAdminController.categoryAndTag] Tag: ID={}, Name={}, Color={}", tag.getId(), tag.getName(), tag.getColor());
		    }
		} else {
		    logger.info("[BlogAdminController.categoryAndTag] Tags list is null or empty.");
		}
		// 添加日志结束

		Map<String, Object> map = new HashMap<>(4);
		map.put("categories", categories);
		map.put("tags", tags);
		return Result.ok("请求成功", map);
	}

	/**
	 * 更新博客置顶状态
	 *
	 * @param id  博客id
	 * @param top 是否置顶
	 * @return
	 */
	@OperationLogger("更新博客置顶状态")
	@PutMapping("/blog/top")
	public Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {
		blogService.updateBlogTopById(id, top);
		return Result.ok("操作成功");
	}

	/**
	 * 更新博客推荐状态
	 *
	 * @param id        博客id
	 * @param recommend 是否推荐
	 * @return
	 */
	@OperationLogger("更新博客推荐状态")
	@PutMapping("/blog/recommend")
	public Result updateRecommend(@RequestParam Long id, @RequestParam Boolean recommend) {
		blogService.updateBlogRecommendById(id, recommend);
		return Result.ok("操作成功");
	}

	/**
	 * 更新博客可见性状态
	 *
	 * @param id             博客id
	 * @param blogVisibility 博客可见性DTO
	 * @return
	 */
	@OperationLogger("更新博客可见性状态")
	@PutMapping("blog/{id}/visibility")
	public Result updateVisibility(@PathVariable Long id, @RequestBody BlogVisibility blogVisibility) {
		blogService.updateBlogVisibilityById(id, blogVisibility);
		return Result.ok("操作成功");
	}

	/**
	 * 按id获取博客详情
	 *
	 * @param id 博客id
	 * @return
	 */
	@GetMapping("/blog/{id}")
	public Result getBlog(@PathVariable Long id) {
		Blog blog = blogService.getBlogById(id);
		if (blog == null) {
			return Result.error("博客不存在");
		}
		return Result.ok("获取成功", blog);
	}

	/**
	 * 保存草稿或发布新文章
	 *
	 * @param blog 博客文章DTO
	 * @return
	 */
	@OperationLogger("发布博客")
	@PostMapping("/blog")
	public Result saveBlog(@RequestBody top.wsido.model.dto.Blog blog) {
		return getResult(blog, "save");
	}

	/**
	 * 更新博客
	 *
	 * @param blog 博客文章DTO
	 * @return
	 */
	@OperationLogger("更新博客")
	@PutMapping("/blog")
	public Result updateBlog(@RequestBody top.wsido.model.dto.Blog blog) {
		return getResult(blog, "update");
	}

	/**
	 * 执行博客添加或更新操作：校验参数是否合法，添加分类、标签，维护博客标签关联表
	 *
	 * @param blog 博客文章DTO
	 * @param type 添加或更新
	 * @return
	 */
	private Result getResult(top.wsido.model.dto.Blog blog, String type) {
		//验证普通字段
		if (StringUtils.isEmpty(blog.getTitle(), blog.getFirstPicture(), blog.getContent(), blog.getDescription())
				|| blog.getWords() == null || blog.getWords() < 0) {
			return Result.error("参数有误");
		}

		//处理分类
		Object cate = blog.getCate();
		if (cate == null) {
			return Result.error("分类不能为空");
		}
		if (cate instanceof Integer) {//选择了已存在的分类
			Category c = categoryService.getCategoryById(((Integer) cate).longValue());
			if (c == null) {
			    return Result.error("选择的分类不存在: " + cate);
            }
			blog.setCategory(c);
			blog.setCategoryId(c.getId().intValue());
		} else if (cate instanceof String) {//添加新分类
		    String categoryName = (String) cate;
		    if (StringUtils.isEmpty(categoryName)) {
		        return Result.error("分类名不能为空");
            }
			Category category = categoryService.getCategoryByName(categoryName);
			if (category != null) {
				blog.setCategory(category);
				blog.setCategoryId(category.getId().intValue());
			} else {
			    Category c = new Category();
			    c.setName(categoryName);
			    categoryService.saveCategory(c);
                if (c.getId() == null) {
                    throw new top.wsido.exception.PersistenceException("保存新分类后未能获取ID: " + categoryName);
                }
			    blog.setCategory(c);
			    blog.setCategoryId(c.getId().intValue());
			}
		} else {
		    logger.warn("getResult: cate field contains unexpected type: {}", cate != null ? cate.getClass().getName() : "null");
			return Result.error("分类数据不正确");
		}

		//处理标签
		List<Object> tagListInput = blog.getTagList(); // 从 DTO 获取前端传来的 tagList
		List<Tag> resolvedTags = new ArrayList<>(); // 用于存放处理后的 Tag 对象
		List<Integer> resolvedTagIds = new ArrayList<>(); // 用于存放处理后的 Tag ID

		if (tagListInput != null && !tagListInput.isEmpty()) {
			for (Object t : tagListInput) {
				if (t instanceof Integer) { //选择了已存在的标签
					Tag tag = tagService.getTagById(((Integer) t).longValue());
					if (tag == null) {
						return Result.error("选择的标签ID不存在: " + t);
					}
					resolvedTags.add(tag);
					resolvedTagIds.add(tag.getId().intValue());
				} else if (t instanceof String) { //添加新标签
					String tagName = (String) t;
                    if (StringUtils.isEmpty(tagName)) {
                        return Result.error("标签名不能为空");
                    }
					Tag existingTag = tagService.getTagByName(tagName);
					if (existingTag != null) {
						// 如果希望使用已存在的同名标签
						resolvedTags.add(existingTag);
						resolvedTagIds.add(existingTag.getId().intValue());
					} else {
						Tag newTag = new Tag();
						newTag.setName(tagName);
						tagService.saveTag(newTag); 
						if (newTag.getId() == null) {
							throw new top.wsido.exception.PersistenceException("保存新标签后未能获取ID: " + tagName);
						}
						resolvedTags.add(newTag);
						resolvedTagIds.add(newTag.getId().intValue());
					}
				} else {
				    logger.warn("getResult: tagListInput contains unexpected type: {}", t != null ? t.getClass().getName() : "null");
					return Result.error("标签数据不正确，包含无法处理的类型");
				}
			}
		}
		
		blog.setTags(resolvedTags); 
		blog.setTagIds(resolvedTagIds);

		Date date = new Date();
		if (blog.getReadTime() == null || blog.getReadTime() < 0) {
			if (blog.getWords() != null && blog.getWords() > 0) {
			    blog.setReadTime((int) Math.round(blog.getWords() / 200.0));
			} else {
			    blog.setReadTime(0); // Default read time if words is null or not positive
			}
		}
		if (blog.getViews() == null || blog.getViews() < 0) {
			blog.setViews(0);
		}

		if ("save".equals(type)) {
			blog.setCreateTime(date);
			blog.setUpdateTime(date);
			blogService.saveBlog(blog); 
			return Result.ok("添加成功");
		} else {
			blog.setUpdateTime(date);
			blogService.updateBlog(blog); 
			return Result.ok("更新成功");
		}
	}
}
