package top.wsido.util.comment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import top.wsido.config.properties.BlogProperties;
import top.wsido.constant.PageConstants;
import top.wsido.constant.RedisKeyConstants;
import top.wsido.entity.User;
import top.wsido.enums.CommentOpenStateEnum;
import top.wsido.enums.CommentPageEnum;
import top.wsido.model.dto.Comment;
import top.wsido.model.vo.FriendInfo;
import top.wsido.service.AboutService;
import top.wsido.service.BlogService;
import top.wsido.service.FriendService;
import top.wsido.service.RedisService;
import top.wsido.service.UserService;
import top.wsido.util.HashUtils;
import top.wsido.util.IpAddressUtils;
import top.wsido.util.MailUtils;
import top.wsido.util.QQInfoUtils;
import top.wsido.util.StringUtils;
import top.wsido.util.comment.channel.ChannelFactory;
import top.wsido.util.comment.channel.CommentNotifyChannel;

/**
 * 评论工具类
 *
 * @author: wsido
 * @date: 2022-01-22
 */
@Component
@DependsOn("springContextUtils")
public class CommentUtils {
	@Autowired
	private BlogProperties blogProperties;
	@Autowired
	private MailUtils mailUtils;
	@Autowired
	private AboutService aboutService;
	@Autowired
	private FriendService friendService;
	@Autowired
	private UserService userService;
	@Autowired
	private RedisService redisService;

	private static BlogService blogService;

	private CommentNotifyChannel notifyChannel;
	/**
	 * 新评论是否默认公开
	 */
	private Boolean commentDefaultOpen;

	@Autowired
	public void setBlogService(BlogService blogService) {
		CommentUtils.blogService = blogService;
	}

	@Value("${comment.notify.channel}")
	public void setNotifyChannel(String channelName) {
		this.notifyChannel = ChannelFactory.getChannel(channelName);
	}

	@Value("${comment.default-open}")
	public void setCommentDefaultOpen(Boolean commentDefaultOpen) {
		this.commentDefaultOpen = commentDefaultOpen;
	}

	/**
	 * 判断是否发送提醒
	 * 6种情况：
	 * 1.我以父评论提交：不用提醒
	 * 2.我回复我自己：不用提醒
	 * 3.我回复访客的评论：只提醒该访客
	 * 4.访客以父评论提交：只提醒我自己
	 * 5.访客回复我的评论：只提醒我自己
	 * 6.访客回复访客的评论(即使是他自己先前的评论)：提醒我自己和他回复的评论
	 *
	 * @param comment          当前收到的评论
	 * @param isVisitorComment 是否访客评论
	 * @param parentComment    父评论
	 */
	public void judgeSendNotify(Comment comment, boolean isVisitorComment, top.wsido.entity.Comment parentComment) {
		if (parentComment != null && !parentComment.getAdminComment() && parentComment.getNotice()) {
			//我回复访客的评论，且对方接收提醒，邮件提醒对方(3)
			//访客回复访客的评论(即使是他自己先前的评论)，且对方接收提醒，邮件提醒对方(6)
			sendMailToParentComment(parentComment, comment);
		}
		if (isVisitorComment) {
			//访客以父评论提交，只提醒我自己(4)
			//访客回复我的评论，提醒我自己(5)
			//访客回复访客的评论，不管对方是否接收提醒，都要提醒我有新评论(6)
			notifyMyself(comment);
		}
	}

	/**
	 * 发送邮件提醒回复对象
	 *
	 * @param parentComment 父评论
	 * @param comment       当前收到的评论
	 */
	private void sendMailToParentComment(top.wsido.entity.Comment parentComment, Comment comment) {
		CommentPageEnum commentPageEnum = getCommentPageEnum(comment);
		Map<String, Object> map = new HashMap<>(16);
		map.put("parentNickname", parentComment.getNickname());
		map.put("nickname", comment.getNickname());
		map.put("title", commentPageEnum.getTitle());
		map.put("time", comment.getCreateTime());
		map.put("parentContent", parentComment.getContent());
		map.put("content", comment.getContent());
		map.put("url", blogProperties.getView() + commentPageEnum.getPath());
		String toAccount = parentComment.getEmail();
		String subject = "您在 " + blogProperties.getName() + " 的评论有了新回复";
		mailUtils.sendHtmlTemplateMail(map, toAccount, subject, "guest.html");
	}

	/**
	 * 通过指定方式通知自己
	 *
	 * @param comment 当前收到的评论
	 */
	private void notifyMyself(Comment comment) {
		notifyChannel.notifyMyself(comment);
	}

	/**
	 * 获取评论对应的页面
	 *
	 * @param comment 当前收到的评论
	 * @return CommentPageEnum
	 */
	public static CommentPageEnum getCommentPageEnum(Comment comment) {
		CommentPageEnum commentPageEnum = CommentPageEnum.UNKNOWN;
		switch (comment.getPage()) {
			case 0:
				//普通博客
				commentPageEnum = CommentPageEnum.BLOG;
				commentPageEnum.setTitle(blogService.getTitleByBlogId(comment.getBlogId()));
				commentPageEnum.setPath("/blog/" + comment.getBlogId());
				break;
			case 1:
				//关于我页面
				commentPageEnum = CommentPageEnum.ABOUT;
				break;
			case 2:
				//友链页面
				commentPageEnum = CommentPageEnum.FRIEND;
				break;
			default:
				break;
		}
		return commentPageEnum;
	}

