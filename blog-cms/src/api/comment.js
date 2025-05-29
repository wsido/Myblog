import axios from '@/util/request'

export function getCommentListByQuery(queryInfo) {
	return axios({
		url: 'comments',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function getCurrentUserCommentListByQuery(queryInfo) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/comment/list',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function getBlogList() {
	return axios({
		url: '/api/user/management/myBlogIdAndTitle',
		method: 'GET'
	})
}

export function updatePublished(id, published) {
	return axios({
		url: 'comment/published',
		method: 'PUT',
		params: {
			id,
			published
		}
	})
}

export function updateCurrentUserCommentPublished(id, published) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/comment/published',
		method: 'PUT',
		params: {
			id,
			published
		}
	})
}

export function updateNotice(id, notice) {
	return axios({
		url: 'comment/notice',
		method: 'PUT',
		params: {
			id,
			notice
		}
	})
}

export function updateCurrentUserCommentNotice(id, notice) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/comment/notice',
		method: 'PUT',
		params: {
			id,
			notice
		}
	})
}

export function deleteCommentById(id) {
	return axios({
		url: 'admin/comment',
		method: 'DELETE',
		params: {
			id
		}
	})
}

export function deleteCurrentUserCommentById(id) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/comment/${id}`,
		method: 'DELETE'
	})
}

export function editComment(form) {
	return axios({
		url: 'comment',
		method: 'PUT',
		data: {
			...form
		}
	})
}

export function editCurrentUserComment(form) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/comment/${form.id}`,
		method: 'PUT',
		data: form
	})
}