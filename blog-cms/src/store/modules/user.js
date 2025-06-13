import { getInfo as getUserInfoFromApi, login as loginApi } from '@/api/login'; // Import both APIs
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
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      loginApi({ username: username.trim(), password: password }).then(response => {
        const { data } = response // In backend, data contains { token, user }
        commit('SET_TOKEN', data.token)
        setToken(data.token)
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getUserInfoFromApi(state.token).then(response => {
        const { data } = response

        if (!data) {
          return reject('Verification failed, please Login again.')
        }

        const { role, type, nickname, username, avatar, id } = data

        // roles must be a non-empty array
        const finalRoles = type ? [type.toLowerCase()] : (role ? [role.toLowerCase().replace('role_', '')] : [])
        if (!finalRoles || finalRoles.length <= 0) {
          reject('getInfo: roles must be a non-null array!')
        }

        commit('SET_ROLES', finalRoles)
        commit('SET_NAME', nickname || username)
        commit('SET_AVATAR', avatar || '')
        commit('SET_ID', id)
        resolve({ roles: finalRoles })
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      removeToken() // must remove  token  first
      resetRouter()
      commit('RESET_STATE')
      resolve()
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken() // must remove  token  first
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