	/**
	 * 查询对应页面评论是否开启
	 *
	 * @param page   页面分类（0普通文章，1关于我，2友链）
	 * @param blogId 如果page==0，需要博客id参数，校验文章是否公开状态
	 * @return CommentOpenStateEnum
	 */
	public CommentOpenStateEnum judgeCommentState(Integer page, Long blogId) {
		switch (page) {
			case PageConstants.BLOG:
				//普通博客
				Boolean commentEnabled = blogService.getCommentEnabledByBlogId(blogId);
				Boolean published = blogService.getPublishedByBlogId(blogId);
				if (commentEnabled == null || published == null) {
					//未查询到此博客
					return CommentOpenStateEnum.NOT_FOUND;
				} else if (!published) {
					//博客未公开
					return CommentOpenStateEnum.NOT_FOUND;
				} else if (!commentEnabled) {
					//博客评论已关闭
					return CommentOpenStateEnum.CLOSE;
				}
				//判断文章是否存在密码
				String password = blogService.getBlogPassword(blogId);
				if (!StringUtils.isEmpty(password)) {
					return CommentOpenStateEnum.PASSWORD;
				}
				break;
			case PageConstants.ABOUT:
				//关于我页面
				if (!aboutService.getAboutCommentEnabled()) {
					//页面评论已关闭
					return CommentOpenStateEnum.CLOSE;
				}
				break;
			case PageConstants.FRIEND:
				//友链页面
				FriendInfo friendInfo = friendService.getFriendInfo(true, false);
				if (!friendInfo.getCommentEnabled()) {
					//页面评论已关闭
					return CommentOpenStateEnum.CLOSE;
				}
				break;
			default:
				break;
		}
		return CommentOpenStateEnum.OPEN;
	}

	/**
	 * 对于昵称不是QQ号的评论，根据昵称Hash设置头像
	 *
	 * @param comment 当前收到的评论
	 */
	private void setCommentRandomAvatar(Comment comment) {
		//设置随机头像
		//根据评论昵称取Hash，保证每一个昵称对应一个头像
		long nicknameHash = HashUtils.getMurmurHash32(comment.getNickname());
		//计算对应的头像
		long num = nicknameHash % 6 + 1;
		String avatar = "/img/comment-avatar/" + num + ".jpg";
		comment.setAvatar(avatar);
	}

	/**
	 * 通用博主评论属性
	 *
	 * @param comment 评论DTO
	 * @param admin   博主信息
	 */
	private void setGeneralAdminComment(Comment comment, User admin) {
		comment.setAdminComment(true);
		comment.setCreateTime(new Date());
		comment.setPublished(true);
		comment.setAvatar(admin.getAvatar());
		comment.setNickname(admin.getNickname());
		comment.setEmail(admin.getEmail());
		comment.setNotice(false);
	}

	/**
	 * 设置博主评论属性
	 *
	 * @param comment 当前收到的评论
	 * @param request 用于获取ip
	 * @param admin   博主信息
	 */
	public void setAdminComment(Comment comment, HttpServletRequest request, User admin) {
		setGeneralAdminComment(comment, admin);
		comment.setIp(IpAddressUtils.getIpAddress(request));
	}

	/**
	 * 设置认证用户评论属性
	 *
	 * @param comment 评论DTO
	 * @param request HttpServletRequest
	 * @param user    认证用户信息
	 */
	public void setAuthenticatedUserComment(Comment comment, HttpServletRequest request, User user) {
		comment.setUserId(user.getId());
		comment.setAdminComment(false); // 明确不是管理员评论
		comment.setCreateTime(new Date());
		comment.setPublished(commentDefaultOpen); // 根据配置决定是否默认公开
		comment.setIp(IpAddressUtils.getIpAddress(request));

		// 优先使用用户账户中的信息
		comment.setAvatar(user.getAvatar());
		comment.setNickname(user.getNickname());
		comment.setEmail(user.getEmail());
		// comment.getNotice() 来自表单，保留用户选择
	}

	/**
	 * 设置访客评论属性
	 *
	 * @param comment 当前收到的评论
	 * @param request 用于获取ip
	 */
	public void setVisitorComment(Comment comment, HttpServletRequest request) {
		comment.setNickname(comment.getNickname().trim());
		setCommentRandomAvatar(comment);

		comment.setAdminComment(false);
		comment.setCreateTime(new Date());
		comment.setPublished(commentDefaultOpen);
		comment.setEmail(comment.getEmail().trim());
		comment.setIp(IpAddressUtils.getIpAddress(request));
	}

	/**
	 * 设置QQ头像，复用已上传过的QQ头像，不再重复上传
	 *
	 * @param comment 当前收到的评论
	 * @param qq      QQ号
	 * @throws Exception 上传QQ头像时可能抛出的异常
	 */
	private void setCommentQQAvatar(Comment comment, String qq) throws Exception {
		String uploadAvatarUrl = (String) redisService.getValueByHashKey(RedisKeyConstants.QQ_AVATAR_URL_MAP, qq);
		if (StringUtils.isEmpty(uploadAvatarUrl)) {
			uploadAvatarUrl = QQInfoUtils.getQQAvatarUrl(qq);
			redisService.saveKVToHash(RedisKeyConstants.QQ_AVATAR_URL_MAP, qq, uploadAvatarUrl);
		}
		comment.setAvatar(uploadAvatarUrl);
	}
}
