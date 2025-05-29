package top.wsido.service;

import top.wsido.entity.Comment;
import top.wsido.model.vo.PageComment;

import java.util.List;

public interface CommentService {
	List<Comment> getListByPageAndParentCommentId(Integer page, Long blogId, Long parentCommentId);

	List<PageComment> getPageCommentList(Integer page, Long blogId, Long parentCommentId);

	List<Comment> getAdminTopLevelCommentList(Integer page, Long blogId, Long parentCommentId);

	Comment getCommentById(Long id);

	void updateCommentPublishedById(Long commentId, Boolean published);

	void updateCommentNoticeById(Long commentId, Boolean notice);

	void deleteCommentById(Long commentId);

	void deleteCommentsByBlogId(Long blogId);

	void updateComment(Comment comment);

	int countByPageAndIsPublished(Integer page, Long blogId, Boolean isPublished);

	void saveComment(top.wsido.model.dto.Comment comment);
	
	/**
	 * 根据昵称查询评论
	 * 
	 * @param nickname 评论者昵称
	 * @return 评论列表
	 */
	List<Comment> getListByNickname(String nickname);

	/**
	 * 根据昵称、页面类型和博客ID查询评论
	 * 
	 * @param nickname 评论者昵称
	 * @param page 页面类型 (0博客, 1关于我, 2友人帐) - null表示不按页面筛选
	 * @param blogId 博客ID (如果page=0) - null表示不按博客ID筛选
	 * @return 评论列表
	 */
	List<Comment> getListByNicknameAndPageAndBlogId(String nickname, Integer page, Long blogId);
}
