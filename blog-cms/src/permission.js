import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken } from '@/util/auth' // get token from cookie
import getPageTitle from '@/util/get-page-title'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/login'] // no redirect whitelist

router.beforeEach(async(to, from, next) => {
  // start progress bar
  NProgress.start()

  // set page title
  document.title = getPageTitle(to.meta.title)

  // determine whether the user has logged in
  const hasToken = getToken()

  if (process.env.NODE_ENV === 'development') {
    console.log('Route guard:', { to: to.path, hasToken, roles: store.getters.roles })
  }

  if (hasToken) {
    if (to.path === '/login') {
      // if is logged in, redirect to the home page
      next({ path: '/' })
      NProgress.done()
    } else {
      // determine whether the user has obtained his permission roles
      const hasRoles = store.getters.roles && store.getters.roles.length > 0
      if (hasRoles) {
        next()
      } else {
        try {
          // Note: roles must be an object array! such as: ['admin'] or ,['developer','editor']
          // Since getInfo is removed, roles should be directly available after login.
          // This block now mainly handles the case where a user is logged in (has token) but refreshes the page,
          // clearing Vuex state. In this scenario, we need to re-generate routes.
          // But first, we need user info. The original getInfo is gone, so let's re-add a lightweight version.
          // Re-adding a simplified getInfo dispatch for page refresh scenario.
          const { roles } = await store.dispatch('user/getInfo')

          // generate accessible routes map based on roles
          const accessRoutes = await store.dispatch('permission/generateRoutes', roles)

          // dynamically add accessible routes
          router.addRoutes(accessRoutes)

          // hack method to ensure that addRoutes is complete
          // set the replace: true, so the navigation will not leave a history record
          next({ ...to, replace: true })
        } catch (error) {
          // for debugging
          console.error('Permission error during route generation:', error)
          // remove token and go to login page to re-login
          await store.dispatch('user/resetToken')
          Message.error(error.message || 'Verification failed, please login again.')
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    /* has no token*/

    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next()
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // finish progress bar
  NProgress.done()
}) 