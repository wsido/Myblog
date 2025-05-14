package top.wsido.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsVO {
    private Integer blogCount;    // 用户发表的博客数量
    private Integer viewCount;    // 用户所有博客的总访问量
    private Integer commentCount; // 用户博客收到的总评论数
} 