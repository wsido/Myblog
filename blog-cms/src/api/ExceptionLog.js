import axios from '@/util/request'

export function getExceptionLogList(queryInfo) {
	return axios({
		url: 'admin/exceptionLogs',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function deleteExceptionLogById(id) {
	return axios({
		url: 'admin/exceptionLog',
		method: 'DELETE',
		params: {
			id
		}
	})
}