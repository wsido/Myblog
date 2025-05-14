package top.wsido.service;

import top.wsido.entity.User;
import top.wsido.model.dto.PasswordDTO;
import top.wsido.model.vo.Result;

/**
 * @Description: 用户服务接口
 * @Author: wsido
 */
public interface UserService {
	User findUserByUsernameAndPassword(String username, String password);

	User findUserById(Long id);

	boolean changeAccount(User user, String jwt);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User findUserByUsername(String username);
    
    /**
     * 注册用户
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @param email 邮箱
     * @return 注册结果
     */
    Result register(String username, String password, String nickname, String email);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    Result updateUserInfo(User user);
    
    /**
     * 修改密码
     * @param passwordDTO 密码数据传输对象
     * @return 修改结果
     */
    Result updatePassword(PasswordDTO passwordDTO);

    /**
     * 获取当前登录用户信息
     * @return 用户信息及结果
     */
    Result getUserInfo();
}
