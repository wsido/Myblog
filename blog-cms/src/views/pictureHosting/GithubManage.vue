<template>
	<div>
		<el-row>
			<el-select v-model="activeRepos" placeholder="请选择仓库" :filterable="true" @change="changeRepos" style="min-width: 300px">
				<el-option v-for="item in reposList" :key="item.id" :label="item.name" :value="item.name"></el-option>
			</el-select>
			<el-cascader :key="resourceShow" v-model="activePath" placeholder="请选择目录" :options="pathArr" :props="pathProps" :disabled="!activeRepos" style="min-width: 450px"></el-cascader>
			<el-button type="primary" size="medium" icon="el-icon-search" :disabled="!activeRepos" @click="search">查询</el-button>
			<el-switch v-model="isCDN" active-text="CDN" inactive-text="源站" :disabled="!activeRepos"></el-switch>
			<el-button class="right-item" type="primary" size="medium" icon="el-icon-upload" :disabled="!activeRepos" @click="isDrawerShow=!isDrawerShow">上传</el-button>
		</el-row>
		<el-alert title="只显示<img>标签支持的 apng,avif,bmp,gif,ico,cur,jpg,jpeg,jfif,pjpeg,pjp,png,svg,tif,tiff,webp 格式的图片，见 https://developer.mozilla.org/zh-CN/docs/Web/HTML/Element/img" type="warning" show-icon close-text="不再提示" v-if="hintShow1" @close="noDisplay(1)"></el-alert>
		<el-alert title="仅支持获取1000个文件内的目录，超出则将请求失败" type="warning" show-icon close-text="不再提示" v-if="hintShow2" @close="noDisplay(2)"></el-alert>
		<el-alert title="不可上传同名文件，否则将收到422错误码，如需替换，请先删除原文件" type="warning" show-icon close-text="不再提示" v-if="hintShow3" @close="noDisplay(3)"></el-alert>
		<el-row v-viewer>
			<div class="image-container" v-for="(file,index) in fileList" :key="index">
				<el-image :src="imgUrl(file)" fit="scale-down"></el-image>
				<div class="image-content">
					<div class="info">
						<span>{{ file.name }}</span>
					</div>
					<div class="icons">
						<el-tooltip class="item" effect="dark" content="复制CDN链接" placement="bottom">
							<i class="icon el-icon-link" @click="copy(1,file)"></i>
						</el-tooltip>
						<el-tooltip class="item" effect="dark" content="复制MD格式" placement="bottom">
							<SvgIcon icon-class="markdown" class-name="icon" @click="copy(2,file)"></SvgIcon>
						</el-tooltip>
						<i class="icon el-icon-delete" @click="delFile(file)"></i>
					</div>
				</div>
			</div>
		</el-row>

		<el-drawer title="上传文件" :visible.sync="isDrawerShow" direction="rtl" size="40%" :wrapperClosable="false" :close-on-press-escape="false">
			<el-row>
				<el-radio v-model="nameType" label="1">使用源文件名</el-radio>
				<el-radio v-model="nameType" label="2">使用UUID文件名</el-radio>
				<el-button size="small" type="primary" icon="el-icon-upload" v-throttle="[submitUpload,`click`,3000]">确定上传</el-button>
			</el-row>
			<el-row>
				当前目录：{{ realPath }}
			</el-row>
			<el-row>
				<el-switch v-model="isCustomPath" active-text="自定义目录"></el-switch>
				<el-input placeholder="例：oldFolder/newFolder/" v-model="customPath" :disabled="!isCustomPath" size="medium" style="margin-top: 10px"></el-input>
			</el-row>
			<el-upload ref="uploadRef" action="" :http-request="upload" drag multiple :file-list="uploadList" list-type="picture" :auto-upload="false">
				<i class="el-icon-upload"></i>
				<div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
			</el-upload>
		</el-drawer>
	</div>
</template>

<script>
	import { delFile, getReposContents, getUserRepos, upload } from "@/api/github";
