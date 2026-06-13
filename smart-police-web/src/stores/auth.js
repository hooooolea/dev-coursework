import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    const data = res.data
    token.value = data.accessToken
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      avatar: data.avatar
    }
    permissions.value = data.permissions || []
    roles.value = data.roles || []

    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    return data
  }

  async function logout() {
    try {
      await request.post('/auth/logout')
    } finally {
      token.value = ''
      userInfo.value = null
      permissions.value = []
      roles.value = []
      localStorage.removeItem('accessToken')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')
      localStorage.removeItem('roles')
    }
  }

  function hasPermission(perm) {
    if (roles.value.includes('ROLE_SUPER_ADMIN')) return true
    return permissions.value.includes(perm)
  }

  return { token, userInfo, permissions, roles, isLoggedIn, login, logout, hasPermission }
})
