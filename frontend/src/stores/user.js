import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')

  const user = (() => {
    const userStr = localStorage.getItem('user')
    if (!userStr || userStr === 'undefined' || userStr === 'null') return null
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  })()
  const userRef = ref(user)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(newUser) {
    userRef.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  function logout() {
    token.value = ''
    userRef.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user: userRef, setToken, setUser, logout }
})
