import { getToken } from '@/util/auth'
import axios from 'axios'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const request = axios.create({
	baseURL: 'http://localhost:8090/admin/',
	timeout: 5000
})

let CancelToken = axios.CancelToken

// 请求拦截
request.interceptors.request.use(config => {
		//对于访客模式，除GET请求外，都拦截并提示
		const userJson = window.localStorage.getItem('userInfo') || '{}'
		const user = JSON.parse(userJson)
		if (userJson !== '{}' && user.type !== 'admin' && config.method !== 'get') {
			config.cancelToken = new CancelToken(function executor(cancel) {
				cancel('演示模式，不允许操作')
			})
			return config
		}

		NProgress.start()
		const token = getToken()
		if (token) {
			config.headers.Authorization = token
		}
		return config
	},
	error => {
		console.info(error)
		return Promise.reject(error)
	}
)

// 响应拦截
request.interceptors.response.use(response => {
		NProgress.done()
		const res = response.data
		if (res.code !== 200) {
			let msg = res.msg || 'Error'
			Message.error(msg)
			return Promise.reject(new Error(msg))
		}
		return res
	},
	error => {
		console.info(error)
		Message.error(error.message)
		return Promise.reject(error)
	}
)

export default request