import SvgIcon from "@/components/SvgIcon";
import { copy } from "@/util/copy";
import { taskQueue } from "@/util/task-queue";
import { randomUUID } from "@/util/uuid";
import { isImgExt } from "@/util/validate";

	export default {
		name: "GithubManage",
		components: {SvgIcon},
		data() {
			return {
				reposList: [],
				pathArr: [{value: '', label: '根目录'}],
				activeRepos: '',
				activePath: [''],//默认选中根目录
				pathProps: {
					lazy: true,
					checkStrictly: true,
					lazyLoad: async (node, resolve) => {
						let path = node.path.join('/')
						let nodes = []
						await this.getReposContents(nodes, path)
						resolve(nodes)
					}
				},
				hintShow1: true,
				hintShow2: true,
				hintShow3: true,
				fileList: [],
				resourceShow: 0,
				isDrawerShow: false,
				nameType: '1',
				uploadList: [],
				isCDN: true,
				isCustomPath: false,
				customPath: '',
				userInfo: { login: null, avatar_url: null },
			}
		},
		computed: {
			realPath() {
				if (!this.userInfo || !this.userInfo.login || !this.activeRepos) {
					return '（请先选择仓库并确保GitHub配置正确）'; 
				}
				if (this.isCustomPath) {
					return `/${this.userInfo.login}/${this.activeRepos}/${this.customPath}`
				}
				const currentPath = Array.isArray(this.activePath) ? this.activePath.join('/') : '';
				return `/${this.userInfo.login}/${this.activeRepos}${currentPath ? '/' + currentPath : ''}/`
			}
		},
		created() {
			this.hintShow1 = localStorage.getItem('hintShow1') ? false : true
			this.hintShow2 = localStorage.getItem('hintShow2') ? false : true
			this.hintShow3 = localStorage.getItem('hintShow3') ? false : true
			const token = localStorage.getItem('githubToken')
			const storedUserInfo = localStorage.getItem('githubUserInfo')
			if (token && storedUserInfo) {
				try {
					const parsedUserInfo = JSON.parse(storedUserInfo);
					if (parsedUserInfo && parsedUserInfo.login) {
						this.userInfo = parsedUserInfo;
						this.getRepos();
					} else {
						this.userInfo = { login: null, avatar_url: null }; 
						this.msgError('GitHub用户信息配置不完整或已损坏，请重新配置Token');
						this.$router.push('/admin/pictureHosting/setting');
					}
				} catch (e) {
					this.userInfo = { login: null, avatar_url: null };
					this.msgError('GitHub用户信息解析失败，请重新配置Token');
					this.$router.push('/admin/pictureHosting/setting');
				}
			} else {
				this.msgError('请先配置GitHub Token');
				this.$router.push('/admin/pictureHosting/setting');
			}
		},
		methods: {
			getRepos() {
				getUserRepos(this.userInfo.login).then(res => {
					this.reposList = res
				})
			},
			changeRepos() {
				this.resourceShow++
				this.activePath = ['']
				this.fileList = []
			},
			async getReposContents(arr, path) {
				await getReposContents(this.userInfo.login, this.activeRepos, path).then(res => {
					res.forEach(item => {
						if (item.type === 'dir') {
							arr.push({value: item.name, label: item.name, leaf: false})
						}
					})
				})
			},
			search() {
				this.fileList = []
				let path = this.activePath.join('/')
				getReposContents(this.userInfo.login, this.activeRepos, path).then(res => {
					res.forEach(item => {
						if (item.type === 'file' && isImgExt(item.name)) {
							this.fileList.push(item)
						}
					})
				})
			},
			noDisplay(id) {
				localStorage.setItem(`hintShow${id}`, '1')
			},
			imgUrl(file) {
				return this.isCDN ? `https://fastly.jsdelivr.net/gh/${this.userInfo.login}/${this.activeRepos}/${file.path}` : file.download_url
			},
			copy(type, file) {
				let imgUrl = `https://fastly.jsdelivr.net/gh/${this.userInfo.login}/${this.activeRepos}/${file.path}`
				let copyCont = imgUrl
				if (type == 2) {
					copyCont = `![${file.name}](${imgUrl})`
				}
				copy(copyCont)
				this.msgSuccess('复制成功')
			},
			delFile(file) {
				this.$confirm("此操作将永久删除该文件, 是否删除?", "提示", {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning',
				}).then(() => {
					let data = {
						message: 'Delete file via PictureHosting',
						sha: file.sha,
					}
					delFile(this.userInfo.login, this.activeRepos, file.path, data).then(() => {
						this.msgSuccess('删除成功')
						this.search()
					})
				}).catch(() => {
					this.$message({
						type: 'info',
						message: '已取消删除',
					})
				})
			},
			submitUpload() {
				this.uploadList = this.$refs.uploadRef.uploadFiles
				if (this.uploadList.length) {
					this.$refs.uploadRef.submit()
				} else {
					this.msgError('请先选择文件')
				}
			},
			upload(data) {
				let reader = new FileReader()
				reader.readAsDataURL(data.file)
				reader.onload = (() => {
					let base64 = reader.result.split(",")[1]
					let fileName = data.file.name
					if (this.nameType === '2') {
						fileName = randomUUID() + fileName.substr(fileName.lastIndexOf("."))
					}
					taskQueue(() => this.push2Github(data, fileName, base64), 1000)
				})
			},
			push2Github(data, fileName, base64) {
				let requestData = {
					message: "Add files via PictureHosting",
					content: base64,
				}

				let path = this.activePath.join('/')
				if (this.isCustomPath) {
					if (this.customPath === '/') {
						path = ''
					} else {
						path = this.customPath
						if (path.charAt(0) !== '/') {
							path = '/' + path
						}
						if (path.charAt(path.length - 1) === '/') {
							path = path.substring(0, path.length - 1)
						}
					}
				}

				upload(this.userInfo.login, this.activeRepos, path, fileName, requestData).then(() => {
					this.msgSuccess('上传成功')
					data.onSuccess()
				})
			},
		},
	}
