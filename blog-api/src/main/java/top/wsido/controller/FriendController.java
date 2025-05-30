package top.wsido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wsido.annotation.VisitLogger;
import top.wsido.enums.VisitBehavior;
import top.wsido.model.vo.Friend;
import top.wsido.model.vo.FriendInfo;
import top.wsido.model.vo.Result;
import top.wsido.service.FriendService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 友链
 * @Author: wsido
 * @Date: 2020-09-08
 */
@RestController
public class FriendController {
	@Autowired
	FriendService friendService;

	/**
	 * 获取友链页面
	 *
	 * @return
	 */
	@VisitLogger(value = VisitBehavior.FRIEND)
	@GetMapping("/friends")
	public Result friends() {
		List<Friend> friendList = friendService.getFriendVOList();
		FriendInfo friendInfo = friendService.getFriendInfo(true, true);
		Map<String, Object> map = new HashMap<>(4);
		map.put("friendList", friendList);
		map.put("friendInfo", friendInfo);
		return Result.ok("获取成功", map);
	}

	/**
	 * 按昵称增加友链浏览次数
	 *
	 * @param nickname 友链昵称
	 * @return
	 */
	@VisitLogger(value = VisitBehavior.CLICK_FRIEND)
	@PostMapping("/friend")
	public Result addViews(@RequestParam String nickname) {
		friendService.updateViewsByNickname(nickname);
		return Result.ok("请求成功");
	}
} 