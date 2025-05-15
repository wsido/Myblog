import Layout from '@/layout'
import store from '@/store'
import { getToken } from '@/util/auth'
import getPageTitle from '@/util/get-page-title'
import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// Constant Routes - accessible to all logged-in users
export const constantRoutes = [
	{
		path: '/login',
		component: () => import('@/views/login'),
		meta: {title: '后台管理登录'},
		hidden: true
	},
	{
		path: '/404',
		component: () => import('@/views/404'),
		meta: {title: '404 NOT FOUND'},
		hidden: true
	},
	{
		path: '/',
		component: Layout,
		redirect: '/dashboard',
		children: [
			{
				path: 'dashboard',
				name: 'Dashboard',
				component: () => import('@/views/dashboard'),
				meta: {title: 'Dashboard', icon: 'dashboard'}
			}
		]
	},
	{
		path: '/blog-user', // Path changed for clarity, distinct from admin blog section
		name: 'BlogUserActions',
		component: Layout,
		meta: {title: '我的博客', icon: 'el-icon-edit'}, // User-facing title
		children: [
			{
				path: 'write',
				name: 'WriteMyBlog', // Name changed for clarity
				component: () => import('@/views/blog/blog/WriteBlog'),
				meta: {title: '写文章'}
			},
			{
				// Edit path can remain generic as it's usually navigated to, not directly shown
				path: 'edit/:id',
				name: 'EditMyBlog', // Name changed for clarity
				component: () => import('@/views/blog/blog/WriteBlog'),
				meta: {title: '编辑文章'},
				hidden: true
			},
			{
				path: 'list',
				name: 'MyBlogList', // Name changed for clarity
				component: () => import('@/views/blog/blog/BlogList'),
				meta: {title: '我的文章'} // Title implies user's own articles
			},
			{
				path: 'comments',
				name: 'MyCommentList', // Name changed for clarity
				component: () => import('@/views/blog/comment/CommentList'),
				meta: {title: '我的评论'} // Title implies user's own comments
			}
		]
	},
	{
		path: '/my-system', // Path changed for clarity
		name: 'MySystemActions',
		component: Layout,
		meta: {title: '账户设置', icon: 'el-icon-user-solid'},
		children: [
			{
				path: 'account',
				name: 'MyAccount', // Name changed for clarity
				component: () => import('@/views/system/Account'),
				meta: {title: '修改账户'}
			}
		]
	}
]

