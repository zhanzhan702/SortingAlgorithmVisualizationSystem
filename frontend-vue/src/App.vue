<template>
    <div>
        <nav class="top-nav" v-if="$route.name !== 'Login'">
            <span>
                <i class="fas fa-user"></i>
                <template v-if="authStore.isLoggedIn">{{ authStore.username }}</template>
                <template v-else>未登录</template>
            </span>
            <span class="nav-links">
                <router-link to="/">可视化</router-link>
                <router-link to="/history">实验历史</router-link>
                <router-link to="/admin" v-if="authStore.isAdmin">管理</router-link>
                <template v-if="authStore.isLoggedIn">
                    <a href="#" @click.prevent="authStore.logout(); $router.push('/login')">退出</a>
                </template>
                <template v-else>
                    <router-link to="/login" class="login-link">登录</router-link>
                </template>
            </span>
        </nav>
        <router-view />
    </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
import { useRoute } from 'vue-router'
const authStore = useAuthStore()
const $route = useRoute()
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
.login-link {
  background: #3498db;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
}
.login-link:hover {
  background: #2980b9;
  color: #fff !important;
}
</style>