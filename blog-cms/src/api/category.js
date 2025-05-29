import axios from '@/util/request'

export function getData(queryInfo) {
	return axios({
		url: '/admin/categoryAndTag',
		method: 'GET',
	})
}

export function addCategory(form) {
	return axios({
		url: 'admin/category',
		method: 'POST',
		data: {
			...form
		}
	})
}

export function editCategory(form) {
	return axios({
		url: 'admin/category',
		method: 'PUT',
		data: {
			...form
		}
	})
}

export function deleteCategoryById(id) {
	return axios({
		url: 'admin/category',
		method: 'DELETE',
		params: {
			id
		}
	})
}