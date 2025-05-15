<template>
	<div :class="{'has-logo':showLogo}" class="sidebar-no-select">
		<logo v-if="showLogo" :collapse="isCollapse"/>
		<el-scrollbar wrap-class="scrollbar-wrapper">
			<el-menu
					:default-openeds="defaultOpeneds"
					:default-active="activeMenu"
					:collapse="isCollapse"
					:background-color="variables.menuBg"
					:text-color="variables.menuText"
					:unique-opened="false"
					:active-text-color="variables.menuActiveText"
					:collapse-transition="false"
					mode="vertical"
			>
				<sidebar-item v-for="route in permission_routes" :key="route.path" :item="route" :base-path="route.path"/>
			</el-menu>
		</el-scrollbar>
	</div>
</template>

<script>
	import variables from '@/assets/styles/variables.scss'
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'

	export default {
		components: {SidebarItem, Logo},
		data() {
			return {
				//展开所有父级菜单
				defaultOpeneds: this.$store.state.settings.defaultOpeneds
			}
		},
		computed: {
			...mapGetters([
				'sidebar',
				// 'permission_routes' // We will define it locally to filter
			]),
			permission_routes() {
				console.log('Vuex permission_routes:', JSON.parse(JSON.stringify(this.$store.getters.permission_routes)));
				const filteredRoutes = this.$store.getters.permission_routes.filter(route => route.path !== '*');
				// console.log('Sidebar filtered_routes:', JSON.parse(JSON.stringify(filteredRoutes)));
				return filteredRoutes;
			},
			activeMenu() {
				const route = this.$route
				const {meta, path} = route
				// if set path, the sidebar will highlight the path you set
				if (meta.activeMenu) {
					return meta.activeMenu
				}
				return path
			},
			showLogo() {
				return this.$store.state.settings.sidebarLogo
			},
			variables() {
				return variables
			},
			isCollapse() {
				return !this.sidebar.opened
			}
		}
	}
</script>

<style scoped>
	.sidebar-no-select {
		user-select: none;
	}
</style>