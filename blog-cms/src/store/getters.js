const getters = {
  sidebar: state => state.app.sidebar,
  device: state => state.app.device,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  name: state => state.user.name,
  roles: state => state.user.roles,
  userId: state => state.user.id,
  isAdmin: state => state.user.roles.includes('admin'),
  isCurrentUserCmsScope: state => state.user.scope === 'currentUserCMS',
  isAdminAllScope: state => state.user.roles.includes('admin') && state.user.scope === 'all',
  permission_routes: state => state.permission.routes
}
export default getters
