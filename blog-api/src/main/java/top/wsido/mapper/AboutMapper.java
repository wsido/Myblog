package top.wsido.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.wsido.entity.About;

import java.util.List;

/**
 * @Description: 关于我持久层接口
 * @Author: wsido
 * @Date: 2020-08-31
 */
@Mapper
@Repository
public interface AboutMapper {
	List<About> getList();

	int updateAbout(String nameEn, String value);

	String getAboutCommentEnabled();
}
