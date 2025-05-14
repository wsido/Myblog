package top.wsido.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 用户注册信息
 * @Author: wsido
 * @Date: 2023-05-15
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDTO {
	private String username;
	private String password;
	private String nickname;
	private String email;
} 