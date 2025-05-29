package top.wsido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.wsido.constant.JwtConstants;
import top.wsido.entity.User;
import top.wsido.model.dto.LoginInfo;
import top.wsido.model.vo.Result;
import top.wsido.service.UserService;
import top.wsido.util.JwtUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 前台登录
 * @Author: wsido
 * @Date: 2020-09-02
 */
@RestController
public class LoginController {
	@Autowired
	UserService userService;

	/**
	 * 登录成功后，签发博主身份Token (此通用 /login 端点已废弃，
	 * 登录应通过 /admin/login 或 /user/login 由 JwtLoginFilter 处理)
	 *
	 * @param loginInfo
	 * @return
	 */
	/* Commenting out this problematic general /login endpoint
	@PostMapping("/login")
	public Result login(@RequestBody LoginInfo loginInfo) {
		User user = userService.findUserByUsernameAndPassword(loginInfo.getUsername(), loginInfo.getPassword());
		if (!"ROLE_admin".equals(user.getRole())) {
			return Result.create(403, "无权限");
		}
		user.setPassword(null);
		String jwt = JwtUtils.generateToken(JwtConstants.ADMIN_PREFIX + user.getUsername());
		Map<String, Object> map = new HashMap<>(4);
		map.put("user", user);
		map.put("token", jwt);
		return Result.ok("登录成功", map);
	}
	*/
}