</script>

<style>
	.el-drawer__body {
		margin: 0 20px;
		overflow: auto;
	}

	.el-drawer__body .el-upload {
		margin-top: 20px;
	}

	.el-drawer__body .el-upload, .el-drawer__body .el-upload-dragger {
		width: 100%;
	}
</style>

<style scoped>
	.el-row + .el-row, .el-row + .el-alert, .el-alert + .el-row {
		margin-top: 20px;
	}

	.el-alert + .el-alert {
		margin-top: 10px;
	}

	.el-select + .el-cascader, .el-cascader + .el-button, .el-button + .el-switch {
		margin-left: 10px;
	}

	.el-switch + .el-button {
		margin-left: 30px;
	}

	.image-container {
		position: relative;
		width: 200px;
		height: 200px;
		overflow: hidden;
		display: inline-block;
		margin: 0 2px;
	}

	.el-image {
		width: 100%;
		height: 100%;
		z-index: 1;
	}

	.image-content {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
	}

	.image-content .info {
		background: linear-gradient(0deg, transparent, rgba(0, 0, 0, .6));
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		padding: 6px;
		font-size: 12px;
		color: #fff;
		opacity: 0;
		transition: opacity 0.5s;
		z-index: 2;
	}

	.image-content .info span {
		word-wrap: break-word;
	}

	.image-container:hover .image-content .info {
		opacity: 1;
		transition-duration: 0.25s;
	}

	.icons {
		position: absolute;
		right: 8px;
		bottom: 8px;
		z-index: 2;
	}

	.icon {
		text-decoration: none;
		font-size: 22px;
		margin-left: 20px;
		transform: translateY(80px);
		cursor: pointer;
	}

	.image-container:hover .icon {
		transform: translateY(0px);
	}

	.icon:nth-child(1) {
		transition: transform 0.25s 0.05s, color 0.3s;
	}

	.icon:nth-child(2) {
		transition: transform 0.25s 0.1s, color 0.3s;
	}

	.icon:nth-child(3) {
		transition: transform 0.25s 0.15s, color 0.3s;
	}

	.icon:nth-child(1):hover {
		color: #409EFF;
	}

	.icon:nth-child(2):hover {
		color: #409EFF;
	}

	.icon:nth-child(3):hover {
		color: #F56C6C;
	}

	.image-content::before {
		content: '';
		position: absolute;
		left: 0;
		bottom: 0;
		background: rgba(255, 255, 255, 0.7);
		width: 100%;
		height: 0;
		clip-path: polygon(0 100%, 100% 0, 100% 100%);
		transition: 0.3s;
		z-index: 2;
	}

	.image-container:hover .image-content::before {
		height: 80px;
	}
</style>