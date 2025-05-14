package top.wsido.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wsido.entity.ExceptionLog;
import top.wsido.exception.PersistenceException;
import top.wsido.mapper.ExceptionLogMapper;
import top.wsido.model.dto.UserAgentDTO;
import top.wsido.service.ExceptionLogService;
import top.wsido.util.IpAddressUtils;
import top.wsido.util.UserAgentUtils;

import java.util.List;

/**
 * @Description: 异常日志业务层实现
 * @Author: wsido
 * @Date: 2020-12-03
 */
@Service
public class ExceptionLogServiceImpl implements ExceptionLogService {
	@Autowired
	ExceptionLogMapper exceptionLogMapper;
	@Autowired
	UserAgentUtils userAgentUtils;

	@Override
	public List<ExceptionLog> getExceptionLogListByDate(String startDate, String endDate) {
		return exceptionLogMapper.getExceptionLogListByDate(startDate, endDate);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveExceptionLog(ExceptionLog log) {
		String ipSource = IpAddressUtils.getCityInfo(log.getIp());
		UserAgentDTO userAgentDTO = userAgentUtils.parseOsAndBrowser(log.getUserAgent());
		log.setIpSource(ipSource);
		log.setOs(userAgentDTO.getOs());
		log.setBrowser(userAgentDTO.getBrowser());
		if (exceptionLogMapper.saveExceptionLog(log) != 1) {
			throw new PersistenceException("日志添加失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteExceptionLogById(Long id) {
		if (exceptionLogMapper.deleteExceptionLogById(id) != 1) {
			throw new PersistenceException("删除日志失败");
		}
	}
}
