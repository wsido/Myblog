import { asyncRoutes, constantRoutes, userCmsAsyncRoutes } from '@/router'

/**
 * Use meta.role to determine if the current user has permission
 * @param roles the roles of the current user (e.g., ['admin'] or ['user'])
 * @param route the route to check
 */
function hasPermission(roles, route) {
  if (route.meta && route.meta.roles) {
    return roles.some(role => route.meta.roles.includes(role))
  }
  return true // If no roles are defined on the route, it's considered public 
              // (within the specific async set being processed, e.g. userCmsAsyncRoutes are all for 'user')
}

/**
 * Filter asynchronous routing tables by recursion
 * This function is still useful if a role (e.g. 'user') might not have access to ALL routes within userCmsAsyncRoutes
 * or an admin might not have access to ALL routes in asyncRoutes based on more granular permissions.
 * For our current simplified case (admin gets all admin, user gets all userCMS), direct assignment is also possible.
 * However, keeping this allows for future flexibility.
 * @param routes asyncRoutes or userCmsAsyncRoutes
 * @param roles user roles
 */
export function filterAsyncRoutes(routes, roles) {
  const res = []
  routes.forEach(route => {
    const tmp = { ...route }
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, roles)
      }
      res.push(tmp)
    }
  })
  return res
}

const state = {
  routes: [],
  addRoutes: []
}

const mutations = {
  SET_ROUTES: (state, routes) => {
    state.addRoutes = routes // Dynamically added routes
    state.routes = constantRoutes.concat(routes) // Full routes for sidebar
  }
}

const actions = {
  generateRoutes({ commit }, roles) { // roles is an array e.g. ['admin'] or ['user']
    return new Promise(resolve => {
      let accessedRoutes
      console.log('Permission/generateRoutes received roles:', JSON.stringify(roles)); 

      if (roles.includes('admin')) {
        // Admin gets all routes from asyncRoutes AND all routes from userCmsAsyncRoutes.
        const adminSpecificRoutes = asyncRoutes || [];
        const userSelfManagementRoutes = userCmsAsyncRoutes || [];
        // Debugging logs for admin routes
        console.log('Admin routes to be loaded (asyncRoutes):', JSON.stringify(adminSpecificRoutes.map(r => ({ path: r.path, name: r.name }))));
        console.log('UserCMS routes to be loaded (userCmsAsyncRoutes):', JSON.stringify(userSelfManagementRoutes.map(r => ({ path: r.path, name: r.name }))));
        
        accessedRoutes = adminSpecificRoutes.concat(userSelfManagementRoutes);
        // console.log('Admin combined accessed routes before filter (if any needed):', JSON.stringify(accessedRoutes.map(r => ({ path: r.path, name: r.name }))));
        // No filtering is applied here for admin, admin gets all from both sets.
      } else if (roles.includes('user')) {
        // User role (not admin) gets routes from userCmsAsyncRoutes, filtered by their permissions.
        accessedRoutes = filterAsyncRoutes(userCmsAsyncRoutes, roles) || [];
      } else {
        accessedRoutes = [] // No specific routes for other roles or if no roles
      }
      
      console.log('Permission/generateRoutes accessedRoutes paths:', JSON.stringify(accessedRoutes.map(r => r.path)));

      // The 404 page must be the last route added.
      const wildcardRoute = { path: '*', redirect: '/404', hidden: true };
      // Check if wildcard already exists (it might if one of the async arrays somehow included it)
      if (!accessedRoutes.find(r => r.path === '*')) {
         accessedRoutes.push(wildcardRoute);
      }

      commit('SET_ROUTES', accessedRoutes)
      resolve(accessedRoutes)
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 