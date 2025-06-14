package top.wsido.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @Description: 博客动态
 * @Author: wsido
 * @Date: 2020-08-24
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Moment {
	private Long id;
	private String content;//动态内容
	private Date createTime;//创建时间
	private Integer likes;//点赞数量
	private Boolean published;//是否公开
	private Long userId; // 新增用户ID字段
}
