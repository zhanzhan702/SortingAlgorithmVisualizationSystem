<template>
    <div>
        <nav class="top-nav" v-if="authStore.isLoggedIn">
            <span><i class="fas fa-user"></i> {{ authStore.username }}</span>
            <span class="nav-links">
                <router-link to="/">可视化</router-link>
                <router-link to="/history">实验历史</router-link>
                <router-link to="/admin" v-if="authStore.isAdmin">管理</router-link>
                <a href="#" @click.prevent="authStore.logout(); $router.push('/login')">退出</a>
            </span>
        </nav>
        <router-view />
    </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
</script>

<style scoped>
.top-nav {
  background: #2c3e50;
  color: white;
  padding: 8px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}
.nav-links a {
  color: #ecf0f1;
  margin-left: 15px;
  text-decoration: none;
}
.nav-links a:hover { color: #3498db; }
.nav-links a.router-link-active { color: #3498db; font-weight: bold; }
</style>