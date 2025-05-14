import axios from '@/plugins/axios'

// 注释掉或删除 getHitokoto 函数
// export function getHitokoto() {
// 	return axios({
// 		url: '/hitokoto/?c=a',
// 		method: 'get'
// 	})
// }

export function getSite() {
	return axios({
		url: 'site',
		method: 'GET'
	})
}