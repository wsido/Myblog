import axios from '@/util/request'

export function getLoginLogList(queryInfo) {
	return axios({
		url: 'admin/loginLogs',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function deleteLoginLogById(id) {
	return axios({
		url: 'admin/loginLog',
		method: 'DELETE',
		params: {
			id
		}
	})
}