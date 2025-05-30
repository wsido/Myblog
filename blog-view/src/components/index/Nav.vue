<template>
	<div ref="nav" class="ui fixed inverted stackable pointing menu" :class="{'transparent':$route.name==='home' && clientSize.clientWidth>768}">
		<div class="ui container">
			<router-link to="/">
				<h3 class="ui header item m-blue">{{ blogName }}</h3>
			</router-link>
			<router-link to="/home" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='home'}">
				<i class="home icon"></i>首页
			</router-link>
			<el-dropdown trigger="click" @command="categoryRoute">
				<span class="el-dropdown-link item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='category'}">
					<i class="idea icon"></i>分类<i class="caret down icon"></i>
				</span>
				<el-dropdown-menu slot="dropdown">
					<el-dropdown-item :command="category.name" v-for="(category,index) in categoryList" :key="index">{{ category.name }}</el-dropdown-item>
				</el-dropdown-menu>
			</el-dropdown>
			<router-link to="/archives" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='archives'}">
				<i class="clone icon"></i>归档
			</router-link>
			<router-link to="/moments" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='moments'}">
				<i class="comment alternate outline icon"></i>动态
			</router-link>
			<router-link to="/friends" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='friends'}">
				<i class="users icon"></i>友人帐
			</router-link>
			<router-link to="/about" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='about'}">
				<i class="info icon"></i>关于我
			</router-link>
			<el-autocomplete v-model="queryString" :fetch-suggestions="debounceQuery" placeholder="Search..."
			                 class="right item m-search" :class="{'m-mobile-hide': mobileHide}"
			                 popper-class="m-search-item" @select="handleSelect">
				<i class="search icon el-input__icon" slot="suffix"></i>
				<template slot-scope="{ item }">
					<div class="title">{{ item.title }}</div>
					<span class="content">{{ item.content }}</span>
				</template>
			</el-autocomplete>
			
			<!-- 用户入口下拉菜单 -->
			<template v-if="isLoggedIn">
				<el-dropdown trigger="click" @command="handleUserCommand" class="user-dropdown item" :class="{'m-mobile-hide': mobileHide}">
					<span class="el-dropdown-link">
						<i class="user icon"></i>{{ userInfo.nickname || userInfo.username }}<i class="caret down icon"></i>
					</span>
					<el-dropdown-menu slot="dropdown">
						<el-dropdown-item command="userCenter"><i class="user circle icon"></i> 个人中心</el-dropdown-item>
						<el-dropdown-item command="userBlogs"><i class="file alternate icon"></i> 我的博客</el-dropdown-item>
						<el-dropdown-item command="userFavorites"><i class="star icon"></i> 我的收藏</el-dropdown-item>
						<el-dropdown-item divided command="logout"><i class="sign-out icon"></i> 退出登录</el-dropdown-item>
					</el-dropdown-menu>
				</el-dropdown>
			</template>
			<template v-else>
				<div class="right menu" :class="{'m-mobile-hide': mobileHide}">
					<router-link to="/login" class="item">
						<i class="sign-in icon"></i>登录
					</router-link>
					<router-link to="/register" class="item">
						<i class="user plus icon"></i>注册
					</router-link>
				</div>
			</template>
			
			<button class="ui menu black icon button m-right-top m-mobile-show" @click="toggle">
				<i class="sidebar icon"></i>
			</button>
		</div>
	</div>
</template>

<script>
	import { getSearchBlogList } from "@/api/blog";
