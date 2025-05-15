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
				</el-form-item>
			</el-form>
		</el-card>
	</div>
</template>

<script>
import { changeAccount } from "@/api/account";
import { mapGetters } from 'vuex';

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
				const userUpdatePayload = {
					username: this.accountForm.username,
					password: this.accountForm.password
				};
				const res = await changeAccount(userUpdatePayload);
				this.$message.success(res.msg || '修改成功，请重新登录');
				await this.$store.dispatch('user/logout');
				this.$router.push('/login');
			} catch (error) {
				this.$message.error(error.msg || error.message || '修改失败');
			}
		}
	}
}
</script>

<style scoped>
.el-card {
	width: 50%;
}
</style>
