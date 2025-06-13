import axios from '@/util/request'

export function getAbout() {
	return axios({
		url: 'admin/about',
		method: 'GET'
	})
}

export function updateAbout(form) {
	return axios({
		url: 'admin/about',
		method: 'PUT',
		data: {
			...form
		}
	})
}