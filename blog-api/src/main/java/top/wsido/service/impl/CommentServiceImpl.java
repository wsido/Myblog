package top.wsido.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wsido.entity.Comment;
import top.wsido.exception.PersistenceException;
import top.wsido.mapper.CommentMapper;
import top.wsido.model.vo.PageComment;
import top.wsido.service.CommentService;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @Description: 博客评论业务层实现
 * @Author: wsido
 * @Date: 2020-08-03
 */
@Service
public class CommentServiceImpl implements CommentService {
	@Autowired
	CommentMapper commentMapper;
	@Autowired
	SensitiveWordBs sensitiveWordBs;

	@Override
	public List<Comment> getListByPageAndParentCommentId(Integer page, Long blogId, Long parentCommentId) {
		List<Comment> comments = commentMapper.getListByPageAndParentCommentId(page, blogId, parentCommentId);
		for (Comment c : comments) {
			//递归查询子评论及其子评论
			List<Comment> replyComments = getListByPageAndParentCommentId(page, blogId, c.getId());
			c.setReplyComments(replyComments);
		}
		return comments;
	}

	@Override
	public List<PageComment> getPageCommentList(Integer page, Long blogId, Long parentCommentId) {
		List<PageComment> comments = getPageCommentListByPageAndParentCommentId(page, blogId, parentCommentId);
		for (PageComment c : comments) {
			List<PageComment> tmpComments = new ArrayList<>();
			getReplyComments(tmpComments, c.getReplyComments());
			//对于两列评论来说，按时间顺序排列应该比树形更合理些
			//排序一下
			Comparator<PageComment> comparator = Comparator.comparing(PageComment::getCreateTime);
			tmpComments.sort(comparator);

			c.setReplyComments(tmpComments);
		}
		return comments;
	}

	@Override
	public Comment getCommentById(Long id) {
		Comment comment = commentMapper.getCommentById(id);
		if (comment == null) {
			throw new PersistenceException("评论不存在");
		}
		return comment;
	}

	@Override
	public List<Comment> getListByNickname(String nickname) {
		return commentMapper.getListByNickname(nickname);
	}

	@Override
	public List<Comment> getListByNicknameAndPageAndBlogId(String nickname, Integer page, Long blogId) {
		// 这里直接调用mapper层对应的方法，Mapper层将负责处理page和blogId为null的情况
		// Mapper的XML中可以使用<if test="page != null">等条件来动态构建SQL
		List<Comment> comments = commentMapper.getListByNicknameAndPageAndBlogId(nickname, page, blogId);
		// 子评论的递归获取逻辑可以保持，或者根据实际需求调整（例如，如果按博客筛选，子评论也应属于同一博客）
		// 为简化，此处暂时不对子评论获取逻辑做复杂修改，假设Mapper返回的已经是顶层评论
		// 如果需要递归加载符合条件的子评论，这里的逻辑需要像getListByPageAndParentCommentId一样处理
		for (Comment c : comments) {
			// 考虑到UserCommentController只获取当前用户的评论，这里递归获取子评论时，
			// 仍然使用原有的getListByPageAndParentCommentId，因为它不按用户筛选，而是按父评论ID和页面筛选
			// 这意味着子评论可能不是当前用户发表的，但属于同一页面和父评论下
			// 如果业务要求"我的评论"下的子评论也必须是"我的"，则此处逻辑需要调整
			List<Comment> replyComments = getListByPageAndParentCommentId(c.getPage(), c.getBlog() != null ? c.getBlog().getId() : null, c.getId());
			c.setReplyComments(replyComments);
		}
		return comments;
	}

	/**
	 * 将所有子评论递归取出到一个List中
	 *
	 * @param comments
	 */
	private void getReplyComments(List<PageComment> tmpComments, List<PageComment> comments) {
		for (PageComment c : comments) {
			tmpComments.add(c);
			getReplyComments(tmpComments, c.getReplyComments());
		}
	}

