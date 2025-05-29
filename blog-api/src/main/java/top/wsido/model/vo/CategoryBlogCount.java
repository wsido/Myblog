package top.wsido.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 分类和博客数量
 * @Author: wsido
 * @Date: 2020-10-08
 */
@Data
@NoArgsConstructor
public class CategoryBlogCount {
	private Long id;    // Corresponds to category_id in mapper, typically the category ID
	private String name;  // To store the category name
	private Integer value; // Corresponds to blog_count in mapper, the count of blogs
}
