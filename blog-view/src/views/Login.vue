<template>
	<div class="login_container">
		<div class="login_box">
			<!--头像-->
			<div class="avatar_box">
				<img src="/img/avatar.jpg" alt="">
			</div>
			<!--登录表单-->
			<el-form ref="loginFormRef" :model="loginForm" :rules="loginFormRules" class="login_form">
				<el-form-item>
					<el-radio-group v-model="loginType">
						<el-radio :label="1">普通用户</el-radio>
						<el-radio :label="2">管理员</el-radio>
					</el-radio-group>
				</el-form-item>
				<el-form-item prop="username">
					<el-input v-model="loginForm.username" prefix-icon="el-icon-user-solid" placeholder="用户名"></el-input>
				</el-form-item>
				<el-form-item prop="password">
					<el-input v-model="loginForm.password" prefix-icon="el-icon-lock" show-password placeholder="密码" @keyup.native.enter="login"></el-input>
				</el-form-item>
				<el-form-item class="btns">
					<el-button type="primary" @click="login">登录</el-button>
					<el-button type="info" @click="resetLoginForm">重置</el-button>
				</el-form-item>
				<el-form-item v-if="loginType === 1" class="register-link">
					<router-link to="/register">没有账号？立即注册</router-link>
				</el-form-item>
			</el-form>
		</div>
	</div>
</template>

<script>
	import { login } from "@/api/login";
import { userLogin } from "@/api/user";

	export default {
		name: "Login",
		data() {
			return {
				loginType: 1,  // 1: 普通用户, 2: 管理员
				loginForm: {
					username: '',
					password: ''
				},
				loginFormRules: {
					username: [
						{required: true, message: '请输入用户名', trigger: 'blur'},
					],
					password: [
						{required: true, message: '请输入密码', trigger: 'blur'},
					]
				}
			}
		},
		methods: {
			resetLoginForm() {
				this.$refs.loginFormRef.resetFields();
			},
			login() {
				this.$refs.loginFormRef.validate(valid => {
					if (valid) {
						// 根据登录类型选择不同的API
						const loginAction = this.loginType === 1 ? userLogin : login;
						const tokenKey = this.loginType === 1 ? 'userToken' : 'adminToken';
						
						loginAction(this.loginForm).then(res => {
							if (res.code === 200) {
								this.msgSuccess(res.msg)
								window.localStorage.setItem(tokenKey, res.data.token)
								
								// 跳转到不同的页面
								if (this.loginType === 1) {
									// 普通用户跳转到首页
									this.$router.push('/home')
									// 添加延迟，确保token已存储，页面刷新获取最新状态
									setTimeout(() => {
										window.location.reload()
									}, 500)
								} else {
									// 管理员跳转到后台
									window.open('/admin', '_blank')
								}
							} else {
								this.msgError(res.msg)
							}
						}).catch(() => {
							this.msgError("请求失败")
						})
					}
				})
			}
		}
	}
</script>

<style scoped>
	.login_container {
		box-sizing: unset !important;
		height: 100%;
		background-color: #2b4b6b;
	}

	.login_box {
		width: 450px;
		height: 330px;
		background-color: #fff;
		border-radius: 3px;
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
	}

	.login_box .avatar_box {
		height: 130px;
		width: 130px;
		border: 1px solid #eee;
		border-radius: 50%;
		padding: 10px;
		box-shadow: 0 0 10px #ddd;
		position: absolute;
		left: 50%;
		transform: translate(-50%, -50%);
		background-color: #fff;
	}

	.login_box .avatar_box img {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background-color: #eee;
	}

	.login_form {
		position: absolute;
		bottom: 0;
		width: 100%;
		padding: 0 20px;
		box-sizing: border-box;
	}

	.btns {
		display: flex;
		justify-content: flex-end;
	}
	
	.register-link {
		margin-top: -15px;
		margin-bottom: 10px;
		text-align: center;
	}
	
	.register-link a {
		color: #409EFF;
		text-decoration: none;
	}
</style>