import axios from '@/util/request'

export function getMomentListByQuery(queryInfo) {
	return axios({
		url: 'moments',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function getCurrentUserMomentListByQuery(queryInfo) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/moment/list',
		method: 'GET',
		params: {
			pageNum: queryInfo.pageNum,
			pageSize: queryInfo.pageSize
		}
	})
}

export function updatePublished(id, published) {
	return axios({
		url: 'admin/moment/published',
		method: 'PUT',
		params: {
			id,
			published
		}
	})
}

export function getMomentById(id) {
	return axios({
		url: 'moment',
		method: 'get',
		params: {id}
	})
}

export function getAdminMomentById(id) {
	return axios({
		url: 'admin/moment',
		method: 'get',
		params: { id }
	});
}

export function getCurrentUserMomentById(id) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/moment/${id}`,
		method: 'GET'
	})
}

export function deleteMomentById(id) {
	return axios({
		url: 'admin/moment',
		method: 'DELETE',
		params: {
			id
		}
	})
}

export function deleteCurrentUserMomentById(id) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/moment/${id}`,
		method: 'DELETE'
	})
}

export function saveMoment(moment) {
	return axios({
		url: 'admin/moment',
		method: 'POST',
		data: {
			...moment
		}
	});
}

export function saveCurrentUserMoment(momentDto) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/moment',
		method: 'POST',
		data: momentDto
	})
}

export function updateMoment(moment) {
	return axios({
		url: 'admin/moment',
		method: 'PUT',
		data: {
			...moment
		}
	});
}

export function updateCurrentUserMoment(momentDto) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/moment/${momentDto.id}`,
		method: 'PUT',
		data: momentDto
	})
}