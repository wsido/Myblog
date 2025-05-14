<template>
	<div class="register_container">
		<div class="register_box">
			<!--头像-->
			<div class="avatar_box">
				<img src="/img/avatar.jpg" alt="">
			</div>
			<!--注册表单-->
			<el-form ref="registerFormRef" :model="registerForm" :rules="registerFormRules" class="register_form">
				<el-form-item prop="username">
					<el-input v-model="registerForm.username" prefix-icon="el-icon-user-solid" placeholder="用户名"></el-input>
				</el-form-item>
				<el-form-item prop="password">
					<el-input v-model="registerForm.password" prefix-icon="el-icon-lock" show-password placeholder="密码"></el-input>
				</el-form-item>
				<el-form-item prop="confirmPassword">
					<el-input v-model="registerForm.confirmPassword" prefix-icon="el-icon-lock" show-password placeholder="确认密码"></el-input>
				</el-form-item>
				<el-form-item prop="nickname">
					<el-input v-model="registerForm.nickname" prefix-icon="el-icon-s-custom" placeholder="昵称"></el-input>
				</el-form-item>
				<el-form-item prop="email">
					<el-input v-model="registerForm.email" prefix-icon="el-icon-message" placeholder="邮箱"></el-input>
				</el-form-item>
				<el-form-item class="btns">
					<el-button type="primary" @click="register">注册</el-button>
					<el-button type="info" @click="resetRegisterForm">重置</el-button>
				</el-form-item>
				<el-form-item class="login-link">
					<router-link to="/login">已有账号？立即登录</router-link>
				</el-form-item>
			</el-form>
		</div>
	</div>
</template>

<script>
	import { register } from "@/api/user";

	export default {
		name: "Register",
		data() {
			// 密码确认校验规则
			const validateConfirmPassword = (rule, value, callback) => {
				if (value !== this.registerForm.password) {
					callback(new Error('两次输入的密码不一致'));
				} else {
					callback();
				}
			};
			return {
				registerForm: {
					username: '',
					password: '',
					confirmPassword: '',
					nickname: '',
					email: ''
				},
				registerFormRules: {
					username: [
						{ required: true, message: '请输入用户名', trigger: 'blur' },
						{ min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
					],
					password: [
						{ required: true, message: '请输入密码', trigger: 'blur' },
						{ min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
					],
					confirmPassword: [
						{ required: true, message: '请再次输入密码', trigger: 'blur' },
						{ validator: validateConfirmPassword, trigger: 'blur' }
					],
					nickname: [
						{ required: true, message: '请输入昵称', trigger: 'blur' },
						{ min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
					],
					email: [
						{ required: true, message: '请输入邮箱地址', trigger: 'blur' },
						{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
					]
				}
			}
		},
		methods: {
			resetRegisterForm() {
				this.$refs.registerFormRef.resetFields();
			},
			register() {
				this.$refs.registerFormRef.validate(valid => {
					if (valid) {
						// 提交注册表单，排除确认密码字段
						const { confirmPassword, ...registerData } = this.registerForm;
						register(registerData).then(res => {
							if (res.code === 200) {
								this.msgSuccess(res.msg);
								this.$router.push('/login');
							} else {
								this.msgError(res.msg);
							}
						}).catch(() => {
							this.msgError("注册请求失败");
						});
					}
				});
			}
		}
	}
</script>

<style scoped>
	.register_container {
		box-sizing: unset !important;
		height: 100%;
		background-color: #2b4b6b;
	}

	.register_box {
		width: 450px;
		height: 450px;
		background-color: #fff;
		border-radius: 3px;
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
	}

	.register_box .avatar_box {
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

	.register_box .avatar_box img {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background-color: #eee;
	}

	.register_form {
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
	
	.login-link {
		margin-top: -15px;
		margin-bottom: 10px;
		text-align: center;
	}
	
	.login-link a {
		color: #409EFF;
		text-decoration: none;
	}
</style> 