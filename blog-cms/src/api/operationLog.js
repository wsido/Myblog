import axios from '@/util/request'

export function getOperationLogList(queryInfo) {
	return axios({
		url: 'admin/operationLogs',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function deleteOperationLogById(id) {
	return axios({
		url: 'admin/operationLog',
		method: 'DELETE',
		params: {
			id
		}
	})
}