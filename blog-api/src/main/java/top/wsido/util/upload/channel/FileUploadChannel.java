package top.wsido.util.upload.channel;

import top.wsido.util.upload.UploadUtils;

/**
 * 文件上传方式
 *
 * @author: wsido
 * @date: 2022-01-23
 */
public interface FileUploadChannel {
	/**
	 * 通过指定方式上传文件
	 *
	 * @param image 需要保存的图片
	 * @return 访问图片的URL
	 * @throws Exception
	 */
	String upload(UploadUtils.ImageResource image) throws Exception;
}
