import request from '@/util/request'

export function login(data) {
	return request({
		url: 'login',
		method: 'post',
		data
	})
}

export function getInfo() {
	return request({
		url: 'user', // Assumes backend exposes GET /admin/user for current user info
		method: 'get'
	})
}

// Optional: Add a getInfo API call if you want to verify token/get user info from backend
// export function getInfo() {
// 	return request({
// 		url: '/user', // Example: an endpoint that returns current user info based on token
// 		method: 'get'
// 	})
// }
