package top.wsido.controller.admin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.wsido.annotation.OperationLogger;
import top.wsido.model.vo.FriendInfo;
import top.wsido.model.vo.Result;
import top.wsido.service.FriendService;

import java.util.List;

/**
 * @Description: 友链页面后台管理
 * @Author: wsido
 * @Date: 2020-09-08
 */
@RestController
@RequestMapping("/admin")
public class FriendAdminController {
	@Autowired
	private FriendService friendService;

	/**
	 * 分页获取友链列表
	 *
	 * @param pageNum  页码
	 * @param pageSize 每页条数
	 * @return
	 */
	@GetMapping("/friends")
	public Result getFriendList(@RequestParam(defaultValue = "1") Integer pageNum,
	                            @RequestParam(defaultValue = "10") Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		List<top.wsido.entity.Friend> friendList = friendService.getFriendList();
		PageInfo<top.wsido.entity.Friend> pageInfo = new PageInfo<>(friendList);
		return Result.ok("获取成功", pageInfo);
	}

	/**
	 * 更新友链公开状态
	 *
	 * @param id        友链id
	 * @param published 是否公开
	 * @return
	 */
	@OperationLogger("更新友链公开状态")
	@PutMapping("/friend/published")
	public Result updatePublished(@RequestParam Long id, @RequestParam Boolean published) {
		friendService.updateFriendPublishedById(id, published);
		return Result.ok("更新成功");
	}

	/**
	 * 添加友链
	 *
	 * @param friendDto 友链DTO
	 * @return
	 */
	@OperationLogger("保存友链")
	@PostMapping("/friend")
	public Result saveFriend(@RequestBody top.wsido.model.dto.Friend friendDto) {
		top.wsido.entity.Friend friendEntity = new top.wsido.entity.Friend();
		friendEntity.setNickname(friendDto.getNickname());
		friendEntity.setDescription(friendDto.getDescription());
		friendEntity.setWebsite(friendDto.getWebsite());
		friendEntity.setAvatar(friendDto.getAvatar());
		friendEntity.setPublished(friendDto.getPublished());
		friendService.saveFriend(friendEntity);
		return Result.ok("保存成功");
	}

	/**
	 * 更新友链
	 *
	 * @param friendDto 友链DTO
	 * @return
	 */
	@OperationLogger("更新友链")
	@PutMapping("/friend")
	public Result updateFriend(@RequestBody top.wsido.model.dto.Friend friendDto) {
		if (friendDto.getId() == null) {
			return Result.error("友链ID不能为空");
		}
		friendService.updateFriend(friendDto);
		return Result.ok("更新成功");
	}

	/**
	 * 按id删除友链
	 *
	 * @param id
	 * @return
	 */
	@OperationLogger("删除友链")
	@DeleteMapping("/friend")
	public Result deleteFriend(@RequestParam Long id) {
		friendService.deleteFriend(id);
		return Result.ok("删除成功");
	}

	/**
	 * 获取友链页面信息
	 *
	 * @return
	 */
	@GetMapping("/friendInfo")
	public Result getFriendInfoDetail() {
		FriendInfo friendInfo = friendService.getFriendInfo(false, false);
		return Result.ok("获取成功", friendInfo);
	}

	/**
	 * 修改友链页面评论开放状态
	 *
	 * @param commentEnabled 是否开放评论
	 * @return
	 */
	@OperationLogger("更新友链页面评论状态")
	@PutMapping("/friendInfo/commentEnabled")
	public Result updateFriendCommentEnabled(@RequestParam Boolean commentEnabled) {
		friendService.updateFriendInfoCommentEnabled(commentEnabled);
		return Result.ok("更新成功");
	}

	/**
	 * 修改友链页面content
	 *
	 * @param friendInfo 包含content的FriendInfo对象
	 * @return
	 */
	@OperationLogger("更新友链页面内容")
	@PutMapping("/friendInfo/content")
	public Result updateFriendContent(@RequestBody FriendInfo friendInfo) {
		friendService.updateFriendInfoContent(friendInfo.getContent());
		return Result.ok("更新成功");
	}
}
