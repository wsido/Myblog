package top.wsido.util; // 或者您选择的其他包

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 在这里替换为您想为“测试用户1”设置的新密码
        String rawPassword = "123456"; 

        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("要更新的用户名: 测试用户1");
        System.out.println("新设置的明文密码: " + rawPassword);
        System.out.println("请将此哈希值更新到数据库中对应用户的password字段: " + encodedPassword);
    }
}