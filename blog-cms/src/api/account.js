import axios from 'axios'
import { getToken } from '@/util/auth'

export function changeAccount(account) {
	return axios({
		url: 'http://localhost:8090/user/account',
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
			'Authorization': getToken()
		},
		data: {
			...account
		}
	})
}
