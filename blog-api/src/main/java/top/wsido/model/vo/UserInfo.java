package top.wsido.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserInfo {
    private Long id;
    private String nickname;
    private String avatar;
    // Add other fields as needed, for example:
    // private String username;
    // private String email;
} 