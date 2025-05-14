package top.wsido.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wsido.entity.User;
import top.wsido.model.vo.Result;
import top.wsido.service.UserService;

/**
 * @Description: 账号后台管理
 * @Author: wsido
 * @Date: 2023-01-31
 */
@RestController
@RequestMapping("/admin")
public class AccountAdminController {
	@Autowired
	UserService userService;

	/**
	 * 账号密码修改
	 */
	@PostMapping("/account")
	public Result account(@RequestBody User user, @RequestHeader(value = "Authorization", defaultValue = "") String jwt) {
		boolean res = userService.changeAccount(user, jwt);
		return res ? Result.ok("修改成功") : Result.error("修改失败");
	}
}
