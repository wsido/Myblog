package top.wsido.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wsido.constant.RedisKeyConstants;
import top.wsido.entity.Tag;
import top.wsido.exception.NotFoundException;
import top.wsido.exception.PersistenceException;
import top.wsido.mapper.TagMapper;
import top.wsido.service.RedisService;
import top.wsido.service.TagService;

import java.util.List;

/**
 * @Description: 博客标签业务层实现
 * @Author: wsido
 * @Date: 2020-07-30
 */
@Service
public class TagServiceImpl implements TagService {
	@Autowired
	TagMapper tagMapper;
	@Autowired
	RedisService redisService;

	@Override
	public List<Tag> getTagList() {
		return tagMapper.getTagList();
	}

	@Override
	public List<Tag> getTagListNotId() {
		String redisKey = RedisKeyConstants.TAG_CLOUD_LIST;
		List<Tag> tagListFromRedis = redisService.getListByValue(redisKey);
		if (tagListFromRedis != null) {
			return tagListFromRedis;
		}
		List<Tag> tagList = tagMapper.getTagListNotId();
		redisService.saveListToValue(redisKey, tagList);
		return tagList;
	}

	@Override
	public List<Tag> getTagListByBlogId(Long blogId) {
		return tagMapper.getTagListByBlogId(blogId);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveTag(Tag tag) {
		System.out.println("TagServiceImpl: Attempting to save tag to DB: " + tag.getName() + ", color: " + tag.getColor());
		try {
			int result = tagMapper.saveTag(tag);
			System.out.println("TagServiceImpl: Result of tagMapper.saveTag: " + result + ". Tag ID after save: " + tag.getId());
			if (result != 1) {
				System.err.println("TagServiceImpl: PersistenceException - Tag save failed, result was not 1. Tag: " + tag.getName());
				throw new PersistenceException("标签添加失败，数据库操作未返回预期结果。");
			}
		} catch (Exception e) {
			System.err.println("TagServiceImpl: Exception during tagMapper.saveTag for tag: " + tag.getName());
			e.printStackTrace(); // 打印详细的异常堆栈
			throw new PersistenceException("标签添加失败，数据库操作异常: " + e.getMessage(), e);
		}
		System.out.println("TagServiceImpl: Tag successfully saved to DB: " + tag.getName() + ". Now deleting Redis cache.");
		redisService.deleteCacheByKey(RedisKeyConstants.TAG_CLOUD_LIST);
	}

	@Override
	public Tag getTagById(Long id) {
		Tag tag = tagMapper.getTagById(id);
		if (tag == null) {
			throw new NotFoundException("标签不存在");
		}
		return tag;
	}

	@Override
	public Tag getTagByName(String name) {
		return tagMapper.getTagByName(name);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteTagById(Long id) {
		if (tagMapper.deleteTagById(id) != 1) {
			throw new PersistenceException("标签删除失败");
		}
		redisService.deleteCacheByKey(RedisKeyConstants.TAG_CLOUD_LIST);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateTag(Tag tag) {
		if (tagMapper.updateTag(tag) != 1) {
			throw new PersistenceException("标签更新失败");
		}
		redisService.deleteCacheByKey(RedisKeyConstants.TAG_CLOUD_LIST);
		//修改了标签名或颜色，可能有首页文章关联了标签，也要更新首页缓存
		redisService.deleteCacheByKey(RedisKeyConstants.HOME_BLOG_INFO_LIST);
	}
}