	private List<PageComment> getPageCommentListByPageAndParentCommentId(Integer page, Long blogId, Long parentCommentId) {
		List<PageComment> comments = commentMapper.getPageCommentListByPageAndParentCommentId(page, blogId, parentCommentId);
		for (PageComment c : comments) {
			List<PageComment> replyComments = getPageCommentListByPageAndParentCommentId(page, blogId, c.getId());
			c.setReplyComments(replyComments);
		}
		return comments;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateCommentPublishedById(Long commentId, Boolean published) {
		//如果是隐藏评论，则所有子评论都要修改成隐藏状态
		if (!published) {
			List<Comment> comments = getAllReplyComments(commentId);
			for (Comment c : comments) {
				hideComment(c);
			}
		}

		if (commentMapper.updateCommentPublishedById(commentId, published) != 1) {
			throw new PersistenceException("操作失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateCommentNoticeById(Long commentId, Boolean notice) {
		if (commentMapper.updateCommentNoticeById(commentId, notice) != 1) {
			throw new PersistenceException("操作失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteCommentById(Long commentId) {
		List<Comment> comments = getAllReplyComments(commentId);
		for (Comment c : comments) {
			delete(c);
		}
		if (commentMapper.deleteCommentById(commentId) != 1) {
			throw new PersistenceException("评论删除失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteCommentsByBlogId(Long blogId) {
		commentMapper.deleteCommentsByBlogId(blogId);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateComment(Comment comment) {
		// 敏感词过滤
		if (comment.getContent() != null) {
			comment.setContent(sensitiveWordBs.replace(comment.getContent()));
		}

		if (commentMapper.updateComment(comment) != 1) {
			throw new PersistenceException("评论修改失败");
		}
	}

	@Override
	public int countByPageAndIsPublished(Integer page, Long blogId, Boolean isPublished) {
		return commentMapper.countByPageAndIsPublished(page, blogId, isPublished);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveComment(top.wsido.model.dto.Comment comment) {
		// 敏感词过滤
		if (comment.getContent() != null) {
			comment.setContent(sensitiveWordBs.replace(comment.getContent()));
		}

		if (commentMapper.saveComment(comment) != 1) {
			throw new PersistenceException("评论失败");
		}
	}

	/**
	 * 递归删除子评论
	 *
	 * @param comment 需要删除子评论的父评论
	 */
	private void delete(Comment comment) {
		for (Comment c : comment.getReplyComments()) {
			delete(c);
		}
		if (commentMapper.deleteCommentById(comment.getId()) != 1) {
			throw new PersistenceException("评论删除失败");
		}
	}

	/**
	 * 递归隐藏子评论
	 *
	 * @param comment 需要隐藏子评论的父评论
	 */
	private void hideComment(Comment comment) {
		for (Comment c : comment.getReplyComments()) {
			hideComment(c);
		}
		if (commentMapper.updateCommentPublishedById(comment.getId(), false) != 1) {
			throw new PersistenceException("操作失败");
		}
	}

	/**
	 * 按id递归查询子评论
	 *
	 * @param parentCommentId 需要查询子评论的父评论id
	 * @return
	 */
	private List<Comment> getAllReplyComments(Long parentCommentId) {
		List<Comment> comments = commentMapper.getListByParentCommentId(parentCommentId);
		for (Comment c : comments) {
			List<Comment> replyComments = getAllReplyComments(c.getId());
			c.setReplyComments(replyComments);
		}
		return comments;
	}

	@Override
	public List<Comment> getAdminTopLevelCommentList(Integer page, Long blogId, Long parentCommentId) {
		List<Comment> comments = commentMapper.getAdminTopLevelComments(page, blogId, parentCommentId);
		for (Comment c : comments) {
			// Recursively fetch replies. For admin view, we want full Comment objects for replies too.
			// We can use the existing getListByPageAndParentCommentId as it fetches full Comment objects
			// and its recursive nature is suitable here. We pass the page/blogId of the parent if available,
			// or null if the parent itself was fetched with null page/blogId (global admin view).
			List<Comment> replyComments = getListByPageAndParentCommentId(c.getPage(), (c.getBlog() != null ? c.getBlog().getId() : null), c.getId());
			c.setReplyComments(replyComments);
		}
		return comments;
	}
}
