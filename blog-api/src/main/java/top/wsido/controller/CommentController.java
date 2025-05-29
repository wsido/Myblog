package top.wsido.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import top.wsido.annotation.AccessLimit;
import top.wsido.constant.JwtConstants;
import top.wsido.entity.User;
import top.wsido.enums.CommentOpenStateEnum;
import top.wsido.model.dto.Comment;
import top.wsido.model.vo.PageComment;
import top.wsido.model.vo.PageResult;
import top.wsido.model.vo.Result;
import top.wsido.service.CommentService;
import top.wsido.service.impl.UserServiceImpl;
import top.wsido.util.JwtUtils;
import top.wsido.util.StringUtils;
import top.wsido.util.comment.CommentUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 评论
 * @Author: wsido
 * @Date: 2020-08-15
 */
@RestController
public class CommentController {
	private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
	@Autowired
	CommentService commentService;
	@Autowired
	UserServiceImpl userService;
	@Autowired
	CommentUtils commentUtils;

	/**
	 * 根据页面分页查询评论列表 (公开) 或 管理员查询全站评论
	 *
	 * @param page     页面分类（0普通文章，1关于我...）, 对 admin scope 可为 null
	 * @param blogId   如果page==0，需要博客id参数, 对 admin scope 可为 null
	 * @param pageNum  页码
	 * @param pageSize 每页个数
	 * @param scope    请求范围，如 "all" 表示管理员请求全站数据
	 * @param jwt      若文章受密码保护，需要获取访问Token (主要用于非admin scope)
	 * @return
	 */
	@GetMapping("/comments")
	public Result comments(@RequestParam(required = false) Integer page,
	                       @RequestParam(required = false) Long blogId,
	                       @RequestParam(defaultValue = "1") Integer pageNum,
	                       @RequestParam(defaultValue = "10") Integer pageSize,
	                       @RequestParam(required = false) String scope,
	                       @RequestHeader(value = "Authorization", defaultValue = "") String jwt) {

		logger.info("[CommentController] Received /comments request: page={}, blogId={}, pageNum={}, pageSize={}, scope={}", page, blogId, pageNum, pageSize, scope);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isAdminAllScopeRequest = false;

		if (authentication != null && authentication.isAuthenticated()) {
			logger.info("[CommentController] Authentication principal: {}, Authorities: {}", authentication.getPrincipal(), authentication.getAuthorities());
			boolean isAdminRole = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
			logger.info("[CommentController] isAdminRole: {}", isAdminRole);
			if ("all".equals(scope) && isAdminRole) {
				isAdminAllScopeRequest = true;
			}
		} else {
			logger.warn("[CommentController] Authentication is null or not authenticated.");
		}
		
		logger.info("[CommentController] isAdminAllScopeRequest: {}", isAdminAllScopeRequest);

		if (isAdminAllScopeRequest) {
			logger.info("[CommentController] Handling as admin all scope request.");
			// 管理员请求全站评论数据
			Integer totalComments = commentService.countByPageAndIsPublished(null, null, null);

			PageHelper.startPage(pageNum, pageSize);
			List<top.wsido.entity.Comment> adminCommentList = commentService.getAdminTopLevelCommentList(null, null, -1L);
			
			// Explicitly use fully qualified name for PageInfo with entity.Comment
			com.github.pagehelper.PageInfo<top.wsido.entity.Comment> adminPageInfo = new com.github.pagehelper.PageInfo<>(adminCommentList);
            adminPageInfo.setTotal(totalComments);

            // Explicitly use fully qualified name for PageResult with entity.Comment
			top.wsido.model.vo.PageResult<top.wsido.entity.Comment> pageResultForAdmin = 
					new top.wsido.model.vo.PageResult<>(adminPageInfo.getPages(), adminPageInfo.getList());
			
			Map<String, Object> map = new HashMap<>(8);
			map.put("allComment", totalComments);
			map.put("closeComment", 0); // Placeholder
			map.put("comments", pageResultForAdmin);
			
			return Result.ok("获取成功 (管理员 全站视图)", map);

		} else {
			logger.info("[CommentController] Handling as non-admin or specific scope request. Page param value: {}", page);
			// Existing logic for public-facing comments (e.g., on a blog post)
			if (page == null) {
				logger.warn("[CommentController] Page parameter is null for non-admin/specific scope request. Returning error.");
				return Result.error("缺少必要的page参数 (非管理员全站请求)");
			}
			CommentOpenStateEnum openState = commentUtils.judgeCommentState(page, blogId);
			switch (openState) {
				case NOT_FOUND:
					return Result.create(404, "该博客不存在");
				case CLOSE:
					return Result.create(403, "评论已关闭");
				case PASSWORD:
					if (JwtUtils.judgeTokenIsExist(jwt)) {
						try {
							String subject = JwtUtils.getTokenBody(jwt).getSubject();
							if (subject.startsWith(JwtConstants.ADMIN_PREFIX)) {
								String username = subject.replace(JwtConstants.ADMIN_PREFIX, "");
								User admin = (User) userService.loadUserByUsername(username);
								if (admin == null) {
									return Result.create(403, "博主身份Token已失效，请重新登录！");
								}
							} else {
								Long tokenBlogId = Long.parseLong(subject);
								if (!tokenBlogId.equals(blogId)) {
									return Result.create(403, "Token不匹配，请重新验证密码！");
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							return Result.create(403, "Token已失效，请重新验证密码！");
						}
					} else {
						return Result.create(403, "此文章受密码保护，请验证密码！");
					}
					break;
				default:
					break;
			}

			Integer allComment = commentService.countByPageAndIsPublished(page, blogId, null);
			Integer openComment = commentService.countByPageAndIsPublished(page, blogId, true);
			PageHelper.startPage(pageNum, pageSize);
			PageInfo<PageComment> pageInfo = new PageInfo<>(commentService.getPageCommentList(page, blogId, -1L));
			PageResult<PageComment> pageResult = new PageResult<>(pageInfo.getPages(), pageInfo.getList());
			Map<String, Object> map = new HashMap<>(8);
			map.put("allComment", allComment);
			map.put("closeComment", allComment - openComment);
			map.put("comments", pageResult);
			return Result.ok("获取成功", map);
		}
	}

	/**
	 * 提交评论 又长又臭 能用就不改了:)
	 * 单个ip，30秒内允许提交1次评论
	 *
	 * @param comment 评论DTO
	 * @param request 获取ip
	 * @param jwt     博主身份Token
	 * @return
	 */
	@AccessLimit(seconds = 30, maxCount = 1, msg = "30秒内只能提交一次评论")
	@PostMapping("/comment")
	public Result postComment(@RequestBody Comment comment,
	                          HttpServletRequest request,
	                          @RequestHeader(value = "Authorization", defaultValue = "") String jwt) {
		//评论内容合法性校验
		if (StringUtils.isEmpty(comment.getContent()) || comment.getContent().length() > 250 ||
				comment.getPage() == null || comment.getParentCommentId() == null) {
			return Result.error("参数有误");
		}
		//是否访客的评论
		boolean isVisitorComment = false;
		//父评论
		top.wsido.entity.Comment parentComment = null;
		//对于有指定父评论的评论，应该以父评论为准，只判断页面可能会被绕过"评论开启状态检测"
		if (comment.getParentCommentId() != -1) {
			//当前评论为子评论
			parentComment = commentService.getCommentById(comment.getParentCommentId());
			Integer page = parentComment.getPage();
			Long blogId = page == 0 ? parentComment.getBlog().getId() : null;
			comment.setPage(page);
			comment.setBlogId(blogId);
		} else {
			//当前评论为顶级评论
			if (comment.getPage() != 0) {
				comment.setBlogId(null);
			}
		}
		//判断是否可评论
		CommentOpenStateEnum openState = commentUtils.judgeCommentState(comment.getPage(), comment.getBlogId());
		switch (openState) {
			case NOT_FOUND:
				return Result.create(404, "该博客不存在");
			case CLOSE:
				return Result.create(403, "评论已关闭");
			case PASSWORD:
				//文章受密码保护
				//验证Token合法性
				if (JwtUtils.judgeTokenIsExist(jwt)) {
					String subject;
					try {
						subject = JwtUtils.getTokenBody(jwt).getSubject();
					} catch (Exception e) {
						e.printStackTrace();
						return Result.create(403, "Token已失效，请重新验证密码！");
					}
					//博主评论，不受密码保护限制，根据博主信息设置评论属性
					if (subject.startsWith(JwtConstants.ADMIN_PREFIX)) {
						//Token验证通过，获取Token中用户名
						String username = subject.replace(JwtConstants.ADMIN_PREFIX, "");
						User admin = (User) userService.loadUserByUsername(username);
						if (admin == null) {
							return Result.create(403, "博主身份Token已失效，请重新登录！");
						}
						commentUtils.setAdminComment(comment, request, admin);
						isVisitorComment = false;
					} else {//普通访客经文章密码验证后携带Token
						//对访客的评论昵称、邮箱合法性校验
						if (StringUtils.isEmpty(comment.getNickname(), comment.getEmail()) || comment.getNickname().length() > 15) {
							return Result.error("参数有误");
						}
						//对于受密码保护的文章，则Token是必须的
						Long tokenBlogId = Long.parseLong(subject);
						//博客id不匹配，验证不通过，可能博客id改变或客户端传递了其它密码保护文章的Token
						if (!tokenBlogId.equals(comment.getBlogId())) {
							return Result.create(403, "Token不匹配，请重新验证密码！");
						}
						commentUtils.setVisitorComment(comment, request);
						isVisitorComment = true;
					}
				} else {//不存在Token则无评论权限
					return Result.create(403, "此文章受密码保护，请验证密码！");
				}
				break;
			case OPEN:
				//评论正常开放
				if (JwtUtils.judgeTokenIsExist(jwt)) {
					String subject;
					try {
						subject = JwtUtils.getTokenBody(jwt).getSubject();
					} catch (Exception e) {
						// 无效的JWT，视为访客或返回错误
						if (StringUtils.isEmpty(comment.getNickname(), comment.getEmail()) || comment.getNickname().length() > 15) {
							return Result.error("参数有误[token_invalid_fallback]");
						}
						commentUtils.setVisitorComment(comment, request);
						isVisitorComment = true;
						break; 
					}

					if (subject.startsWith(JwtConstants.ADMIN_PREFIX)) {
						//博主评论
						String username = subject.replace(JwtConstants.ADMIN_PREFIX, "");
						User admin = (User) userService.loadUserByUsername(username);
						if (admin == null) {
							return Result.create(403, "博主身份Token已失效，请重新登录！");
						}
						commentUtils.setAdminComment(comment, request, admin);
						isVisitorComment = false;
					} else {
						// 尝试作为普通用户Token处理 (subject is username)
						User loggedInUser = null;
						try {
							loggedInUser = (User) userService.loadUserByUsername(subject); 
						} catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) { // Catch specific exception
							// 用户名不存在，可能不是一个有效的用户token
							System.err.println("CommentController: JWT subject '" + subject + "' not found as user, treating as visitor or invalid blog token.");
						}

						if (loggedInUser != null) {
							// 是已登录的普通用户
							commentUtils.setAuthenticatedUserComment(comment, request, loggedInUser);
							isVisitorComment = false;
						} else {
							// 不是管理员，也不是已知的普通用户token，
							// 可能是一个数字型的旧密码文章的token (subject is blogId string)
							// 或者一个无效/伪造的username token.
							// 按访客处理，但需要校验表单字段。
							if (StringUtils.isEmpty(comment.getNickname(), comment.getEmail()) || comment.getNickname().length() > 15) {
								return Result.error("参数有误[user_token_invalid_or_blog_token]");
							}
							commentUtils.setVisitorComment(comment, request);
							isVisitorComment = true;
						}
					}
				} else {
					//无Token，访客评论
					//对访客的评论昵称、邮箱合法性校验
					if (StringUtils.isEmpty(comment.getNickname(), comment.getEmail()) || comment.getNickname().length() > 15) {
						return Result.error("参数有误");
					}
					commentUtils.setVisitorComment(comment, request);
					isVisitorComment = true;
				}
				break;
			default:
				break;
		}
		commentService.saveComment(comment);
		commentUtils.judgeSendNotify(comment, isVisitorComment, parentComment);
		return Result.ok("评论成功");
	}
}