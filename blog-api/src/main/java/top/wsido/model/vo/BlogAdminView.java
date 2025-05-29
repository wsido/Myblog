package top.wsido.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class BlogAdminView {
    private Long id;
    private String title;
    private Date createTime;
    private Date updateTime;
    private Boolean published;       // is_published
    private Boolean recommend;       // is_recommend
    private Boolean appreciation;    // is_appreciation
    private Boolean commentEnabled;  // is_comment_enabled
    private Boolean top;             // is_top
    private String password;
    private String categoryName;    // c.category_name
    private String userNickname;    // u.nickname as user_nickname
} 