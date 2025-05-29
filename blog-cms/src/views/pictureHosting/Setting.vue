<template>
	<div>
		<el-alert title="图床配置及用法请查看：https://github.com/Naccl/PictureHosting" type="warning" show-icon v-if="hintShow"></el-alert>
		<el-card>
			<div slot="header">
				<span>GitHub配置</span>
			</div>
			<el-row>
				<el-col>
					<el-input placeholder="请输入token进行初始化" v-model="githubToken" :clearable="true" @keyup.native.enter="searchGithubUser" style="min-width: 500px">
						<el-button slot="append" icon="el-icon-search" @click="searchGithubUser">查询</el-button>
					</el-input>
				</el-col>
			</el-row>
			<el-row>
				<el-col>
					<span class="middle">当前用户：</span>
					<el-avatar :size="50" :src="githubUserInfo.avatar_url">User</el-avatar>
					<span class="middle">{{ githubUserInfo.login }}</span>
				</el-col>
			</el-row>
			<el-row>
				<el-col>
					<el-button type="primary" size="medium" icon="el-icon-check" :disabled="!isGithubSave" @click="saveGithub(true)">保存配置</el-button>
					<el-button type="info" size="medium" icon="el-icon-close" @click="saveGithub(false)">清除配置</el-button>
				</el-col>
			</el-row>
		</el-card>

		<!-- 本地存储配置卡片 (如果需要，可以在此添加) -->
		<!-- 
		<el-card>
			<div slot="header">
				<span>本地存储配置</span>
			</div>
			<el-form label-width="100px">
				<el-form-item label="上传URL">
					<el-input v-model="localStoreConfig.uploadUrl" placeholder="/api/admin/upload/image (示例)"></el-input>
				</el-form-item>
				<el-button type="primary" size="medium" icon="el-icon-check" @click="saveLocalStoreConfig(true)">保存配置</el-button>
				<el-button type="info" size="medium" icon="el-icon-close" @click="saveLocalStoreConfig(false)">清除配置</el-button>
			</el-form>
		</el-card>
	 	-->
	</div>
</template>

<script>
import { getUserInfo } from "@/api/github";

export default {
	name: "Setting",
	data() {
		return {
			githubToken: '',
			githubUserInfo: {
				login: '未配置'
			},
			isGithubSave: false,
			hintShow: false,
			// localStoreConfig: { uploadUrl: '' } // 如果添加本地存储，可以在此定义
		}
	},
	computed: {
		// 如果有本地存储的保存按钮启用逻辑，可以在此添加
	},
	created() {
		this.githubToken = localStorage.getItem("githubToken")
		const githubUserInfo = localStorage.getItem('githubUserInfo')
		if (this.githubToken && githubUserInfo) {
			this.githubUserInfo = JSON.parse(githubUserInfo)
			this.isGithubSave = true
		} else {
			this.githubUserInfo = {login: '未配置'}
		}

		// const localStoreConfig = localStorage.getItem('localStoreConfig') // 如果添加本地存储
		// if (localStoreConfig) { // 如果添加本地存储
		// 	this.localStoreConfig = JSON.parse(localStoreConfig) // 如果添加本地存储
		// }

		const userJson = window.localStorage.getItem('user') || '{}'
		const user = JSON.parse(userJson)
		if (userJson !== '{}' && user.role !== 'ROLE_admin') {
			//对于访客模式，增加个提示
			this.hintShow = true
		}
	}
	,
	methods: {
		// 获取用户信息
		searchGithubUser() {
			getUserInfo(this.githubToken).then(res => {
				this.githubUserInfo = res
				this.isGithubSave = true
			})
		}
		,
		saveGithub(save) {
			if (save) {
				localStorage.setItem('githubToken', this.githubToken)
				localStorage.setItem('githubUserInfo', JSON.stringify(this.githubUserInfo))
				this.msgSuccess('保存成功')
			} else {
				localStorage.removeItem('githubToken')
				localStorage.removeItem('githubUserInfo')
				this.msgSuccess('清除成功')
			}
		}
		// ,
		// saveLocalStoreConfig(save) { // 如果添加本地存储
		// 	if (save) {
		// 		localStorage.setItem('localStoreConfig', JSON.stringify(this.localStoreConfig))
		// 		this.msgSuccess('保存成功')
		// 	} else {
		// 		localStorage.removeItem('localStoreConfig')
		// 		this.msgSuccess('清除成功')
		// 	}
		// }
	}
	,
}
</script>

<style scoped>
.el-alert + .el-row, .el-row + .el-row {
	margin-top: 20px;
}

.el-avatar {
	vertical-align: middle;
	margin-right: 15px;
}

.middle {
	vertical-align: middle;
}

.el-card {
	width: 50%;
}

.el-card + .el-card {
	margin-top: 20px;
}
</style>
