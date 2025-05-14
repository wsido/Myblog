package top.wsido.util.comment.channel;

import top.wsido.constant.CommentConstants;
import top.wsido.util.common.SpringContextUtils;

/**
 * 评论提醒方式
 *
 * @author: wsido
 * @date: 2022-01-22
 */
public class ChannelFactory {
	/**
	 * 创建评论提醒方式
	 *
	 * @param channelName 方式名称
	 * @return
	 */
	public static CommentNotifyChannel getChannel(String channelName) {
		if (CommentConstants.MAIL.equalsIgnoreCase(channelName)) {
			return SpringContextUtils.getBean("mailChannel", CommentNotifyChannel.class);
		}
		throw new RuntimeException("Unsupported value in [application.properties]: [comment.notify.channel]");
	}
}