import { getUserInfo } from "@/api/user";
import { mapState } from 'vuex';

	export default {
		name: "Nav",
		props: {
			blogName: {
				type: String,
				required: true
			},
			categoryList: {
				type: Array,
				required: true
			},
		},
		data() {
			return {
				mobileHide: true,
				queryString: '',
				queryResult: [],
				timer: null,
				isLoggedIn: false,
				userInfo: {}
			}
		},
		computed: {
			...mapState(['clientSize'])
		},
		watch: {
			//路由改变时，收起导航栏
			'$route.path'() {
				this.mobileHide = true
			}
		},
		created() {
			this.checkLoginStatus()
		},
		mounted() {
			//监听页面滚动位置，改变导航栏的显示
			window.addEventListener('scroll', () => {
				//首页且不是移动端
				if (this.$route.name === 'home' && this.clientSize.clientWidth > 768) {
					if (window.scrollY > this.clientSize.clientHeight / 2) {
						if (this.$refs.nav && this.$refs.nav.classList) {
							this.$refs.nav.classList.remove('transparent')
						}
					} else {
						if (this.$refs.nav && this.$refs.nav.classList) {
							this.$refs.nav.classList.add('transparent')
						}
					}
				}
			})
			//监听点击事件，收起导航菜单
			document.addEventListener('click', (e) => {
				//确保$refs.nav存在后再进行操作
				if (this.$refs.nav) {
					//遍历冒泡
					let flag = this.$refs.nav.contains(e.target)
					//如果导航栏是打开状态，且点击的元素不是Nav的子元素，则收起菜单
					if (!this.mobileHide && !flag) {
						this.mobileHide = true
					}
				}
			})
		},
		methods: {
			toggle() {
				this.mobileHide = !this.mobileHide
			},
			categoryRoute(name) {
				const targetPath = `/category/${name}`;
				if (this.$route.fullPath !== targetPath) {
					this.$router.push(targetPath);
				} else {
					// 可选：如果希望在尝试导航到同一页面时有反馈，可以取消注释下一行
					// console.warn('Navigation to current location was prevented:', targetPath);
				}
			},
			debounceQuery(queryString, callback) {
				this.timer && clearTimeout(this.timer)
				this.timer = setTimeout(() => this.querySearchAsync(queryString, callback), 1000)
			},
			querySearchAsync(queryString, callback) {
				if (queryString == null
						|| queryString.trim() === ''
						|| queryString.indexOf('%') !== -1
						|| queryString.indexOf('_') !== -1
						|| queryString.indexOf('[') !== -1
						|| queryString.indexOf('#') !== -1
						|| queryString.indexOf('*') !== -1
						|| queryString.trim().length > 20) {
					return
				}
				getSearchBlogList(queryString).then(res => {
					if (res.code === 200) {
						this.queryResult = res.data
						if (this.queryResult.length === 0) {
							this.queryResult.push({title: '无相关搜索结果'})
						}
						callback(this.queryResult)
					}
				}).catch(() => {
					this.msgError("请求失败")
				})
			},
			handleSelect(item) {
				if (item.id) {
					this.$router.push(`/blog/${item.id}`)
				}
			},
			checkLoginStatus() {
				const token = window.localStorage.getItem('userToken')
				if (token) {
					this.isLoggedIn = true
					this.fetchUserInfo()
				} else {
					this.isLoggedIn = false
				}
			},
			fetchUserInfo() {
				getUserInfo().then(res => {
					if (res.code === 200) {
						this.userInfo = res.data
					} else {
						// 获取信息失败，可能是token过期
						this.logout()
					}
				}).catch(() => {
					this.logout()
				})
			},
			handleUserCommand(command) {
				if (command === 'logout') {
					this.logout()
				} else if (command === 'userCenter') {
					this.$router.push('/user/center')
				} else if (command === 'userBlogs') {
					this.$router.push('/user/blogs')
				} else if (command === 'userFavorites') {
					this.$router.push('/user/favorites')
				}
			},
			logout() {
				// 先检查是否真的需要登出，避免重复提示
				if (!this.isLoggedIn) return
				
				window.localStorage.removeItem('userToken')
				this.isLoggedIn = false
				this.userInfo = {}
				this.$message.success('退出登录成功')
				// 如果当前在需要登录的页面，重定向到登录页
				if (this.$route.meta.requireAuth) {
					this.$router.push('/login')
				}
			}
		}
	}
</script>

<style>
	.ui.fixed.menu .container {
		width: 1400px !important;
		margin-left: auto !important;
		margin-right: auto !important;
	}

	.ui.fixed.menu {
		transition: .3s ease-out;
	}

	.ui.inverted.pointing.menu.transparent {
		background: transparent !important;
	}

	.ui.inverted.pointing.menu.transparent .active.item:after {
		background: transparent !important;
		transition: .3s ease-out;
	}

	.ui.inverted.pointing.menu.transparent .active.item:hover:after {
		background: transparent !important;
	}

	.el-dropdown-link {
		outline-style: none !important;
		outline-color: unset !important;
		height: 100%;
		cursor: pointer;
	}

	.el-dropdown-menu {
		margin: 7px 0 0 0 !important;
		padding: 0 !important;
		border: 0 !important;
		background: #1b1c1d !important;
	}

	.el-dropdown-menu__item {
		padding: 0 15px !important;
		color: rgba(255, 255, 255, .9) !important;
	}

	.el-dropdown-menu__item:hover {
		background: rgba(255, 255, 255, .08) !important;
	}

	.el-popper .popper__arrow::after {
		content: none !important;
	}

	.popper__arrow {
		display: none !important;
	}

	.m-search {
		min-width: 220px;
		padding: 0 !important;
	}

	.m-search input {
		color: rgba(255, 255, 255, .9);;
		border: 0px !important;
		background-color: inherit;
		padding: .67857143em 2.1em .67857143em 1em;
	}

	.m-search i {
		color: rgba(255, 255, 255, .9) !important;
	}

	.m-search-item {
		min-width: 350px !important;
	}

	.m-search-item li {
		line-height: normal !important;
		padding: 8px 10px !important;
	}

	.m-search-item li .title {
		text-overflow: ellipsis;
		overflow: hidden;
		color: rgba(0, 0, 0, 0.87);
	}

	.m-search-item li .content {
		text-overflow: ellipsis;
		font-size: 12px;
		color: rgba(0, 0, 0, .70);
	}
	
	.user-dropdown {
		margin-left: auto;
	}
</style>
