package top.wsido.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import top.wsido.entity.User;
import top.wsido.exception.NotFoundException;
import top.wsido.exception.PersistenceException;
import top.wsido.mapper.UserMapper;
import top.wsido.model.dto.PasswordDTO;
import top.wsido.model.vo.Result;
import top.wsido.service.UserService;
import top.wsido.util.JwtUtils;

/**
 * @Description: 用户业务层接口实现类
 * @Author: wsido
 * @Date: 2020-07-19
 */
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
	@Autowired
	private UserMapper userMapper;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userMapper.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("用户不存在");
		}
		return user;
	}

	@Override
	public User findUserByUsernameAndPassword(String username, String password) {
		User user = userMapper.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("用户不存在");
		}
		if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
			throw new UsernameNotFoundException("密码错误");
		}
		return user;
	}

	@Override
	public User findUserById(Long id) {
		User user = userMapper.findById(id);
		if (user == null) {
			throw new NotFoundException("用户不存在");
		}
		return user;
	}

	@Override
	public boolean changeAccount(User user, String jwt) {
		String username = JwtUtils.getTokenBody(jwt).getSubject();
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		if (userMapper.updateUserByUsername(username, user) != 1) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
		return true;
	}
	
	@Override
	public User findUserByUsername(String username) {
		return userMapper.findByUsername(username);
	}
	
	@Override
	public Result register(String username, String password, String nickname, String email) {
		// 检查用户名是否已存在
		if (userMapper.findByUsername(username) != null) {
			return Result.error("用户名已存在");
		}
		
		// 创建新用户
		User user = new User();
		user.setUsername(username);
		// 使用注入的bCryptPasswordEncoder进行密码加密
		user.setPassword(bCryptPasswordEncoder.encode(password));
		user.setNickname(nickname);
		user.setEmail(email);
		user.setAvatar("/img/avatar.jpg"); // 默认头像
		user.setCreateTime(new Date());
		user.setUpdateTime(new Date());
		user.setRole("ROLE_user"); // 普通用户角色
		user.setType("user");
		
		try {
			if (userMapper.insertUser(user) != 1) { // 通常insert返回影响行数
                throw new PersistenceException("注册用户失败，数据库未插入记录");
            }
			return Result.ok("注册成功");
		} catch (Exception e) {
			// 更具体的异常处理或日志记录
			throw new PersistenceException("注册失败：" + e.getMessage());
		}
	}
	
	@Override
	public Result updateUserInfo(User user) {
		// 获取当前登录用户，确保操作的是自己的信息或有权限
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		User currentUser = userMapper.findByUsername(currentUsername);

		if (currentUser == null) {
			return Result.error("当前登录用户不存在");
		}
		// 确保更新的是自己的信息, 通过ID来判断传入的user是否是当前用户
		// 如果user参数中的ID与当前登录用户的ID不一致，则拒绝 (除非是管理员有特殊权限)
		if (!currentUser.getId().equals(user.getId())) {
			 // 这里的逻辑取决于：是只允许用户修改自己的信息，还是说user参数的ID必须是当前用户的ID。
			 // 如果user的ID是不可信的，应该使用currentUser.getId()
			 // 如果允许传入的user对象指定要修改的用户ID（例如管理员操作），则需要权限检查。
			 // 简单起见，这里我们假设用户只能修改自己的信息，并且传入的user对象的ID是当前用户的ID。
			 // 如果user对象中没有ID，或者ID与当前用户不符，应如何处理？
			 // 为了安全，更新时应该基于当前登录用户的ID。
		}

		User updateUser = new User();
		updateUser.setId(currentUser.getId()); // 使用当前登录用户的ID
		
		// 只允许更新部分字段
		if (user.getNickname() != null) {
            updateUser.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            updateUser.setEmail(user.getEmail());
        }
        if (user.getAvatar() != null) {
            updateUser.setAvatar(user.getAvatar());
        }
		updateUser.setUpdateTime(new Date());
		
		try {
			if (userMapper.updateById(updateUser) != 1) {
                throw new PersistenceException("用户信息更新失败，数据库未更新记录");
            }
			return Result.ok("更新成功");
		} catch (Exception e) {
			throw new PersistenceException("更新失败：" + e.getMessage());
		}
	}
	
	// 修改后的 updatePassword 方法
	@Override
	public Result updatePassword(PasswordDTO passwordDTO) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		User user = userMapper.findByUsername(username);
		
		if (user == null) {
			return Result.error("用户不存在或未登录");
		}
		
		// 验证旧密码
		if (!bCryptPasswordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
			return Result.error("旧密码错误");
		}
		
		// 更新密码
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setPassword(bCryptPasswordEncoder.encode(passwordDTO.getNewPassword()));
		updateUser.setUpdateTime(new Date());
		
		try {
			if (userMapper.updateById(updateUser) != 1) { // 假设updateById只更新非null字段，或者有专门的updatePassword方法
                throw new PersistenceException("密码修改失败，数据库未更新记录");
            }
			return Result.ok("密码修改成功");
		} catch (Exception e) {
			throw new PersistenceException("密码修改失败：" + e.getMessage());
		}
	}

	// 新增的 getUserInfo 方法
	@Override
	public Result getUserInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// ---------- DEBUGGING START ----------
		if (authentication != null) {
			System.out.println("UserServiceImpl.getUserInfo(): START DEBUG");
			System.out.println("UserServiceImpl.getUserInfo(): authentication.getName() = " + authentication.getName());
			System.out.println("UserServiceImpl.getUserInfo(): authentication.getAuthorities() = " + authentication.getAuthorities());
			System.out.println("UserServiceImpl.getUserInfo(): authentication.getPrincipal() = " + authentication.getPrincipal().toString());
			System.out.println("UserServiceImpl.getUserInfo(): END DEBUG");
		} else {
			System.out.println("UserServiceImpl.getUserInfo(): authentication is NULL");
		}
		// ---------- DEBUGGING END ----------

		if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return Result.error("用户未登录");
        }
		String username = authentication.getName();
		User user = userMapper.findByUsername(username);
		
		if (user == null) {
			return Result.error("用户不存在");
		}
		
		user.setPassword(null); // 出于安全考虑，不返回密码
		return Result.ok("获取成功", user);
	}
}
