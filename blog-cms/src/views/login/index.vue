<template>
	<div class="login-container">
		<el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" auto-complete="on" label-position="left">
			<div class="title-container">
				<h1 class="title">后台管理</h1>
			</div>

			<el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user"/>
        </span>
				<el-input
						ref="username"
						v-model="loginForm.username"
						placeholder="Username"
						name="username"
						type="text"
						tabindex="1"
						auto-complete="on"
				/>
			</el-form-item>

			<el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password"/>
        </span>
				<el-input
						:key="passwordType"
						ref="password"
						v-model="loginForm.password"
						:type="passwordType"
						placeholder="Password"
						name="password"
						tabindex="2"
						auto-complete="on"
						@keyup.enter.native="handleLogin"
				/>
				<span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'"/>
        </span>
			</el-form-item>
			<el-button :loading="loading" type="primary" style="width:100%;margin-bottom:30px;" @click.native.prevent="handleLogin">Login</el-button>
		</el-form>
	</div>
</template>

<script>
import { login } from '@/api/login'; // Import the real login API

export default {
		name: 'Login',
		data() {
			return {
				loginForm: {
					username: '',
					password: ''
				},
				loginRules: {
					username: [
						{required: true, message: '请输入用户名', trigger: 'blur'},
					],
					password: [
						{required: true, message: '请输入密码', trigger: 'blur'},
					]
				},
				loading: false,
				passwordType: 'password',
			}
		},
		methods: {
			showPwd() {
				if (this.passwordType === 'password') {
					this.passwordType = ''
				} else {
					this.passwordType = 'password'
				}
				this.$nextTick(() => {
					this.$refs.password.focus()
				})
			},
			handleLogin() {
				this.$refs.loginForm.validate(valid => {
					if (valid) {
						this.loading = true
						// this.$store.dispatch('user/login', this.loginForm).then(() => { // Old Vuex direct call
						login(this.loginForm).then(response => {
							// Backend login successful, now commit to Vuex store
							// The backend returns { code, msg, data: { user, token } }
							// We need to dispatch to user/login in Vuex with the data from backend
							// This assumes the Vuex 'user/login' action is updated to accept this payload
							this.$store.dispatch('user/login', response.data).then(() => {
								this.$message.success(response.msg || '登录成功');
								const redirectPath = this.$route.query.redirect || '/dashboard';
								this.$router.push({ path: redirectPath });
								this.loading = false;
							}).catch(vuexError => {
								// Error from Vuex store processing (e.g., setting roles, permissions)
								this.$message.error(vuexError.message || '登录状态处理失败');
								this.loading = false;
							});
						}).catch(error => {
							// Error from backend API call
							// error.response.data might contain { code, msg }
							const errorMsg = error.response && error.response.data && error.response.data.msg ? error.response.data.msg : (error.message || '登录失败');
							this.$message.error(errorMsg);
							this.loading = false;
						});
					}
				})
			}
		}
	}
</script>

<style lang="scss">
	/* 修复input 背景不协调 和光标变色 */
	/* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */

	$bg: #283443;
	$light_gray: #fff;
	$cursor: #fff;

	@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
		.login-container .el-input input {
			color: $cursor;
		}
	}

	/* reset element-ui css */
	.login-container {
		.el-input {
			display: inline-block;
			height: 47px;
			width: 85%;

			input {
				background: transparent;
				border: 0px;
				-webkit-appearance: none;
				border-radius: 0px;
				padding: 12px 5px 12px 15px;
				color: $light_gray;
				height: 47px;
				caret-color: $cursor;

				&:-webkit-autofill {
					box-shadow: 0 0 0px 1000px $bg inset !important;
					-webkit-text-fill-color: $cursor !important;
				}
			}
		}

		.el-form-item {
			border: 1px solid rgba(255, 255, 255, 0.1);
			background: rgba(0, 0, 0, 0.1);
			border-radius: 5px;
			color: #454545;
		}
	}
</style>

<style lang="scss" scoped>
	$bg: #2d3a4b;
	$dark_gray: #889aa4;
	$light_gray: #eee;

	.login-container {
		min-height: 100%;
		width: 100%;
		background-color: $bg;
		overflow: hidden;

		.login-form {
			position: relative;
			width: 520px;
			max-width: 100%;
			padding: 160px 35px 0;
			margin: 0 auto;
			overflow: hidden;
		}

		.tips {
			font-size: 14px;
			color: #fff;
			margin-bottom: 10px;

			span {
				&:first-of-type {
					margin-right: 16px;
				}
			}
		}

		.svg-container {
			padding: 6px 5px 6px 15px;
			color: $dark_gray;
			vertical-align: middle;
			width: 30px;
			display: inline-block;
		}

		.title-container {
			position: relative;

			.title {
				color: $light_gray;
				margin: 0px auto 40px auto;
				text-align: center;
				font-weight: bold;
			}
		}

		.show-pwd {
			position: absolute;
			right: 10px;
			top: 7px;
			font-size: 16px;
			color: $dark_gray;
			cursor: pointer;
			user-select: none;
		}
	}
</style>
