import axios from '@/util/request'

export function getDataByQuery(queryInfo) {
	return axios({
		url: 'blogs',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function getCurrentUserBlogsByQuery(queryInfo) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/blog/list',
		method: 'GET',
		params: {
			pageNum: queryInfo.pageNum,
			pageSize: queryInfo.pageSize,
			title: queryInfo.title,
			categoryId: queryInfo.categoryId
		}
	})
}

export function deleteBlogById(id) {
	return axios({
		url: 'admin/blog',
		method: 'DELETE',
		params: {
			id
		}
	})
}

export function deleteCurrentUserBlogById(id) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/blog/${id}`,
		method: 'DELETE'
	})
}

export function getCategoryAndTag() {
	return axios({
		url: '/admin/categoryAndTag',
		method: 'GET'
	})
}

export function getUserCategoryAndTag() {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/blog/categoryAndTag',
		method: 'GET'
	})
}

export function saveBlog(blog) {
	return axios({
		url: 'admin/blog',
		method: 'POST',
		data: {
			...blog
		}
	})
}

export function saveCurrentUserBlog(blogDto) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: 'user/blog',
		method: 'POST',
		data: blogDto
	})
}

export function updateCurrentUserBlog(blogDto) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/blog/${blogDto.id}`,
		method: 'PUT',
		data: blogDto
	})
}

export function updateTop(id, top) {
	return axios({
		url: 'admin/blog/top',
		method: 'PUT',
		params: {
			id,
			top
		}
	})
}

export function updateRecommend(id, recommend) {
	return axios({
		url: 'admin/blog/recommend',
		method: 'PUT',
		params: {
			id,
			recommend
		}
	})
}

export function updateVisibility(id, form) {
	return axios({
		url: `admin/blog/${id}/visibility`,
		method: 'PUT',
		data: {
			...form
		}
	})
}

export function updateCurrentUserBlogVisibility(id, form) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/blog/${id}/visibility`,
		method: 'PUT',
		data: form
	})
}

export function getPublicBlogById(id) {
	return axios({
		url: 'blog',
		method: 'GET',
		params: {
			id
		}
	})
}

export function adminGetBlogById(id) {
	return axios({
		url: `admin/blog/${id}`,
		method: 'GET'
	})
}

export function getCurrentUserBlogById(id) {
	return axios({
		baseURL: 'http://localhost:8090/',
		url: `user/blog/${id}`,
		method: 'GET'
	})
}

export function updateBlog(blog) {
	return axios({
		url: 'admin/blog',
		method: 'PUT',
		data: {
			...blog
		}
	})
}

// User Content Management APIs (New)
export function getUserManagementBlogs(queryInfo) {
	return axios({
		url: '/api/user/management/blogs', // Note: leading slash if your request util handles base URL correctly
		method: 'GET',
		params: {
			...queryInfo // pageNum, pageSize, title, categoryId
		}
	})
}

export function createUserManagementBlog(blogData) {
	return axios({
		url: '/api/user/management/blogs',
		method: 'POST',
		data: blogData
	})
}

export function getUserManagementBlogDetail(blogId) {
	return axios({
		url: `/api/user/management/blogs/${blogId}`,
		method: 'GET'
	})
}

export function updateUserManagementBlog(blogId, blogData) {
	return axios({
		url: `/api/user/management/blogs/${blogId}`,
		method: 'PUT',
		data: blogData
	})
}

export function deleteUserManagementBlog(blogId) {
	return axios({
		url: `/api/user/management/blogs/${blogId}`,
		method: 'DELETE'
	})
}

export function getUserManagementCategoriesAndTags() {
	return axios({
		url: '/api/user/management/categories-tags',
		method: 'GET'
	})
}

// Admin specific API to get blogs
export function getAdminBlogs(queryInfo) {
	return axios({
		url: '/admin/blogs',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}