// Async Routes - require specific roles/permissions (e.g., admin)
export const asyncRoutes = [
	{
		path: '/admin/blog',
		name: 'BlogAdminOperations',
		component: Layout,
		meta: {title: '博客管理(管理员)', icon: 'el-icon-menu', roles: ['admin'] }, // Expects roles array
		children: [
			// WriteBlog and EditBlog are part of BlogUserActions for general use
			// BlogList and CommentList are part of BlogUserActions for general use
			// Admin specific views or broader scoped lists would go here if different from user views
			// For now, assuming admin uses the same BlogList but sees all data (handled by backend & component logic)
			{
				path: 'moment/write',
				name: 'WriteMomentAdmin',
				component: () => import('@/views/blog/moment/WriteMoment'),
				meta: {title: '写动态', icon: 'el-icon-edit', roles: ['admin']}
			},
			{
				path: 'moment/edit/:id',
				name: 'EditMomentAdmin',
				component: () => import('@/views/blog/moment/WriteMoment'),
				meta: {title: '编辑动态', icon: 'el-icon-edit', roles: ['admin']},
				hidden: true
			},
			{
				path: 'moment/list',
				name: 'MomentListAdmin',
				component: () => import('@/views/blog/moment/MomentList'),
				meta: {title: '动态管理', icon: 'el-icon-chat-dot-round', roles: ['admin']}
			},
			{
				path: 'category/list',
				name: 'CategoryListAdmin',
				component: () => import('@/views/blog/category/CategoryList'),
				meta: {title: '分类管理', icon: 'el-icon-s-opportunity', roles: ['admin']}
			},
			{
				path: 'tag/list',
				name: 'TagListAdmin',
				component: () => import('@/views/blog/tag/TagList'),
				meta: {title: '标签管理', icon: 'biaoqian', roles: ['admin']}
			}
		]
	},
	{
		path: '/admin/page',
		name: 'PageAdminOperations',
		component: Layout,
		meta: {title: '页面管理', icon: 'el-icon-document-copy', roles: ['admin']},
		children: [
			{
				path: 'site',
				name: 'SiteSettingAdmin',
				component: () => import('@/views/page/SiteSetting'),
				meta: {title: '站点设置', icon: 'bianjizhandian', roles: ['admin']}
			},
			{
				path: 'friend',
				name: 'FriendListAdmin',
				component: () => import('@/views/page/FriendList'),
				meta: {title: '友链管理', icon: 'friend', roles: ['admin']}
			},
			{
				path: 'about',
				name: 'AboutAdmin',
				component: () => import('@/views/page/About'),
				meta: {title: '关于我设置', icon: 'el-icon-tickets', roles: ['admin']}
			},
		]
	},
	{
		path: '/admin/pictureHosting',
		name: 'PictureHostingAdmin',
		component: Layout,
		meta: {title: '图床管理', icon: 'el-icon-picture', roles: ['admin']},
		children: [
			{
				path: 'setting',
				name: 'PictureSettingAdmin',
				component: () => import('@/views/pictureHosting/Setting'),
				meta: {title: '图床配置', icon: 'el-icon-setting', roles: ['admin']}
			},
			{
				path: 'github',
				name: 'GithubManageAdmin',
				component: () => import('@/views/pictureHosting/GithubManage'),
				meta: {title: 'GitHub图床', icon: 'el-icon-folder-opened', roles: ['admin']}
			},
			{
				path: 'txyun',
				name: 'TxyunManageAdmin',
				component: () => import('@/views/pictureHosting/TxyunManage'),
				meta: {title: '腾讯云图床', icon: 'el-icon-folder-opened', roles: ['admin']}
			},
		]
	},
	{
		path: '/admin/system',
		name: 'SystemAdminOperations',
		component: Layout,
		meta: {title: '系统管理(管理员)', icon: 'el-icon-s-tools', roles: ['admin']},
		children: [
			// MyAccount is in constantRoutes for user self-service
			{
				path: 'job',
				name: 'JobListAdmin',
				component: () => import('@/views/system/ScheduleJobList'),
				meta: {title: '定时任务', icon: 'el-icon-alarm-clock', roles: ['admin']}
			},
		]
	},
	{
		path: '/admin/log',
		name: 'LogAdmin',
		component: Layout,
		meta: {title: '日志管理', icon: 'el-icon-document', roles: ['admin']},
		children: [
			{
				path: 'job',
				name: 'JobLogAdmin',
				component: () => import('@/views/log/ScheduleJobLog'),
				meta: {title: '任务日志', icon: 'el-icon-alarm-clock', roles: ['admin']}
			},
			{
				path: 'login',
				name: 'LoginLogAdmin',
				component: () => import('@/views/log/LoginLog'),
				meta: {title: '登录日志', icon: 'el-icon-finished', roles: ['admin']}
			},
			{
				path: 'operation',
				name: 'OperationLogAdmin',
				component: () => import('@/views/log/OperationLog'),
				meta: {title: '操作日志', icon: 'el-icon-document-checked', roles: ['admin']}
			},
			{
				path: 'exception',
				name: 'ExceptionLogAdmin',
				component: () => import('@/views/log/ExceptionLog'),
				meta: {title: '异常日志', icon: 'el-icon-document-delete', roles: ['admin']}
			},
			{
				path: 'visit',
				name: 'VisitLogAdmin',
				component: () => import('@/views/log/VisitLog'),
				meta: {title: '访问日志', icon: 'el-icon-data-line', roles: ['admin']}
			},
		]
	},
	{
		path: '/admin/statistics',
		name: 'StatisticsAdmin',
		component: Layout,
		meta: {title: '数据统计', icon: 'el-icon-s-data', roles: ['admin']},
		children: [
			{
				path: 'visitor',
				name: 'VisitorStatisticsAdmin',
				component: () => import('@/views/statistics/Visitor'),
				meta: {title: '访客统计', icon: 'el-icon-s-marketing', roles: ['admin']}
			},
		]
	},
	// 404 page must be placed at the end of all routes, typically added after async routes
	{path: '*', redirect: '/404', hidden: true}
]

const createRouter = () => new VueRouter({
	mode: 'history', // require service support
	base: process.env.BASE_URL,
	scrollBehavior: () => ({ y: 0 }),
	routes: constantRoutes // Initialize with constant routes only
})

const router = createRouter()

export function resetRouter() {
	const newRouter = createRouter()
	router.matcher = newRouter.matcher // reset router
}

router.beforeEach(async (to, from, next) => {
	document.title = getPageTitle(to.meta.title)

	const hasToken = getToken()

	if (hasToken) {
		if (to.path === '/login') {
			next({ path: '/' })
		} else {
			const hasRoles = store.getters.roles && store.getters.roles.length > 0
			if (hasRoles) {
				next()
			} else {
				try {
					const { roles } = await store.dispatch('user/getInfo')

					const accessRoutes = await store.dispatch('permission/generateRoutes', roles)

					router.addRoutes(accessRoutes)

					Vue.nextTick(() => {
						next({ ...to, replace: true })
					})
				} catch (error) {
					await store.dispatch('user/resetToken')
					Vue.prototype.$message.error(error.message || 'Error fetching user info or generating routes')
					next(`/login?redirect=${to.path}`)
				}
			}
		}
	} else {
		if (to.path === '/login') {
			next()
		} else {
			next(`/login?redirect=${to.path}`)
		}
	}
})

export default router
