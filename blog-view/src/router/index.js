import getPageTitle from '@/util/get-page-title'
import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// 解决Vue Router重复导航错误
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
	return originalPush.call(this, location).catch(err => {
		if (err.name !== 'NavigationDuplicated') throw err
	})
}

const routes = [
	{
		path: '/login',
		component: () => import('@/views/Login'),
		meta: {title: '登录'}
	},
	{
		path: '/register',
		component: () => import('@/views/Register'),
		meta: {title: '注册'}
	},
	{
		path: '/',
		component: () => import('@/views/Index'),
		redirect: '/home',
		children: [
			{
				path: '/home',
				name: 'home',
				component: () => import('@/views/home/Home'),
				meta: {title: '首页'}
			},
			{
				path: '/archives',
				name: 'archives',
				component: () => import('@/views/archives/Archives'),
				meta: {title: '归档'}
			},
			{
				path: '/blog/:id',
				name: 'blog',
				component: () => import('@/views/blog/Blog'),
				meta: {title: '博客'}
			},
			{
				path: '/tag/:name',
				name: 'tag',
				component: () => import('@/views/tag/Tag'),
				meta: {title: '标签'}
			},
			{
				path: '/category/:name',
				name: 'category',
				component: () => import('@/views/category/Category'),
				meta: {title: '分类'}
			},
			{
				path: '/moments',
				name: 'moments',
				component: () => import('@/views/moments/Moments'),
				meta: {title: '动态'}
			},
			{
				path: '/friends',
				name: 'friends',
				component: () => import('@/views/friends/Friends'),
				meta: {title: '友人帐'}
			},
			{
				path: '/about',
				name: 'about',
				component: () => import('@/views/about/About'),
				meta: {title: '关于我'}
			},
			{
				path: '/user/center',
				name: 'userCenter',
				component: () => import('@/views/user/UserCenter'),
				meta: {title: '个人中心', requireAuth: true}
			},
			{
				path: '/user/blogs',
				name: 'userBlogs',
				component: () => import('@/views/user/UserBlogs'),
				meta: {title: '我的博客', requireAuth: true}
			},
			{
				path: '/user/favorites',
				name: 'userFavorites',
				component: () => import('@/views/user/UserFavorites'),
				meta: {title: '我的收藏', requireAuth: true}
			}
		]
	}
]

const router = new VueRouter({
	mode: 'history',
	base: process.env.BASE_URL,
	routes
})

//挂载路由守卫
router.beforeEach((to, from, next) => {
	// 需要用户登录的页面
	if (to.meta.requireAuth) {
		const token = window.localStorage.getItem('userToken')
		if (token) {
			next()
		} else {
			next('/login')
		}
	} else {
		next()
	}
	document.title = getPageTitle(to.meta.title)
})

export default router
