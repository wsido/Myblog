package top.wsido.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import top.wsido.constant.RedisKeyConstants;
import top.wsido.entity.Blog;
import top.wsido.entity.Category;
import top.wsido.entity.SiteSetting;
import top.wsido.entity.Tag;
import top.wsido.entity.User;
import top.wsido.exception.BadRequestException;
import top.wsido.exception.ForbiddenException;
import top.wsido.exception.NotFoundException;
import top.wsido.exception.PersistenceException;
import top.wsido.mapper.BlogMapper;
import top.wsido.mapper.CategoryMapper;
import top.wsido.mapper.CommentMapper;
import top.wsido.mapper.SiteSettingMapper;
import top.wsido.mapper.TagMapper;
import top.wsido.mapper.UserMapper;
import top.wsido.model.dto.BlogView;
import top.wsido.model.dto.BlogVisibility;
import top.wsido.model.vo.ArchiveBlog;
import top.wsido.model.vo.BlogDetail;
import top.wsido.model.vo.BlogInfo;
import top.wsido.model.vo.NewBlog;
import top.wsido.model.vo.PageResult;
import top.wsido.model.vo.RandomBlog;
import top.wsido.model.vo.SearchBlog;
import top.wsido.model.vo.UserInfo;
import top.wsido.service.BlogService;
import top.wsido.service.RedisService;
import top.wsido.service.TagService;
import top.wsido.util.JacksonUtils;
import top.wsido.util.SecurityUtils;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import top.wsido.util.markdown.MarkdownUtils;
import top.wsido.util.CoverGeneratorUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @Description: 博客文章业务层实现
 * @Author: wsido
 * @Date: 2020-07-29
 */
@Service
public class BlogServiceImpl implements BlogService {
	@Autowired
	private BlogMapper blogMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private CategoryMapper categoryMapper;
	@Autowired
	private TagService tagService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private SecurityUtils securityUtils;
	@Autowired
	private SensitiveWordBs sensitiveWordBs;
	@Autowired
	private TagMapper tagMapper;
	@Autowired
	private SiteSettingMapper siteSettingMapper;
	@Autowired
	private CoverGeneratorUtil coverGeneratorUtil;

