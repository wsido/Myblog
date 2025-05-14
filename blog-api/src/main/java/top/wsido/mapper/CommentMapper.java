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

	List<Comment> getListByParentCommentId(Long parentCommentId);

	List<PageComment> getPageCommentListByPageAndParentCommentId(Integer page, Long blogId, Long parentCommentId);

	Comment getCommentById(Long id);

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
}
