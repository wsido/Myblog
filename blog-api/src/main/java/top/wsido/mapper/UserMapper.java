package top.wsido.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import top.wsido.entity.User;

/**
 * @Description: 用户持久层接口
 * @Author: wsido
 * @Date: 2020-07-19
 */
@Mapper
@Repository
public interface UserMapper {
	User findByUsername(String username);

	User findById(Long id);

	int updateUserByUsername(String username, User user);
	
	// 插入新用户
	int insertUser(User user);
	
	// 根据ID更新用户信息
	int updateById(User user);
}
