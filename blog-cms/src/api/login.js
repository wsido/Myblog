import request from '@/util/request'

export function login(data) {
	return request({
		url: 'admin/login',
		method: 'post',
		data
	})
}

export function getInfo() {
	return request({
		url: '/admin/user', // Corrected to point to the actual backend endpoint
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
