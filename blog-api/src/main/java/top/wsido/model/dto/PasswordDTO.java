package top.wsido.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 密码更新信息
 * @Author: wsido
 * @Date: 2023-05-15
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PasswordDTO {
	private String oldPassword;
	private String newPassword;
} 