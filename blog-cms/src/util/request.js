import { getToken } from '@/util/auth'
import axios from 'axios'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const request = axios.create({
	baseURL: 'http://localhost:8090/',
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
		console.log(`[Request Interceptor] Path: ${config.url}, Token: ${token}`);
		if (token) {
			config.headers.Authorization = 'Bearer ' + token
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
		const res = response.data // res is the Result object { code, msg, data }
		if (res.code === 200) { // Successful business logic
				return res // Return the full Result object, components will use res.data for payload
		} else {
				// Handle business errors reported by the backend in the Result object
				let msg = res.msg || '操作失败'
				if (res.code === 40101) { // Example: Specific code from backend indicating "Please Login"
						msg = res.msg || '会话已过期，请重新登录';
						// store.dispatch('user/resetToken').then(() => location.reload()) // Uncomment and adapt if Vuex store is accessible
				} else if (res.code === 40301) { // Example: Specific code for "No Permission"
						msg = res.msg || '您没有权限执行此操作';
				}
				Message.error(msg)
				return Promise.reject(new Error(msg)) // Reject with the business error message
		}
	},
	error => { // Handles network errors or other errors before reaching the business logic (e.g., actual HTTP 401/403/404/500)
		NProgress.done()
		console.error('Axios request error:', error.toJSON ? error.toJSON() : error ); // Log more details if available
		let errorMsg = '网络请求异常，请稍后重试';
		if (error.response) {
				// The request was made and the server responded with a status code
				// that falls out of the range of 2xx
				if (error.response.status === 401) {
						errorMsg = '会话已过期或未授权，请重新登录';
						// store.dispatch('user/resetToken').then(() => location.reload()) // Uncomment and adapt
				} else if (error.response.status === 403) {
						errorMsg = '禁止访问：您没有权限执行此操作';
				} else if (error.response.status === 404) {
						errorMsg = '请求的资源未找到 (404)';
				} else if (error.response.data && error.response.data.msg) {
						errorMsg = error.response.data.msg; // If backend error response has a msg field (e.g. for Spring Boot default error responses)
				} else {
						errorMsg = `请求错误 ${error.response.status}`;
				}
		} else if (error.request) {
				// The request was made but no response was received
				errorMsg = '无法连接到服务器，请检查您的网络连接或服务器状态';
		} else {
				// Something happened in setting up the request that triggered an Error
				errorMsg = error.message;
		}
		Message.error(errorMsg)
		return Promise.reject(new Error(errorMsg)) // Ensure a new Error object is rejected for consistency
	}
)

export default request