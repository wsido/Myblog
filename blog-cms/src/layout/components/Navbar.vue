<template>
	<div class="navbar">
		<hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar"/>

		<breadcrumb class="breadcrumb-container"/>

		<div class="right-menu">
			<el-dropdown class="avatar-container" trigger="click">
				<div class="avatar-wrapper">
					<img :src="avatar" class="user-avatar">
				</div>
				<el-dropdown-menu slot="dropdown" class="user-dropdown">
					<a target="_blank" href="https://github.com/wsido/NBlog">
						<el-dropdown-item>
							<SvgIcon icon-class="github" class-name="svg"/>
							<span>GitHub</span>
						</el-dropdown-item>
					</a>
					<el-dropdown-item @click.native="logout">
						<SvgIcon icon-class="logout" class-name="svg"/>
						<span>退出</span>
					</el-dropdown-item>
				</el-dropdown-menu>
			</el-dropdown>
		</div>
	</div>
</template>

<script>
	import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import SvgIcon from '@/components/SvgIcon'
import { mapGetters } from 'vuex'

	export default {
		components: {
			Breadcrumb,
			Hamburger,
			SvgIcon
		},
		data() {
			return {
				// user: null, // No longer needed in data if using getters
			}
		},
		computed: {
			...mapGetters([
				'sidebar',
				'name',    // Add name from Vuex getters
				'avatar'   // Add avatar from Vuex getters
			])
		},
		created() {
			// this.getUserInfo() // No longer needed if using Vuex getters directly in template
		},
		methods: {
			toggleSideBar() {
				this.$store.dispatch('app/toggleSideBar')
			},
			// getUserInfo() { // Method no longer needed
			// 	this.user = JSON.parse(window.localStorage.getItem('userInfo') || null)
			// 	if (!this.user) {
			// 		this.$router.push('/login')
			// 	}
			// },
			async logout() {
				await this.$store.dispatch('user/logout')
				this.$router.push(`/login?redirect=${this.$route.fullPath}`)
				this.msgSuccess('退出成功');
			}
		}
	}
</script>

<style lang="scss" scoped>
	.navbar {
		height: 50px;
		overflow: hidden;
		position: relative;
		background: #fff;
		box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
		user-select: none;

		.hamburger-container {
			line-height: 46px;
			height: 100%;
			float: left;
			cursor: pointer;
			transition: background .3s;
			-webkit-tap-highlight-color: transparent;

			&:hover {
				background: rgba(0, 0, 0, .025)
			}
		}

		.breadcrumb-container {
			float: left;
		}

		.right-menu {
			float: right;
			height: 100%;
			line-height: 50px;

			&:focus {
				outline: none;
			}

			.right-menu-item {
				display: inline-block;
				padding: 0 8px;
				height: 100%;
				font-size: 18px;
				color: #5a5e66;
				vertical-align: text-bottom;

				&.hover-effect {
					cursor: pointer;
					transition: background .3s;

					&:hover {
						background: rgba(0, 0, 0, .025)
					}
				}
			}

			.avatar-container {
				margin-right: 20px;

				.avatar-wrapper {
					margin-top: 5px;
					position: relative;

					.user-avatar {
						cursor: pointer;
						width: 40px;
						height: 40px;
						border-radius: 10px;
					}

					.el-icon-caret-bottom {
						cursor: pointer;
						position: absolute;
						right: -20px;
						top: 0px;
						font-size: 12px;
					}
				}
			}
		}
	}

	.user-dropdown .svg {
		margin-right: 5px;
	}

	.el-dropdown-menu {
		margin: 7px 0 0 0 !important;
		padding: 0 !important;
		border: 0 !important;
	}
</style>
