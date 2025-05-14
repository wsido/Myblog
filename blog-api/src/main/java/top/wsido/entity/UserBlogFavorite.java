package top.wsido.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBlogFavorite {
    private Long id;
    private Long userId;
    private Long blogId;
    private Date createTime;
} 