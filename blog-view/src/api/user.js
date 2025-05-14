import axios from '@/plugins/axios'

// 用户注册
export function register(registerForm) {
	return axios({
		url: '/user/register',
		method: 'POST',
		data: {
			...registerForm
		}
	})
}

// 用户登录
export function userLogin(loginForm) {
	return axios({
		url: '/user/login',
		method: 'POST',
		data: {
			...loginForm
		}
	})
}

// 获取用户信息
export function getUserInfo() {
	return axios({
		url: '/user/info',
		method: 'GET'
	})
}

// 更新用户信息
export function updateUserInfo(userInfo) {
	return axios({
		url: '/user/info',
		method: 'PUT',
		data: {
			...userInfo
		}
	})
}

// 修改密码
export function updatePassword(passwordForm) {
	return axios({
		url: '/user/password',
		method: 'PUT',
		data: {
			...passwordForm
		}
	})
}

// 获取用户博客列表
export function getUserBlogs(pageNum, pageSize) {
	return axios({
		url: '/user/blog/list',
		method: 'GET',
		params: {
			pageNum,
			pageSize
		}
	})
}

// 获取博客详情
export function getBlogById(id) {
	return axios({
		url: `/user/blog/${id}`,
		method: 'GET'
	})
}

// 更新博客可见性
export function updateBlogVisibility(id, visibilityForm) {
	return axios({
		url: `/user/blog/${id}/visibility`,
		method: 'PUT',
		data: {
			...visibilityForm
		}
	})
}

// 删除博客
export function deleteBlog(id) {
	return axios({
		url: `/user/blog/${id}`,
		method: 'DELETE'
	})
}

// 上传用户头像
export function uploadAvatar(formData) {
	return axios({
		url: '/user/avatar',
		method: 'POST',
		data: formData,
		headers: {
			'Content-Type': 'multipart/form-data'
		}
	})
}

// 获取用户统计数据
export function getUserStats() {
	return axios({
		url: '/user/stats',
		method: 'GET'
	})
}

// 获取用户收藏列表
export function getUserFavorites(pageNum, pageSize) {
	return axios({
		url: '/user/favorites',
		method: 'GET',
		params: {
			pageNum,
			pageSize
		}
	})
}

// 添加收藏
export function addFavorite(blogId) {
	return axios({
		url: '/user/favorites',
		method: 'POST',
		data: {
			blogId
		}
	})
}

// 取消收藏
export function removeFavorite(blogId) {
	return axios({
		url: `/user/favorites/${blogId}`,
		method: 'DELETE'
	})
} 