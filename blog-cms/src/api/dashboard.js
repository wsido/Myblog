import axios from '@/util/request'

export function getDashboard() {
	return axios({
		url: '/admin/dashboard',
		method: 'GET'
	})
}