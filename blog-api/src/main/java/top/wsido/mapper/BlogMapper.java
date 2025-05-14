package top.wsido.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import top.wsido.entity.Blog;
import top.wsido.model.dto.BlogView;
import top.wsido.model.dto.BlogVisibility;
import top.wsido.model.vo.ArchiveBlog;
import top.wsido.model.vo.BlogDetail;
import top.wsido.model.vo.BlogInfo;
import top.wsido.model.vo.CategoryBlogCount;
import top.wsido.model.vo.NewBlog;
import top.wsido.model.vo.RandomBlog;
import top.wsido.model.vo.SearchBlog;

/**
 * @Description: 博客文章持久层接口
 * @Author: wsido
 * @Date: 2020-07-26
 */
@Mapper
@Repository
public interface BlogMapper {
	List<Blog> getListByTitleAndCategoryId(String title, Integer categoryId);

	List<SearchBlog> getSearchBlogListByQueryAndIsPublished(String query);

	List<Blog> getIdAndTitleList();

	List<NewBlog> getNewBlogListByIsPublished();

	List<BlogInfo> getBlogInfoListByIsPublished();

	List<BlogInfo> getBlogInfoListByCategoryNameAndIsPublished(String categoryName);

	List<BlogInfo> getBlogInfoListByTagNameAndIsPublished(String tagName);

	List<String> getGroupYearMonthByIsPublished();

	List<ArchiveBlog> getArchiveBlogListByYearMonthAndIsPublished(String yearMonth);

	List<RandomBlog> getRandomBlogListByLimitNumAndIsPublishedAndIsRecommend(Integer limitNum);

	List<BlogView> getBlogViewsList();

	int deleteBlogById(Long id);

	int deleteBlogTagByBlogId(Long blogId);

	int saveBlog(top.wsido.model.dto.Blog blog);

	int saveBlogTag(Long blogId, Long tagId);

	int updateBlogRecommendById(Long blogId, Boolean recommend);

	int updateBlogVisibilityById(Long blogId, BlogVisibility bv);

	int updateBlogTopById(Long blogId, Boolean top);

	int updateViews(Long blogId, Integer views);

	Blog getBlogById(Long id);

	String getTitleByBlogId(Long id);

	BlogDetail getBlogByIdAndIsPublished(Long id);

	String getBlogPassword(Long blogId);

	int updateBlog(top.wsido.model.dto.Blog blog);

	int countBlog();

	int countBlogByIsPublished();

	int countBlogByCategoryId(Long categoryId);

	int countBlogByTagId(Long tagId);

	Boolean getCommentEnabledByBlogId(Long blogId);

	Boolean getPublishedByBlogId(Long blogId);

	List<CategoryBlogCount> getCategoryBlogCountList();
	
	// 根据用户ID获取博客列表
	List<Blog> getBlogListByUserId(@Param("userId") Long userId, @Param("start") Integer start, @Param("size") Integer size);
	
	// 根据用户ID统计博客数量
	int countBlogByUserId(Long userId);

	/**
	 * 根据用户ID统计其所有博客的总浏览量
	 * @param userId 用户ID
	 * @return 总浏览量，如果用户没有博客或浏览量为0，可能返回null或0，需SQL实现配合
	 */
	Integer sumViewsByUserId(@Param("userId") Long userId);
}
