package top.wsido.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.wsido.constant.RedisKeyConstants;
import top.wsido.entity.Friend;
import top.wsido.entity.SiteSetting;
import top.wsido.exception.PersistenceException;
import top.wsido.mapper.FriendMapper;
import top.wsido.mapper.SiteSettingMapper;
import top.wsido.model.vo.FriendInfo;
import top.wsido.service.FriendService;
import top.wsido.service.RedisService;
import top.wsido.util.markdown.MarkdownUtils;

/**
 * @Description: 友链业务层实现
 * @Author: wsido
 * @Date: 2020-09-08
 */
@Service
public class FriendServiceImpl implements FriendService {
	@Autowired
	FriendMapper friendMapper;
	@Autowired
	SiteSettingMapper siteSettingMapper;
	@Autowired
	RedisService redisService;

	@Override
	public List<Friend> getFriendList() {
		return friendMapper.getFriendList();
	}

	@Override
	public List<top.wsido.model.vo.Friend> getFriendVOList() {
		return friendMapper.getFriendVOList();
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateFriendPublishedById(Long friendId, Boolean published) {
		if (friendMapper.updateFriendPublishedById(friendId, published) != 1) {
			throw new PersistenceException("操作失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveFriend(Friend friend) {
		friend.setViews(0);
		friend.setCreateTime(new Date());
		if (friendMapper.saveFriend(friend) != 1) {
			throw new PersistenceException("添加失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateFriend(top.wsido.model.dto.Friend friend) {
		if (friendMapper.updateFriend(friend) != 1) {
			throw new PersistenceException("修改失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteFriend(Long id) {
		if (friendMapper.deleteFriend(id) != 1) {
			throw new PersistenceException("删除失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateViewsByNickname(String nickname) {
		if (friendMapper.updateViewsByNickname(nickname) != 1) {
			throw new PersistenceException("操作失败");
		}
	}

	@Override
	public FriendInfo getFriendInfo(boolean cache, boolean md) {
		String redisKey = RedisKeyConstants.FRIEND_INFO_MAP;
		if (cache) {
			FriendInfo friendInfoFromRedis = redisService.getObjectByValue(redisKey, FriendInfo.class);
			if (friendInfoFromRedis != null) {
				return friendInfoFromRedis;
			}
		}
		List<SiteSetting> siteSettings = siteSettingMapper.getFriendInfo();
		FriendInfo friendInfo = new FriendInfo();
		for (SiteSetting siteSetting : siteSettings) {
			if ("friendContent".equals(siteSetting.getNameEn())) {
				if (md) {
					friendInfo.setContent(MarkdownUtils.markdownToHtmlExtensions(siteSetting.getValue()));
				} else {
					friendInfo.setContent(siteSetting.getValue());
				}
			} else if ("friendCommentEnabled".equals(siteSetting.getNameEn())) {
				if ("1".equals(siteSetting.getValue())) {
					friendInfo.setCommentEnabled(true);
				} else {
					friendInfo.setCommentEnabled(false);
				}
			}
		}
		if (cache && md) {
			redisService.saveObjectToValue(redisKey, friendInfo);
		}
		return friendInfo;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateFriendInfoContent(String content) {
		if (siteSettingMapper.updateFriendInfoContent(content) != 1) {
			throw new PersistenceException("修改失败");
		}
		deleteFriendInfoRedisCache();
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateFriendInfoCommentEnabled(Boolean commentEnabled) {
		if (siteSettingMapper.updateFriendInfoCommentEnabled(commentEnabled) != 1) {
			throw new PersistenceException("修改失败");
		}
		deleteFriendInfoRedisCache();
	}

	/**
	 * 删除友链页面缓存
	 */
	private void deleteFriendInfoRedisCache() {
		redisService.deleteCacheByKey(RedisKeyConstants.FRIEND_INFO_MAP);
	}
}
