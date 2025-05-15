// src/util/auth.js

const TokenKey = 'Admin-Token' // 定义一个 localStorage 中存储 token 的键名

export function getToken() {
  return localStorage.getItem(TokenKey)
}

export function setToken(token) {
  return localStorage.setItem(TokenKey, token)
}

export function removeToken() {
  return localStorage.removeItem(TokenKey)
} 