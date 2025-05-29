import axios from '@/plugins/axios'

export function getBlogById(token, id) {
	return axios({
		url: 'blog',
		method: 'GET',
		headers: {
			Authorization: token,
		},
		params: {
			id
		}
	})
}

export function checkBlogPassword(blogPasswordForm) {
	return axios({
		url: 'checkBlogPassword',
		method: 'POST',
		data: {
			...blogPasswordForm
		}
	})
}

export function getSearchBlogList(query) {
	return axios({
		url: 'searchBlog',
		method: 'GET',
		params: {
			query
		}
	})
}

// 添加收藏相关的 API 函数
export function checkFavorite(blogId) {
	const token = window.localStorage.getItem('userToken'); // 或者从 Vuex store 获取
	return axios({
		url: `/user/favorites/check/${blogId}`,
		method: 'GET',
		headers: {
			Authorization: token,
		},
	});
}

export function addFavorite(blogId) {
	const token = window.localStorage.getItem('userToken');
	return axios({
		url: '/user/favorites',
		method: 'POST',
		headers: {
			Authorization: token,
		},
		data: {
			blogId,
		},
	});
}

export function removeFavorite(blogId) {
	const token = window.localStorage.getItem('userToken');
	return axios({
		url: `/user/favorites/${blogId}`,
		method: 'DELETE',
		headers: {
			Authorization: token,
		},
	});
}