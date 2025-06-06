package top.wsido.controller.admin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wsido.annotation.OperationLogger;
import top.wsido.entity.Tag;
import top.wsido.model.vo.Result;
import top.wsido.service.BlogService;
import top.wsido.service.TagService;
import top.wsido.util.StringUtils;
import top.wsido.util.SecurityUtils;
import top.wsido.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 博客标签后台管理
 * @Author: wsido
 * @Date: 2020-08-02
 */
@RestController
@RequestMapping("/admin")
public class TagAdminController {
	private static final Logger logger = LoggerFactory.getLogger(TagAdminController.class);

	@Autowired
	BlogService blogService;
	@Autowired
	TagService tagService;
	@Autowired
	private SecurityUtils securityUtils;

	/**
	 * 获取博客标签列表
	 *
	 * @param pageNum  页码
	 * @param pageSize 每页个数
	 * @return
	 */
	@GetMapping("/tags")
	public Result tags(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
		User currentUser = securityUtils.getCurrentUser();
		logger.info("[TagAdminController.tags] Current user from SecurityUtils: Username: {}, Type: {}", 
				(currentUser != null ? currentUser.getUsername() : "null"), 
				(currentUser != null ? currentUser.getType() : "null"));

		String orderBy = "id desc";
		PageHelper.startPage(pageNum, pageSize, orderBy);
		PageInfo<Tag> pageInfo = new PageInfo<>(tagService.getTagList());
		return Result.ok("请求成功", pageInfo);
	}

	/**
	 * 添加新标签
	 *
	 * @param tag 标签实体
	 * @return
	 */
	@OperationLogger("添加标签")
	@PostMapping("/tag")
	public Result saveTag(@RequestBody Tag tag) {
		return getResult(tag, "save");
	}

	/**
	 * 修改标签
	 *
	 * @param tag 标签实体
	 * @return
	 */
	@OperationLogger("修改标签")
	@PutMapping("/tag")
	public Result updateTag(@RequestBody Tag tag) {
		return getResult(tag, "update");
	}

	/**
	 * 执行标签添加或更新操作：校验参数是否合法，标签是否已存在
	 *
	 * @param tag  标签实体
	 * @param type 添加或更新
	 * @return
	 */
	private Result getResult(Tag tag, String type) {
		if (StringUtils.isEmpty(tag.getName())) {
			return Result.error("参数不能为空");
		}
		//查询标签是否已存在
		Tag tag1 = tagService.getTagByName(tag.getName());
		//如果 tag1.getId().equals(tag.getId()) == true 就是更新标签
		if (tag1 != null && !tag1.getId().equals(tag.getId())) {
			return Result.error("该标签已存在");
		}
		if ("save".equals(type)) {
			tagService.saveTag(tag);
			return Result.ok("添加成功");
		} else {
			tagService.updateTag(tag);
			return Result.ok("更新成功");
		}
	}

	/**
	 * 按id删除标签
	 *
	 * @param id 标签id
	 * @return
	 */
	@OperationLogger("删除标签")
	@DeleteMapping("/tag")
	public Result delete(@RequestParam Long id) {
		//删除存在博客关联的标签后，该博客的查询会出异常
		int num = blogService.countBlogByTagId(id);
		if (num != 0) {
			return Result.error("已有博客与此标签关联，不可删除");
		}
		tagService.deleteTagById(id);
		return Result.ok("删除成功");
	}
}
