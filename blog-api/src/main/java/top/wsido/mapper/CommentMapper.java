package top.wsido.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import top.wsido.entity.Comment;
import top.wsido.model.vo.PageComment;

/**
 * @Description: 博客评论持久层接口
 * @Author: wsido
 * @Date: 2020-08-03
 */
@Mapper
@Repository
public interface CommentMapper {
	List<Comment> getListByPageAndParentCommentId(Integer page, Long blogId, Long parentCommentId);

	List<Comment> getListByParentCommentId(@Param("parentCommentId") Long parentCommentId);

	List<PageComment> getPageCommentListByPageAndParentCommentId(@Param("page") Integer page, @Param("blogId") Long blogId, @Param("parentCommentId") Long parentCommentId);

	Comment getCommentById(@Param("id") Long id);

	int updateCommentPublishedById(Long commentId, Boolean published);

	int updateCommentNoticeById(Long commentId, Boolean notice);

	int deleteCommentById(Long commentId);

	int deleteCommentsByBlogId(Long blogId);

	int updateComment(Comment comment);

	int countByPageAndIsPublished(Integer page, Long blogId, Boolean isPublished);

	int countComment();

	int saveComment(top.wsido.model.dto.Comment comment);

	/**
	 * 根据用户ID统计其所有博客收到的公开评论总数
	 * @param userId 博客作者的用户ID
	 * @return 公开评论总数
	 */
	Integer countCommentsByUserId(@Param("userId") Long userId);
	
	/**
	 * 获取用户发表的所有评论
	 * @param nickname 评论者昵称
	 * @return 评论列表
	 */
	List<Comment> getListByNickname(@Param("nickname") String nickname);

	/**
	 * 获取用户在特定页面或博客下发表的评论
	 * @param nickname 评论者昵称
	 * @param page 页面类型 (0博客, 1关于我, 2友人帐) - null表示不按页面筛选
	 * @param blogId 博客ID (如果page=0) - null表示不按博客ID筛选
	 * @return 评论列表
	 */
	List<Comment> getListByNicknameAndPageAndBlogId(@Param("nickname") String nickname, @Param("page") Integer page, @Param("blogId") Long blogId);

	List<Comment> getAdminTopLevelComments(@Param("page") Integer page, @Param("blogId") Long blogId, @Param("parentCommentId") Long parentCommentId);
}
