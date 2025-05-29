import axios from '@/util/request'

export function getJobList(queryInfo) {
	return axios({
		url: 'admin/jobs',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function updateJobStatus(jobId, status) {
	return axios({
		url: 'admin/job/status',
		method: 'PUT',
		params: {
			jobId,
			status
		}
	})
}

export function runJobOnce(jobId) {
	return axios({
		url: 'admin/job/run',
		method: 'POST',
		params: {
			jobId
		}
	})
}

export function deleteJobById(jobId) {
	return axios({
		url: 'admin/job',
		method: 'DELETE',
		params: {
			jobId
		}
	})
}

export function addJob(job) {
	return axios({
		url: 'admin/job',
		method: 'POST',
		data: {
			...job
		}
	})
}

export function editJob(job) {
	return axios({
		url: 'admin/job',
		method: 'PUT',
		data: {
			...job
		}
	})
}

export function getJobLogList(queryInfo) {
	return axios({
		url: 'admin/job/logs',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function deleteJobLogByLogId(logId) {
	return axios({
		url: 'admin/job/log',
		method: 'DELETE',
		params: {
			logId
		}
	})
}