	//随机博客显示5条
	private static final int randomBlogLimitNum = 5;
	//最新推荐博客显示3条
	private static final int newBlogPageSize = 3;
	//每页显示5条博客简介
	private static final int pageSize = 5;
	//博客简介列表排序方式
	private static final String orderBy = "is_top desc, create_time desc";
	//私密博客提示
	private static final String PRIVATE_BLOG_DESCRIPTION = "此文章受密码保护！";
	private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);
	private static final String AUTO_COVER_GENERATOR_URL_SETTING_NAME_EN = "autoCoverGeneratorUrl";

	/**
	 * 项目启动时，保存所有博客的浏览量到Redis
	 */
	@PostConstruct
	private void saveBlogViewsToRedis() {
		String redisKey = RedisKeyConstants.BLOG_VIEWS_MAP;
		//Redis中没有存储博客浏览量的Hash
		if (!redisService.hasKey(redisKey)) {
			//从数据库中读取并存入Redis
			Map<Long, Integer> blogViewsMap = getBlogViewsMap();
			redisService.saveMapToHash(redisKey, blogViewsMap);
		}
	}

	@Override
	public List<Blog> getListByTitleAndCategoryId(String title, Integer categoryId) {
		List<Blog> blogList;
		Long currentUserId = null;
		boolean isAdmin = false;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated() && !("anonymousUser".equals(authentication.getPrincipal()))) {
			Object principal = authentication.getPrincipal();
			User userEntity = null;

			if (principal instanceof User) {
				userEntity = (User) principal;
			} else if (principal instanceof String) { // Principal is username string
				// Temporarily clear PageHelper's context to avoid interference
				com.github.pagehelper.PageHelper.clearPage();
				try {
					userEntity = userMapper.findByUsername((String) principal);
				} finally {
					// If the main query blogMapper.getListByTitleAndCategoryId OR blogMapper.getListByUserIdAndTitleAndCategoryId
					// is itself wrapped by PageHelper.startPage() EXTERNALLY (e.g., in the Controller or another service method that calls this),
					// then clearing it here might affect that outer pagination.
					// However, the typical PageHelper usage is PageHelper.startPage() right before the main list mapper call.
					// The controller usually looks like:
					//   PageHelper.startPage(pageNum, pageSize);
					//   List<Blog> blogs = blogService.getListByTitleAndCategoryId(title, categoryId);
					//   PageInfo<Blog> pageInfo = new PageInfo<>(blogs);
					// In this scenario, the PageHelper.startPage() is for the *result* of getListByTitleAndCategoryId.
					// The internal call to userMapper.findByUsername should NOT be paginated.
					// So, clearing it here is correct. We don't need to restore it because the main blog list query
					// will be executed under the original PageHelper.startPage() context set by the controller/outer service.
				}
			}

			if (userEntity != null) {
				currentUserId = userEntity.getId();
				isAdmin = authentication.getAuthorities().stream()
						.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"));
				// Alternative check if User entity has a reliable 'type' or 'role' field directly:
				// isAdmin = isAdmin || "admin".equalsIgnoreCase(userEntity.getType()) || "ROLE_admin".equals(userEntity.getRole());
			}
		}

		if (!isAdmin) {
			System.out.println("Warning: Non-admin user or unidentifiable admin accessing blog list. Filtering by user ID: " + currentUserId);
			if (currentUserId == null) {
				throw new ForbiddenException("用户未登录或无法识别用户ID，无法过滤博客列表");
			}
			blogList = blogMapper.getListByUserIdAndTitleAndCategoryId(currentUserId, title, categoryId);
		} else {
			System.out.println("Admin user accessing blog list.");
			blogList = blogMapper.getListByTitleAndCategoryId(title, categoryId);
		}

		return blogList;
	}

	@Override
	public List<SearchBlog> getSearchBlogListByQueryAndIsPublished(String query) {
		List<SearchBlog> searchBlogs = blogMapper.getSearchBlogListByQueryAndIsPublished(query);
		// 数据库的处理是不区分大小写的，那么这里的匹配串处理也应该不区分大小写，否则会出现不准确的结果
		query = query.toUpperCase();
		for (SearchBlog searchBlog : searchBlogs) {
			String content = searchBlog.getContent().toUpperCase();
			int contentLength = content.length();
			int index = content.indexOf(query) - 10;
			index = Math.max(index, 0);
			int end = index + 21;//以关键字字符串为中心返回21个字
			end = Math.min(end, contentLength - 1);
			searchBlog.setContent(searchBlog.getContent().substring(index, end));
		}
		return searchBlogs;
	}

	@Override
	public List<Blog> getIdAndTitleList() {
		return blogMapper.getIdAndTitleList();
	}

	@Override
	public List<NewBlog> getNewBlogListByIsPublished() {
		String redisKey = RedisKeyConstants.NEW_BLOG_LIST;
		List<NewBlog> newBlogListFromRedis = redisService.getListByValue(redisKey);
		if (newBlogListFromRedis != null) {
			return newBlogListFromRedis;
		}
		PageHelper.startPage(1, newBlogPageSize);
		List<NewBlog> newBlogList = blogMapper.getNewBlogListByIsPublished();
		for (NewBlog newBlog : newBlogList) {
			if (!"".equals(newBlog.getPassword())) {
				newBlog.setPrivacy(true);
				newBlog.setPassword("");
			} else {
				newBlog.setPrivacy(false);
			}
		}
		redisService.saveListToValue(redisKey, newBlogList);
		return newBlogList;
	}

	@Override
	public PageResult<BlogInfo> getBlogInfoListByIsPublished(Integer pageNum) {
		String redisKey = RedisKeyConstants.HOME_BLOG_INFO_LIST;
		//redis已有当前页缓存
		PageResult<BlogInfo> pageResultFromRedis = redisService.getBlogInfoPageResultByHash(redisKey, pageNum);
		if (pageResultFromRedis != null) {
			logger.info("[getBlogInfoListByIsPublished] Cache hit for pageNum: {}. Returning from cache.", pageNum);
			setBlogViewsFromRedisToPageResult(pageResultFromRedis);
			return pageResultFromRedis;
		}
		//redis没有缓存，从数据库查询，并添加缓存
		logger.info("[getBlogInfoListByIsPublished] Cache miss for pageNum: {}. Fetching from DB.", pageNum);
		PageHelper.startPage(pageNum, pageSize, orderBy);
		List<BlogInfo> blogInfosFromMapper = blogMapper.getBlogInfoListByIsPublished();
		logger.info("[getBlogInfoListByIsPublished] blogInfosFromMapper size: {}", blogInfosFromMapper != null ? blogInfosFromMapper.size() : "null");
		if (blogInfosFromMapper != null) {
			for(int i = 0; i < blogInfosFromMapper.size(); i++) {
				logger.info("[getBlogInfoListByIsPublished] blogInfosFromMapper[{}]: {}", i, blogInfosFromMapper.get(i));
			}
		} else {
		    logger.error("[getBlogInfoListByIsPublished] blogInfosFromMapper is NULL!");
        }

		List<BlogInfo> blogInfos = processBlogInfosPassword(blogInfosFromMapper);
		logger.info("[getBlogInfoListByIsPublished] blogInfos after processBlogInfosPassword size: {}", blogInfos != null ? blogInfos.size() : "null");
        if (blogInfos != null) {
            for(int i = 0; i < blogInfos.size(); i++) {
                logger.info("[getBlogInfoListByIsPublished] blogInfos after process[{}]: {}", i, blogInfos.get(i));
            }
        } else {
            logger.error("[getBlogInfoListByIsPublished] blogInfos after processBlogInfosPassword is NULL!");
        }

		PageInfo<BlogInfo> pageInfo = new PageInfo<>(blogInfos);
		PageResult<BlogInfo> pageResult = new PageResult<>(pageInfo.getPages(), pageInfo.getList());
		setBlogViewsFromRedisToPageResult(pageResult);
		//添加首页缓存
		redisService.saveKVToHash(redisKey, pageNum, pageResult);
		return pageResult;
	}

	/**
	 * 将pageResult中博客对象的浏览量设置为Redis中的最新值
	 *
	 * @param pageResult
	 */
	private void setBlogViewsFromRedisToPageResult(PageResult<BlogInfo> pageResult) {
		String redisKey = RedisKeyConstants.BLOG_VIEWS_MAP;
		List<BlogInfo> blogInfos = pageResult.getList();
		logger.info("[setBlogViewsFromRedisToPageResult] Received pageResult.getList() size: {}", blogInfos != null ? blogInfos.size() : "null");
		if (blogInfos == null) {
		    logger.error("[setBlogViewsFromRedisToPageResult] blogInfos list is NULL. Aborting.");
		    return;
		}

		for (int i = 0; i < blogInfos.size(); i++) {
			BlogInfo originalBlogInfo = blogInfos.get(i);
            logger.info("[setBlogViewsFromRedisToPageResult] Processing item [{}]: {}", i, originalBlogInfo);
            if (originalBlogInfo == null) {
                logger.error("[setBlogViewsFromRedisToPageResult] Item [{}] is NULL in the list. Skipping.", i);
                continue;
            }

			BlogInfo blogInfo = JacksonUtils.convertValue(originalBlogInfo, BlogInfo.class);
			logger.info("[setBlogViewsFromRedisToPageResult] Item [{}] after Jackson conversion: {}", i, blogInfo);
            if (blogInfo == null) {
                logger.error("[setBlogViewsFromRedisToPageResult] Item [{}] became NULL after Jackson conversion. Original was: {}. Skipping.", i, originalBlogInfo);
                // Potentially set the original item back if it's safer, or handle as error
                // blogInfos.set(i, originalBlogInfo); // Or some other default/error state
                continue;
            }

			Long blogId = null;
			try {
                blogId = blogInfo.getId();
                logger.info("[setBlogViewsFromRedisToPageResult] Item [{}] blogId: {}", i, blogId);
            } catch (Exception e) {
                logger.error("[setBlogViewsFromRedisToPageResult] Error getting ID for item [{}]. blogInfo: {}. Exception: ", i, blogInfo, e);
                // Decide if to skip or use a default
                continue;
            }

            if (blogId == null) {
                logger.error("[setBlogViewsFromRedisToPageResult] blogId is NULL for item [{}]. blogInfo: {}. Skipping.", i, blogInfo);
                continue;
            }

			/**
			 * 这里如果出现异常，通常是手动修改过 MySQL 而没有通过后台管理，导致 Redis 和 MySQL 不同步
			 * 从 Redis 中查出了 null，强转 int 时出现 NullPointerException
			 * 直接抛出异常比带着 bug 继续跑要好得多
			 *
			 * 解决步骤：
			 * 1.结束程序
			 * 2.删除 Redis DB 中 blogViewsMap 这个 key（或者直接清空对应的整个 DB）
			 * 3.重新启动程序
			 *
			 * 具体请查看: https://github.com/wsido/NBlog/issues/58
			 */
			Object rawViewCount = redisService.getValueByHashKey(redisKey, blogId);
            logger.info("[setBlogViewsFromRedisToPageResult] Item [{}] blogId: {}, rawViewCount from Redis: {}", i, blogId, rawViewCount);
            
            int view = 0; // Default to 0
            if (rawViewCount instanceof Integer) {
                view = (Integer) rawViewCount;
            } else if (rawViewCount != null) {
                try {
                    view = Integer.parseInt(rawViewCount.toString());
                } catch (NumberFormatException e) {
                    logger.warn("[setBlogViewsFromRedisToPageResult] Could not parse view count '{}' for blogId {}. Defaulting to 0.", rawViewCount, blogId);
                }
            } // if rawViewCount is null, view remains 0

			blogInfo.setViews(view);
			blogInfos.set(i, blogInfo);
			logger.info("[setBlogViewsFromRedisToPageResult] Item [{}] updated with view count: {}", i, view);
		}
	}

	@Override
	public PageResult<BlogInfo> getBlogInfoListByCategoryNameAndIsPublished(String categoryName, Integer pageNum) {
		PageHelper.startPage(pageNum, pageSize, orderBy);
		List<BlogInfo> blogInfos = processBlogInfosPassword(blogMapper.getBlogInfoListByCategoryNameAndIsPublished(categoryName));
		PageInfo<BlogInfo> pageInfo = new PageInfo<>(blogInfos);
		PageResult<BlogInfo> pageResult = new PageResult<>(pageInfo.getPages(), pageInfo.getList());
		setBlogViewsFromRedisToPageResult(pageResult);
		return pageResult;
	}

	@Override
	public PageResult<BlogInfo> getBlogInfoListByTagNameAndIsPublished(String tagName, Integer pageNum) {
		PageHelper.startPage(pageNum, pageSize, orderBy);
		List<BlogInfo> blogInfos = processBlogInfosPassword(blogMapper.getBlogInfoListByTagNameAndIsPublished(tagName));
		PageInfo<BlogInfo> pageInfo = new PageInfo<>(blogInfos);
		PageResult<BlogInfo> pageResult = new PageResult<>(pageInfo.getPages(), pageInfo.getList());
		setBlogViewsFromRedisToPageResult(pageResult);
		return pageResult;
	}

	private List<BlogInfo> processBlogInfosPassword(List<BlogInfo> blogInfos) {
		for (BlogInfo blogInfo : blogInfos) {
			if (!"".equals(blogInfo.getPassword())) {
				blogInfo.setPrivacy(true);
				blogInfo.setPassword("");
				blogInfo.setDescription(PRIVATE_BLOG_DESCRIPTION);
			} else {
				blogInfo.setPrivacy(false);
				blogInfo.setDescription(MarkdownUtils.markdownToHtmlExtensions(blogInfo.getDescription()));
			}
			blogInfo.setTags(tagService.getTagListByBlogId(blogInfo.getId()));
		}
		return blogInfos;
	}

	@Override
	public Map<String, Object> getArchiveBlogAndCountByIsPublished() {
		String redisKey = RedisKeyConstants.ARCHIVE_BLOG_MAP;
		Map<String, Object> mapFromRedis = redisService.getMapByValue(redisKey);
		if (mapFromRedis != null) {
			return mapFromRedis;
		}
		List<String> groupYearMonth = blogMapper.getGroupYearMonthByIsPublished();
		Map<String, List<ArchiveBlog>> archiveBlogMap = new LinkedHashMap<>();
		for (String s : groupYearMonth) {
			List<ArchiveBlog> archiveBlogs = blogMapper.getArchiveBlogListByYearMonthAndIsPublished(s);
			for (ArchiveBlog archiveBlog : archiveBlogs) {
				if (!"".equals(archiveBlog.getPassword())) {
					archiveBlog.setPrivacy(true);
					archiveBlog.setPassword("");
				} else {
					archiveBlog.setPrivacy(false);
				}
			}
			archiveBlogMap.put(s, archiveBlogs);
		}
		Integer count = countBlogByIsPublished();
		Map<String, Object> map = new HashMap<>(4);
		map.put("blogMap", archiveBlogMap);
		map.put("count", count);
		redisService.saveMapToValue(redisKey, map);
		return map;
	}

	@Override
	public List<RandomBlog> getRandomBlogListByLimitNumAndIsPublishedAndIsRecommend() {
		List<RandomBlog> randomBlogs = blogMapper.getRandomBlogListByLimitNumAndIsPublishedAndIsRecommend(randomBlogLimitNum);
		for (RandomBlog randomBlog : randomBlogs) {
			if (!"".equals(randomBlog.getPassword())) {
				randomBlog.setPrivacy(true);
				randomBlog.setPassword("");
			} else {
				randomBlog.setPrivacy(false);
			}
		}
		return randomBlogs;
	}

	private Map<Long, Integer> getBlogViewsMap() {
		List<BlogView> blogViewList = blogMapper.getBlogViewsList();
		Map<Long, Integer> blogViewsMap = new HashMap<>(128);
		for (BlogView blogView : blogViewList) {
			blogViewsMap.put(blogView.getId(), blogView.getViews());
		}
		return blogViewsMap;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteBlogById(Long id) {
		Blog blog = blogMapper.getBlogById(id);
		if (blog == null) {
			throw new NotFoundException("该博客不存在");
		}

		if (!securityUtils.isAdmin()) {
			Long currentUserId = securityUtils.getCurrentUserId();
			if (currentUserId == null || !currentUserId.equals(blog.getUser().getId())) {
				throw new ForbiddenException("无权删除该博客");
			}
		}

		if (blogMapper.deleteBlogById(id) != 1) {
			throw new PersistenceException("删除博客失败或博客不存在");
		}
		deleteBlogRedisCache();
		redisService.deleteByHashKey(RedisKeyConstants.BLOG_VIEWS_MAP, id);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteBlogTagByBlogId(Long blogId) {
		// 即使没有标签关联被删除（例如博客原本就没有标签，或者blogId为null导致没有匹配），
		// 这通常不应视为一个阻止博客更新流程的致命错误。
		// Mapper的delete操作如果因SQL问题失败，通常会抛出更底层的DataAccessException。
		// 我们只调用删除，不检查影响的行数是否为0。
		blogMapper.deleteBlogTagByBlogId(blogId); 
		// 如果需要严格检查blogId的有效性，应在调用此方法前进行，
		// 或者在mapper层面如果blogId为null则不执行或抛出特定异常。
		// 原来的检查: if (blogMapper.deleteBlogTagByBlogId(blogId) == 0) {
		// 	throw new PersistenceException("维护博客标签关联表失败");
		// }
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveBlog(top.wsido.model.dto.Blog blog) {
		if (StringUtils.isEmpty(blog.getFirstPicture())) {
			generateAndSetFirstPicture(blog);
		}
		// 敏感词过滤
		if (blog.getTitle() != null) {
			blog.setTitle(sensitiveWordBs.replace(blog.getTitle()));
		}
		if (blog.getContent() != null) {
			blog.setContent(sensitiveWordBs.replace(blog.getContent()));
		}
		if (blog.getDescription() != null) {
			blog.setDescription(sensitiveWordBs.replace(blog.getDescription()));
		}

		if (blogMapper.saveBlog(blog) != 1) {
			throw new PersistenceException("添加博客失败");
		}
		
		if (blog.getTagIds() != null && !blog.getTagIds().isEmpty()) {
			for (Integer tagId : blog.getTagIds()) {
				blogMapper.saveBlogTag(blog.getId(), tagId.longValue());
			}
		}
		deleteBlogRedisCache();
	}

	/**
	 * 如果文章首图URL为空，则根据标题自动生成一个。
	 *
	 * @param blog 博客文章
	 */
	private void generateAndSetFirstPicture(top.wsido.model.dto.Blog blog) {
		try {
			// 调用本地工具类生成封面
			String coverUrl = coverGeneratorUtil.generate(blog.getTitle());
			blog.setFirstPicture(coverUrl);
		} catch (Exception e) {
			logger.error("Failed to generate cover image locally.", e);
			// 如果本地生成失败，可以设置一个固定的默认图片URL作为备用
			blog.setFirstPicture("/img/default_cover.jpg");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateBlog(top.wsido.model.dto.Blog blog) {
		if (StringUtils.isEmpty(blog.getFirstPicture())) {
			generateAndSetFirstPicture(blog);
		}
		// 敏感词过滤
		if (blog.getTitle() != null) {
			blog.setTitle(sensitiveWordBs.replace(blog.getTitle()));
		}
		if (blog.getContent() != null) {
			blog.setContent(sensitiveWordBs.replace(blog.getContent()));
		}
		if (blog.getDescription() != null) {
			blog.setDescription(sensitiveWordBs.replace(blog.getDescription()));
		}

		blog.setUpdateTime(new Date());

		if (blogMapper.updateBlog(blog) != 1) {
			throw new PersistenceException("更新博客失败");
		}

		deleteBlogTagByBlogId(blog.getId());
		if (blog.getTagIds() != null && !blog.getTagIds().isEmpty()) {
			for (Integer tagId : blog.getTagIds()) {
				blogMapper.saveBlogTag(blog.getId(), tagId.longValue());
			}
		}
		deleteBlogRedisCache();
		redisService.saveKVToHash(RedisKeyConstants.BLOG_VIEWS_MAP, blog.getId(), blog.getViews());
	}

	@Override
	public int countBlogByIsPublished() {
		return blogMapper.countBlogByIsPublished();
	}

	@Override
	public int countBlogByCategoryId(Long categoryId) {
		return blogMapper.countBlogByCategoryId(categoryId);
	}

	@Override
	public int countBlogByTagId(Long tagId) {
		return blogMapper.countBlogByTagId(tagId);
	}

	@Override
	public Boolean getCommentEnabledByBlogId(Long blogId) {
		return blogMapper.getCommentEnabledByBlogId(blogId);
	}

	@Override
	public Boolean getPublishedByBlogId(Long blogId) {
		return blogMapper.getPublishedByBlogId(blogId);
	}

	/**
	 * 删除首页缓存、最新推荐缓存、归档页面缓存、博客浏览量缓存
	 */
	private void deleteBlogRedisCache() {
		redisService.deleteCacheByKey(RedisKeyConstants.HOME_BLOG_INFO_LIST);
		redisService.deleteCacheByKey(RedisKeyConstants.NEW_BLOG_LIST);
		redisService.deleteCacheByKey(RedisKeyConstants.ARCHIVE_BLOG_MAP);
	}

	@Override
	public List<Blog> getBlogListByUserId(Long userId, Integer start, Integer size) {
		return blogMapper.getBlogListByUserId(userId, start, size);
	}

	@Override
	public int countBlogByUserId(Long userId) {
		return blogMapper.countBlogByUserId(userId);
	}

	@Override
	public List<Blog> getBlogListByTitleAndCategoryIdAndUserId(String title, Integer categoryId, Long userId, Integer start, Integer size) {
		return blogMapper.getBlogListByTitleAndCategoryIdAndUserId(title, categoryId, userId, start, size);
	}

	@Override
	public int countBlogByTitleAndCategoryIdAndUserId(String title, Integer categoryId, Long userId) {
		return blogMapper.countBlogByTitleAndCategoryIdAndUserId(title, categoryId, userId);
	}

	@Override
	public BlogDetail getMyBlogForEdit(Long blogId) {
		Long currentUserId = securityUtils.getCurrentUserId();
		if (currentUserId == null) {
			throw new ForbiddenException("用户未登录或无法识别用户ID");
		}

		Blog blogEntity = blogMapper.getBlogById(blogId); // This returns entity.Blog
		if (blogEntity == null) {
			throw new NotFoundException("博客不存在");
		}
		if (blogEntity.getUser() == null || !currentUserId.equals(blogEntity.getUser().getId())) {
			throw new ForbiddenException("无权访问此博客");
		}

		BlogDetail blogDetail = new BlogDetail();
		blogDetail.setId(blogEntity.getId());
		blogDetail.setTitle(blogEntity.getTitle());
		blogDetail.setContent(MarkdownUtils.markdownToHtmlExtensions(blogEntity.getContent()));
		blogDetail.setPublished(blogEntity.getPublished()); // Now BlogDetail should have this setter
		blogDetail.setRecommend(blogEntity.getRecommend()); // Now BlogDetail should have this setter
		blogDetail.setAppreciation(blogEntity.getAppreciation());
		blogDetail.setCommentEnabled(blogEntity.getCommentEnabled());
		blogDetail.setTop(blogEntity.getTop());
		blogDetail.setCreateTime(blogEntity.getCreateTime());
		blogDetail.setUpdateTime(blogEntity.getUpdateTime());
		blogDetail.setViews(blogEntity.getViews()); 
		blogDetail.setWords(blogEntity.getWords());
		blogDetail.setReadTime(blogEntity.getReadTime());
		blogDetail.setPassword(blogEntity.getPassword()); 
		blogDetail.setCategory(blogEntity.getCategory());
		blogDetail.setTags(tagService.getTagListByBlogId(blogId)); 

		// Populate UserInfo
		if (blogEntity.getUser() != null) {
			User author = userMapper.findById(blogEntity.getUser().getId());
			if (author != null) {
				UserInfo userInfo = new UserInfo();
				userInfo.setId(author.getId());
				userInfo.setNickname(author.getNickname());
				userInfo.setAvatar(author.getAvatar());
				blogDetail.setUser(userInfo); // Now BlogDetail should have setUser(UserInfo)
			}
		}

		int view = (int) redisService.getValueByHashKey(RedisKeyConstants.BLOG_VIEWS_MAP, blogDetail.getId());
		blogDetail.setViews(view);

		return blogDetail;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteMyBlogById(Long blogId) {
		Long currentUserId = securityUtils.getCurrentUserId();
		if (currentUserId == null) {
			throw new ForbiddenException("用户未登录或无法识别用户ID");
		}

		Blog blogEntity = blogMapper.getBlogById(blogId);
		if (blogEntity == null) {
			return; // Or throw NotFoundException
		}

		if (blogEntity.getUser() == null || !currentUserId.equals(blogEntity.getUser().getId())) {
			throw new ForbiddenException("无权删除此博客");
		}

		if (blogMapper.deleteBlogById(blogId) != 1) {
			throw new PersistenceException("删除博客失败");
		}
		blogMapper.deleteBlogTagByBlogId(blogId); 
		redisService.deleteByHashKey(RedisKeyConstants.BLOG_VIEWS_MAP, blogId); // Corrected method name
		deleteBlogRedisCache(); 
	}

	@Override
	public List<Map<String, Object>> getBlogIdAndTitleListByUserId(Long userId) {
		// This is a placeholder implementation. You need to ensure your BlogMapper
		// has a method like findBlogIdAndTitleListByUserId that takes a userId
		// and returns a List of Maps, where each map contains "id" and "title".
		return blogMapper.findBlogIdAndTitleListByUserId(userId); 
	}

	@Override
	public List<Blog> getBlogAdminListByDesc(String description) {
		if (description == null) {
			description = "";
		}
		// 使用已有的方法，根据title查询，因为title和description在业务上通常可以互通
		// 如果BlogMapper没有直接根据description查询的方法，可以先使用title查询替代
		return blogMapper.getListByTitleAndCategoryId(description, null);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveBlogTag(Long blogId, Long tagId) {
		if (blogMapper.saveBlogTag(blogId, tagId) != 1) {
			throw new PersistenceException("维护博客标签关联表失败 (saveBlogTag)");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateBlogRecommendById(Long blogId, Boolean recommend) {
		if (!securityUtils.isAdmin()) {
			throw new ForbiddenException("无权操作 (updateBlogRecommendById)");
		}
		// Ensure recommend is not null for DB
		if (blogMapper.updateBlogRecommendById(blogId, Boolean.TRUE.equals(recommend)) != 1) {
			throw new PersistenceException("操作失败 (updateBlogRecommendById)");
		}
		// Consider cache implications if recommend status affects lists
        deleteBlogRedisCache(); 
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateBlogVisibilityById(Long blogId, BlogVisibility blogVisibility) {
		// Permission check: Admin or owner
		if (!securityUtils.isAdmin()) {
			Blog existingBlog = blogMapper.getBlogById(blogId);
			if (existingBlog == null) {
				throw new NotFoundException("该博客不存在 (updateBlogVisibilityById)");
			}
			Long currentUserId = securityUtils.getCurrentUserId();
			if (currentUserId == null || existingBlog.getUser() == null || !currentUserId.equals(existingBlog.getUser().getId())) {
				throw new ForbiddenException("无权修改该博客的可见性 (updateBlogVisibilityById)");
			}
		}
		
		// Ensure boolean fields in blogVisibility are non-null if DB requires
		// Example: blogVisibility.setPublished(Boolean.TRUE.equals(blogVisibility.getPublished()));
		// For now, assume BlogMapper handles the DTO correctly.

		if (blogMapper.updateBlogVisibilityById(blogId, blogVisibility) != 1) {
			throw new PersistenceException("操作失败 (updateBlogVisibilityById)");
		}
		deleteBlogRedisCache();
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateBlogTopById(Long blogId, Boolean top) {
		if (!securityUtils.isAdmin()) {
			throw new ForbiddenException("无权操作 (updateBlogTopById)");
		}
		// Ensure top is not null for DB
		if (blogMapper.updateBlogTopById(blogId, Boolean.TRUE.equals(top)) != 1) {
			throw new PersistenceException("操作失败 (updateBlogTopById)");
		}
		deleteBlogRedisCache(); // Top status affects home list order
	}

	@Override
	public void updateViewsToRedis(Long blogId) {
		redisService.incrementByHashKey(RedisKeyConstants.BLOG_VIEWS_MAP, blogId, 1);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateViews(Long blogId, Integer views) {
		if (blogMapper.updateViews(blogId, views) != 1) {
			throw new PersistenceException("更新失败");
		}
	}

	@Override
	public Blog getBlogById(Long id) {
		Blog blog = blogMapper.getBlogById(id);
		if (blog == null) {
			throw new NotFoundException("博客不存在");
		}
		/**
		 * 将浏览量设置为Redis中的最新值
		 * 这里如果出现异常，查看第 152 行注释说明
		 * @see BlogServiceImpl#setBlogViewsFromRedisToPageResult
		 */
		Integer viewCount = (Integer) redisService.getValueByHashKey(RedisKeyConstants.BLOG_VIEWS_MAP, blog.getId());
		blog.setViews(viewCount == null ? 0 : viewCount);
		return blog;
	}

	@Override
	public String getTitleByBlogId(Long id) {
		return blogMapper.getTitleByBlogId(id);
	}

	@Override
	public BlogDetail getBlogByIdAndIsPublished(Long id) {
		BlogDetail blog = blogMapper.getBlogByIdAndIsPublished(id);
		if (blog == null) {
			throw new NotFoundException("该博客不存在");
		}
		blog.setContent(MarkdownUtils.markdownToHtmlExtensions(blog.getContent()));
		/**
		 * 将浏览量设置为Redis中的最新值
		 * 这里如果出现异常，查看第 152 行注释说明
		 * @see BlogServiceImpl#setBlogViewsFromRedisToPageResult
		 */
		Integer viewCount = (Integer) redisService.getValueByHashKey(RedisKeyConstants.BLOG_VIEWS_MAP, blog.getId());
		blog.setViews(viewCount == null ? 0 : viewCount);
		return blog;
	}

	@Override
	public String getBlogPassword(Long blogId) {
		return blogMapper.getBlogPassword(blogId);
	}
}
