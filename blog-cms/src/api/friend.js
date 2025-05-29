import axios from '@/util/request'

export function getFriendsByQuery(queryInfo) {
	return axios({
		url: '/admin/friends',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function updatePublished(id, published) {
	return axios({
		url: '/admin/friend/published',
		method: 'PUT',
		params: {
			id,
			published
		}
	})
}

export function saveFriend(form) {
	return axios({
		url: '/admin/friend',
		method: 'POST',
		data: {
			...form
		}
	})
}

export function updateFriend(form) {
	return axios({
		url: '/admin/friend',
		method: 'PUT',
		data: {
			...form
		}
	})
}

export function deleteFriendById(id) {
	return axios({
		url: '/admin/friend',
		method: 'DELETE',
		params: {
			id
		}
	})
}

export function getFriendInfo() {
	return axios({
		url: '/admin/friendInfo',
		method: 'GET'
	})
}

export function updateCommentEnabled(commentEnabled) {
	return axios({
		url: '/admin/friendInfo/commentEnabled',
		method: 'PUT',
		params: {
			commentEnabled
		}
	})
}

export function updateContent(content) {
	return axios({
		url: '/admin/friendInfo/content',
		method: 'PUT',
		data: {
			content
		}
	})
}