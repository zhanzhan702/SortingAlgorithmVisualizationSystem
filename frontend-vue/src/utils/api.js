/**
 * 受保护接口的统一请求封装。
 *
 * 后端从 W1 起默认对所有 /api/** 做 JWT 校验，因此前端不能再裸调 fetch：
 * 1. 自动附加 `Authorization: Bearer <token>`
 * 2. 401 时清空本地登录态并跳转登录页
 * 3. 403 时抛出可读错误，页面自行提示
 */
const AUTH_KEYS = ['token', 'userId', 'username', 'role']

export function getToken() {
  return localStorage.getItem('token')
}

export function clearAuth() {
  AUTH_KEYS.forEach((k) => localStorage.removeItem(k))
}

export async function apiFetch(url, options = {}) {
  const headers = { ...(options.headers || {}) }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`
  if (options.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json'

  const res = await fetch(url, { ...options, headers })

  if (res.status === 401) {
    clearAuth()
    if (!window.location.hash.startsWith('#/login')) window.location.hash = '#/login'
    throw new Error('登录已失效，请重新登录')
  }
  if (res.status === 403) {
    throw new Error('没有权限执行该操作')
  }
  return res
}
