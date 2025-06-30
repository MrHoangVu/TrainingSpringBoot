// src/App.vue

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useUserStore } from './stores/user'

const authStore = useAuthStore()
const userStore = useUserStore()

onMounted(() => {
  // Kiểm tra xem người dùng có token hay không (đã đăng nhập từ phiên trước)
  if (authStore.isAuthenticated) {
    // Nếu có, gọi action để lấy thông tin người dùng
    userStore.fetchCurrentUser()
  }
})
</script>

<template>
  <router-view />
</template>