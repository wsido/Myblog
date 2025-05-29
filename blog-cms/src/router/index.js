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
		// redirect: '/dashboard/index', // Redirect will be handled by permission.js for admin
		// For user, it will be to the first route in userCmsAsyncRoutes
		// No explicit redirect here, let router guards handle it based on role.
	}
	// User self-service routes moved to userCmsAsyncRoutes
]

// User CMS Async Routes - for USER role (creator/self-management)
export const userCmsAsyncRoutes = [
	{
		path: '/my-dashboard', // Optional: a dedicated dashboard for users
		component: Layout,
		meta: { title: '我的主页', icon: 'el-icon-user', roles: ['user'] },
		redirect: '/my-blog/article-list', // Redirect to a default user page
		children: [
			{
				path: 'index',
				name: 'MyDashboard',
				// component: () => import('@/views/user-dashboard/index'), // Create this component if needed
				// For now, let's assume my-blog/article-list is the primary landing spot for users
				// So this route might not be strictly necessary if /my-blog is the main focus.
				// If we want a distinct dashboard, we create the component.
				// For now, we can make this a simple redirect container or omit if not needed.
				// Let's redirect from /my-dashboard to /my-blog/article-list to make /my-blog the primary content area.
				// This makes the parent /my-dashboard essentially a grouping for a potential future user dashboard page.
				// For now, it mainly serves to group user routes and provide a top-level menu item if not redirecting immediately.
				// Given the redirect, the 'index' child might not even be rendered.
				// A more direct approach might be to have /my-blog as a top-level item in userCmsAsyncRoutes.
				// Let's simplify and make /my-blog the top-level for now.
				// This means removing /my-dashboard and promoting /my-blog and /account-settings.
				component: () => import('@/views/blog/blog/BlogList'), // Placeholder, will be redirected
				meta: { title: '概览', roles: ['user'] },
				hidden: true // Hide if it's just a redirect target
			}
		]
	},
	{
		path: '/my-blog',
		name: 'MyBlog',
		component: Layout,
		meta: {title: '我的博客', icon: 'el-icon-edit-outline', roles: ['user', 'admin'] },
		redirect: '/my-blog/article-list', // Default to article list
		children: [
			{
				path: 'write-article',
				name: 'WriteMyArticle',
				component: () => import('@/views/blog/blog/WriteBlog'),
				meta: {title: '写文章', scope: 'currentUserCMS', roles: ['user', 'admin']}
			},
			{
				path: 'edit-article/:id',
				name: 'EditMyArticle',
				component: () => import('@/views/blog/blog/WriteBlog'),
				meta: {title: '编辑文章', scope: 'currentUserCMS', roles: ['user', 'admin']},
				hidden: true
			},
			{
				path: 'article-list',
				name: 'MyArticleList',
				component: () => import('@/views/blog/blog/BlogList'),
				meta: {title: '我的文章', scope: 'currentUserCMS', roles: ['user', 'admin']}
			},
			{
				path: 'moment/write',
				name: 'WriteMyMoment',
				component: () => import('@/views/blog/moment/WriteMoment'),
				meta: {title: '写动态', scope: 'currentUserCMS'}
			},
			{
				path: 'moment/edit/:id',
				name: 'EditMyMoment',
				component: () => import('@/views/blog/moment/WriteMoment'),
				meta: {title: '编辑动态', scope: 'currentUserCMS'},
				hidden: true
			},
			{
				path: 'moment/list',
				name: 'MyMomentList',
				component: () => import('@/views/blog/moment/MomentList'),
				meta: {title: '我的动态', scope: 'currentUserCMS'}
			},
			{
				path: 'comment-list',
				name: 'MyCommentList',
				component: () => import('@/views/blog/comment/CommentList'),
				meta: {title: '我的评论', scope: 'currentUserCMS'}
			}
		]
	},
	{
		path: '/account-settings',
		name: 'AccountSettings',
		component: Layout,
		meta: {title: '账户设置', icon: 'el-icon-user-solid', roles: ['user'] },
		redirect: '/account-settings/profile', // Default to profile
		children: [
			{
				path: 'profile',
				name: 'MyProfile',
				component: () => import('@/views/system/Account'),
				meta: {title: '修改账户'}
			}
		]
	}
]

