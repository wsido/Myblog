package top.wsido.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import top.wsido.entity.Category;
import top.wsido.entity.Tag;
// import top.wsido.entity.User; // No longer directly using User entity here

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 博客详情
 * @Author: wsido
 * @Date: 2020-08-12
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BlogDetail {
	private Long id;
	private String title;//文章标题
	private String content;//文章正文
	private Boolean published; // 新增
	private Boolean recommend; // 新增
	private Boolean appreciation;//赞赏开关
	private Boolean commentEnabled;//评论开关
	private Boolean top;//是否置顶
	private Date createTime;//创建时间
	private Date updateTime;//更新时间
	private Integer views;//浏览次数
	private Integer words;//文章字数
	private Integer readTime;//阅读时长(分钟)
	private String password;//密码保护

	private Category category;//文章分类
	private List<Tag> tags = new ArrayList<>();//文章标签
	private UserInfo user; // 新增，用于显示作者信息
}
