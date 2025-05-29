<template>
	<div>
		<el-card>
			<div slot="header">
				<span>修改当前登录账户</span>
			</div>
			<el-form :model="accountForm" label-width="80px">
				<el-form-item label="新账号">
					<el-input v-model="accountForm.username" placeholder="输入新用户名"></el-input>
				</el-form-item>
				<el-form-item label="新密码">
					<el-input v-model="accountForm.password" type="password" placeholder="输入新密码" show-password></el-input>
				</el-form-item>
				<el-form-item>
					<el-popconfirm title="确定修改吗？修改后需要重新登录。" icon="el-icon-user-solid" iconColor="#409EFF" @confirm="saveAccountChanges">
						<el-button type="primary" size="medium" icon="el-icon-check" slot="reference" :disabled="!accountForm.username || !accountForm.password">确认修改</el-button>
					</el-popconfirm>
					<el-button type="info" size="medium" @click="testConnection" style="margin-left: 10px;">测试连接</el-button>
					<el-button type="warning" size="medium" @click="relogin" style="margin-left: 10px;">重新登录</el-button>
				</el-form-item>
			</el-form>
		</el-card>
	</div>
</template>

<script>
import { changeAccount } from "@/api/account";
import { mapGetters } from 'vuex';
import { getToken } from '@/util/auth'; // 导入获取token的函数

export default {
	name: "AccountSettings",
	data() {
		return {
			accountForm: {
				username: '',
				password: ''
			}
		}
	},
	computed: {
		...mapGetters([
			'name',
			'userId'
		])
	},
	created() {
		if (this.name) {
			this.accountForm.username = this.name;
		}
	},
	methods: {
		async saveAccountChanges() {
			try {
				console.log('开始提交账户修改');
				const userUpdatePayload = {
					username: this.accountForm.username,
					password: this.accountForm.password
				};
				console.log('提交数据:', userUpdatePayload);
				
				const res = await changeAccount(userUpdatePayload);
				console.log('修改成功, 响应:', res);
				this.$message.success(res.msg || '修改成功，请重新登录');
				
				// 确保登出操作完成后再跳转页面
				setTimeout(async () => {
					try {
						console.log('执行登出');
						await this.$store.dispatch('user/logout');
						console.log('登出成功，即将跳转到登录页');
						// 直接刷新页面并强制跳转到登录页
						localStorage.removeItem('token');
						localStorage.removeItem('userInfo');
						window.location.href = '/login';
					} catch (error) {
						console.error('登出过程出错:', error);
						// 即使登出失败也强制跳转
						window.location.href = '/login';
					}
				}, 1500); // 给予消息显示的时间
			} catch (error) {
				console.error('修改失败:', error);
				this.$message.error(error.response?.data?.msg || error.message || '修改失败');
			}
		},
		async testConnection() {
			try {
				console.log('测试连接开始');
				// 使用原生axios，不依赖配置好的实例
				const axios = require('axios');
				
				// 显示登录状态
				const token = getToken();
				console.log('当前token:', token);
				this.$message.info('当前token状态: ' + (token ? '已找到' : '未找到') + 
					(token ? ' (长度:' + token.length + ')' : ''));
				
				// 测试连接
				console.log('测试连接到: http://localhost:8090/user/test');
				const testResponse = await axios.get('http://localhost:8090/user/test');
				console.log('测试接口响应:', testResponse.data);
				this.$message.success('测试连接成功: ' + JSON.stringify(testResponse.data));
				
				// 测试账户接口
				console.log('测试连接到账户接口');
				if (!token) {
					this.$message.warning('没有找到token，请确保已登录');
					return;
				}
				
				try {
					const accountResponse = await axios({
						url: 'http://localhost:8090/user/account',
						method: 'POST',
						headers: {
							'Content-Type': 'application/json',
							'Authorization': token
						},
						data: {
							username: this.accountForm.username || 'test',
							password: this.accountForm.password || 'test'
						}
					});
					console.log('账户接口响应:', accountResponse.data);
					this.$message.success('账户接口测试成功: ' + JSON.stringify(accountResponse.data));
				} catch (accountError) {
					console.error('账户接口测试失败:', accountError);
					this.$message.error('账户接口测试失败: ' + (accountError.response?.data?.msg || accountError.message));
				}
			} catch (error) {
				console.error('测试连接失败:', error);
				this.$message.error('测试连接失败: ' + (error.response?.data?.msg || error.message));
			}
		},
		relogin() {
			this.$confirm('确定要重新登录吗?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				// 移除token和用户信息
				this.$store.dispatch('user/logout').then(() => {
					this.$message.success('已登出，即将跳转到登录页面');
					// 延迟跳转，让用户看到消息
					setTimeout(() => {
						window.location.href = '/login';
					}, 1500);
				}).catch(error => {
					console.error('登出失败:', error);
					this.$message.error('登出失败，但仍将跳转到登录页面');
					// 强制跳转
					window.location.href = '/login';
				});
			}).catch(() => {
				this.$message.info('已取消重新登录');
			});
		}
	}
}
</script>

<style scoped>
.el-card {
	width: 50%;
}
</style>