// Async Routes - for ADMIN users
export const asyncRoutes = [
	{ 
		path: '/dashboard', 
		component: Layout, 
		meta: { roles: ['admin'] }, // Role guard at parent level for dashboard
		redirect: '/dashboard/index', // Ensure a default child is shown
		children: [
			{
				path: 'index', // Path for the actual dashboard page
				name: 'DashboardAdmin', 
				component: () => import('@/views/dashboard'),
				meta: {title: 'Dashboard', icon: 'dashboard', roles: ['admin']}
			}
		]
	},
	// ... (other admin routes like /admin/content-management, /admin/page, etc., ensure they all have meta: { roles: ['admin'] })
	{
		path: '/admin/content-management', 
		name: 'ContentManagementAdmin',    
		component: Layout,
		redirect: '/admin/content-management/article-list-all', 
		meta: {title: '内容管理(管理员)', icon: 'el-icon-s-management', roles: ['admin'] },
		children: [
			{
				path: 'article-list-all', 
				name: 'ArticleListAdminAll',
				component: () => import('@/views/blog/blog/BlogList'),
				meta: {title: '文章管理(全站)', icon: 'el-icon-document-copy', roles: ['admin'], scope: 'all'}
			},
			{
				path: 'moment-list-all', 
				name: 'MomentListAdminAll',
				component: () => import('@/views/blog/moment/MomentList'),
				meta: {title: '动态管理(全站)', icon: 'el-icon-chat-line-round', roles: ['admin'], scope: 'all'}
			},
			{
				path: 'comment-list-all',
				name: 'CommentListAdminAll',
				component: () => import('@/views/blog/comment/CommentList'),
				meta: {title: '评论管理(全站)', icon: 'el-icon-s-comment', roles: ['admin'], scope: 'all'}
			},
			{
				path: 'category-list', 
				name: 'CategoryListAdmin', 
				component: () => import('@/views/blog/category/CategoryList'),
				meta: {title: '分类管理', icon: 'el-icon-s-opportunity', roles: ['admin']}
			},
			{
				path: 'tag-list', 
				name: 'TagListAdmin', 
				component: () => import('@/views/blog/tag/TagList'),
				meta: {title: '标签管理', icon: 'biaoqian', roles: ['admin']}
			},
			{
				path: 'edit-article-all/:id', 
				name: 'EditArticleAdminAll',
				component: () => import('@/views/blog/blog/WriteBlog'),
				meta: {title: '编辑全站文章', scope: 'all', roles: ['admin']},
				hidden: true
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
			}
		]
	},
	{
		path: '/admin/system',
		name: 'SystemAdminOperations',
		component: Layout,
		meta: {title: '系统管理(管理员)', icon: 'el-icon-s-tools', roles: ['admin']},
		children: [
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
		meta: {title: '日志管理', icon: 'el-icon-notebook-2', roles: ['admin']},
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
				meta: {title: '访问日志', icon: 'el-icon-data-analysis', roles: ['admin']}
			}
		]
	},
	// IMPORTANT: 404 page must be the last route to be added if it's part of dynamic routes
	// However, it's usually better to have it in constantRoutes or handled globally.
	// If it's dynamically added, ensure it's the VERY LAST item in the *final* list of routes added by router.addRoutes()
	// For now, we'll manage the wildcard in permission.js to ensure it's appended correctly after all other dynamic routes.
	// {path: '*', redirect: '/404', hidden: true} // Moved to permission.js logic
];

const createRouter = () => new VueRouter({
	// mode: 'history', // require service support
	scrollBehavior: () => ({y: 0}),
	routes: constantRoutes // Initially, only constant routes are loaded
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
	const newRouter = createRouter()
	router.matcher = newRouter.matcher // reset router
}

// Add this line to export userCmsAsyncRoutes
export { userCmsAsyncRoutes as defaultUserCmsRoutes } // Optional: alias it if 'userCmsAsyncRoutes' is too generic

export default router;

// Global navigation guard
router.beforeEach(async (to, from, next) => {
	// set page title
	document.title = getPageTitle(to.meta.title)

	const hasToken = getToken()

	if (hasToken) {
		if (to.path === '/login') {
			// if is logged in, redirect to the home page
			// Determine home page based on role
			const roles = store.getters.roles && store.getters.roles.length > 0 ? store.getters.roles : []
			if (roles.includes('admin')) {
				next({path: '/dashboard/index'})
			} else if (roles.includes('user')) {
				next({path: '/my-blog/article-list'})
			} else {
				next({path: '/'}) // Fallback, though should ideally not happen if roles are set
			}
		} else {
			const hasRoles = store.getters.roles && store.getters.roles.length > 0
			if (hasRoles) {
				next()
			} else {
				try {
					// get user info, including roles
					const {roles} = await store.dispatch('user/getInfo')
					// generate accessible routes map based on roles
					const accessRoutes = await store.dispatch('permission/generateRoutes', roles)
					// dynamically add accessible routes
					router.addRoutes(accessRoutes)
					// hack method to ensure addRoutes is complete
					// set the replace: true, so the navigation will not leave a history record
					next({...to, replace: true})
				} catch (error) {
					// remove token and go to login page to re-login
					await store.dispatch('user/resetToken')
					// Message.error(error || 'Has Error')
					next(`/login?redirect=${to.path}`)
				}
			}
		}
	} else {
		// has no token
		if (to.path === '/login') {
			next()
		} else {
			next(`/login?redirect=${to.path}`)
		}
	}
})

// ... (existing code, if any, after beforeEach) ...
