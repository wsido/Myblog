package top.wsido.service;

import java.util.List;
import java.util.Map;

import top.wsido.entity.Blog;
import top.wsido.model.dto.BlogVisibility;
import top.wsido.model.vo.BlogDetail;
import top.wsido.model.vo.BlogInfo;
import top.wsido.model.vo.NewBlog;
import top.wsido.model.vo.PageResult;
import top.wsido.model.vo.RandomBlog;
import top.wsido.model.vo.SearchBlog;

public interface BlogService {
	List<Blog> getListByTitleAndCategoryId(String title, Integer categoryId);

	List<SearchBlog> getSearchBlogListByQueryAndIsPublished(String query);

	List<Blog> getIdAndTitleList();

	List<NewBlog> getNewBlogListByIsPublished();

	PageResult<BlogInfo> getBlogInfoListByIsPublished(Integer pageNum);

	PageResult<BlogInfo> getBlogInfoListByCategoryNameAndIsPublished(String categoryName, Integer pageNum);

	PageResult<BlogInfo> getBlogInfoListByTagNameAndIsPublished(String tagName, Integer pageNum);

	Map<String, Object> getArchiveBlogAndCountByIsPublished();

	List<RandomBlog> getRandomBlogListByLimitNumAndIsPublishedAndIsRecommend();

	void deleteBlogById(Long id);

	void deleteBlogTagByBlogId(Long blogId);

	void saveBlog(top.wsido.model.dto.Blog blog);

	void saveBlogTag(Long blogId, Long tagId);

	void updateBlogRecommendById(Long blogId, Boolean recommend);

	void updateBlogVisibilityById(Long blogId, BlogVisibility blogVisibility);

	void updateBlogTopById(Long blogId, Boolean top);

	void updateViewsToRedis(Long blogId);

	void updateViews(Long blogId, Integer views);

	Blog getBlogById(Long id);

	String getTitleByBlogId(Long id);

	BlogDetail getBlogByIdAndIsPublished(Long id);

	String getBlogPassword(Long blogId);

	void updateBlog(top.wsido.model.dto.Blog blog);

	int countBlogByIsPublished();

	int countBlogByCategoryId(Long categoryId);

	int countBlogByTagId(Long tagId);

	Boolean getCommentEnabledByBlogId(Long blogId);

	Boolean getPublishedByBlogId(Long blogId);
	
	/**
	 * 根据用户ID获取博客列表
	 *
	 * @param userId 用户ID
	 * @param start 起始索引
	 * @param size 每页数量
	 * @return 博客列表
	 */
	List<Blog> getBlogListByUserId(Long userId, Integer start, Integer size);
	
	/**
	 * 根据用户ID统计博客数量
	 *
	 * @param userId 用户ID
	 * @return 博客数量
	 */
	int countBlogByUserId(Long userId);
	
	/**
	 * 根据标题和分类ID以及用户ID过滤博客列表
	 *
	 * @param title 标题关键字
	 * @param categoryId 分类ID
	 * @param userId 用户ID
	 * @param start 起始索引
	 * @param size 每页数量
	 * @return 博客列表
	 */
	List<Blog> getBlogListByTitleAndCategoryIdAndUserId(String title, Integer categoryId, Long userId, Integer start, Integer size);
	
	/**
	 * 根据标题和分类ID以及用户ID统计博客数量
	 *
	 * @param title 标题关键字
	 * @param categoryId 分类ID
	 * @param userId 用户ID
	 * @return 博客数量
	 */
	int countBlogByTitleAndCategoryIdAndUserId(String title, Integer categoryId, Long userId);

	/**
	 * 获取当前登录用户自己的指定博客详情 (供编辑页使用)
	 *
	 * @param blogId 博客ID
	 * @return 博客详情VO，若无权或不存在则抛出异常
	 */
	BlogDetail getMyBlogForEdit(Long blogId);

	/**
	 * 当前登录用户删除自己的指定博客
	 *
	 * @param blogId 博客ID
	 */
	void deleteMyBlogById(Long blogId);

	List<Map<String, Object>> getBlogIdAndTitleListByUserId(Long userId);
	
	/**
	 * 根据描述获取博客管理列表
	 * 
	 * @param description 描述关键字
	 * @return 博客列表
	 */
	List<Blog> getBlogAdminListByDesc(String description);
}
