package top.wsido.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.wsido.entity.ScheduleJob;

import java.util.List;

/**
 * @Description: 定时任务持久层接口
 * @Author: wsido
 * @Date: 2020-11-01
 */
@Mapper
@Repository
public interface ScheduleJobMapper {
	List<ScheduleJob> getJobList();

	ScheduleJob getJobById(Long jobId);

	int saveJob(ScheduleJob scheduleJob);

	int updateJob(ScheduleJob scheduleJob);

	int deleteJobById(Long jobId);

	int updateJobStatusById(Long jobId, Boolean status);
}
