<template>
	<div class="login_container">
		<div class="login_box">
			<!--头像-->
			<div class="avatar_box">
				<img src="/img/avatar.jpg" alt="">
			</div>
			<!--登录表单-->
			<el-form ref="loginFormRef" :model="loginForm" :rules="loginFormRules" class="login_form">
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
				<el-form-item class="register-link">
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
						if (this.loginType === 1) { // 普通用户登录
							this.$store.dispatch('login', this.loginForm).then(() => {
								// 登录成功且用户信息已存入Vuex (由action处理)
								this.$router.push('/home');
							}).catch(() => {
								// 登录失败或获取用户信息失败 (错误消息已在action中弹出)
							});
						} 
						// else { // 管理员登录逻辑不再需要，因为UI选项已移除
						// 	const adminLoginAPI = login; // login from "@/api/login"
						// 	adminLoginAPI(this.loginForm).then(res => {
						// 		if (res.code === 200 && res.data && res.data.token) {
						// 			this.msgSuccess(res.msg || '管理员登录成功');
						// 			window.localStorage.setItem('adminToken', res.data.token);
						// 			window.open('/admin', '_blank'); // 管理员跳转到后台
						// 		} else {
						// 			this.msgError(res.msg || '管理员登录失败');
						// 		}
						// 	}).catch(() => {
						// 		this.msgError("管理员登录请求失败");
						// 	});
						// }
					}
				});
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