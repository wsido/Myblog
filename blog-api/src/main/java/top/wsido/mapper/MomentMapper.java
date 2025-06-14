package top.wsido.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.wsido.entity.Moment;

import java.util.List;

/**
 * @Description: 博客动态持久层接口
 * @Author: wsido
 * @Date: 2020-08-24
 */
@Mapper
@Repository
public interface MomentMapper {
	List<Moment> getMomentList();

	List<Moment> getMomentListByUserId(Long userId);

	int addLikeByMomentId(Long momentId);

	int updateMomentPublishedById(Long momentId, Boolean published);

	Moment getMomentById(Long id);

	int deleteMomentById(Long id);

	int saveMoment(Moment moment);

	int updateMoment(Moment moment);
}
