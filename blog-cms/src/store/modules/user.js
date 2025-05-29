import { getInfo as getUserInfoFromApi } from '@/api/login'; // Renamed to avoid conflict
import { resetRouter } from '@/router';
import { getToken, removeToken, setToken } from '@/util/auth'; // Assuming auth utility functions exist for token handling

const getDefaultState = () => {
  return {
    token: getToken(),
    id: '', // Added user ID
    name: '',
    avatar: '',
    roles: [] // Will store roles like ['admin'] or ['user']
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_ID: (state, id) => { // Added SET_ID mutation
    state.id = id
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  }
}

const actions = {
  // user login
  // The 'loginData' is what the backend login endpoint returns: { user: {id, username, nickname, avatar, role, type}, token: '... '}
  login({ commit }, loginData) {
    return new Promise((resolve, reject) => {
      const { token, user } = loginData; // Destructure from backend response
      if (!token || !user || !user.username) {
        return reject(new Error('Login data from backend is invalid.'));
      }
      
      commit('SET_TOKEN', token);
      setToken(token);
      
      // Assuming user.role from backend is like 'ROLE_admin' or 'ROLE_user'
      // And user.type is like 'admin' or 'user' as derived by backend or directly present
      const roles = user.role ? [user.role] : (user.type ? [user.type] : []); 
      // Prefer user.type for CMS if available and simple (e.g. 'admin', 'user')
      // Or derive from user.role. The key is consistency with permission/generateRoutes
      // For now, let's assume user.type is what we need for roles array like ['admin']
      const finalRoles = user.type ? [user.type.toLowerCase()] : (user.role ? [user.role.toLowerCase().replace('role_', '')] : []);

      commit('SET_ID', user.id);
      commit('SET_ROLES', finalRoles);
      commit('SET_NAME', user.nickname || user.username);
      commit('SET_AVATAR', user.avatar || '');
      resolve();
    })
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getUserInfoFromApi().then(response => {
        // Backend /admin/user returns Result { code, msg, data: UserObject }
        const { data } = response; // data here is the UserObject from backend
        console.log('User/getInfo backend response data:', JSON.stringify(data)); // DEBUG ADDED
        if (!data || !data.username || !data.id) {
          return reject('Verification failed, please Login again.');
        }

        // Backend user object has: id, username, nickname, avatar, email, role, type
        // The `roles` array for Vuex should be like ['admin'] or ['user']
        const roles = data.type ? [data.type.toLowerCase()] : (data.role ? [data.role.toLowerCase().replace('role_', '')] : []);
        console.log('User/getInfo determined roles:', JSON.stringify(roles)); // DEBUG ADDED

        commit('SET_ID', data.id);
        commit('SET_ROLES', roles);
        commit('SET_NAME', data.nickname || data.username);
        commit('SET_AVATAR', data.avatar || '');
        resolve({ roles: roles, id: data.id, name: data.nickname || data.username, avatar: data.avatar || '' }); // Return user data for permission check
      }).catch(error => {
        reject(error.response?.data?.msg || error.message || 'Failed to fetch user info');
      });
    });
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      removeToken()
      window.localStorage.removeItem('userInfo');
      resetRouter()
      commit('RESET_STATE')
      resolve()
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken()
      commit('RESET_STATE')
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 