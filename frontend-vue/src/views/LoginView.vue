<template>
  <div class="login-container">
    <div class="login-card">
      <h2><i class="fas fa-sign-in-alt"></i> {{ isRegister ? '注册' : '登录' }}</h2>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" required placeholder="请输入用户名" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" required placeholder="请输入密码" />
        </div>
        <div class="form-group" v-if="isRegister">
          <label>角色</label>
          <select v-model="role">
            <option value="student">学生</option>
            <option value="teacher">教师</option>
          </select>
        </div>
        <button type="submit" class="btn primary-btn">{{ isRegister ? '注册' : '登录' }}</button>
        <p class="toggle-link" @click="isRegister = !isRegister">
          {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
        </p>
        <p class="toggle-link" @click="$router.push('/')">跳过，直接使用</p>
        <p v-if="error" class="error-msg">{{ error }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const role = ref('student')
const isRegister = ref(false)
const error = ref('')

const handleSubmit = async () => {
  error.value = ''
  const url = isRegister.value ? '/api/auth/register' : '/api/auth/login'
  const fullUrl = url
  try {
    const res = await fetch(fullUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value, role: role.value }),
    })
    const data = await res.json()
    if (data.success) {
      authStore.setAuth(data.token, data.userId, data.username, data.role || 'student')
      router.push('/')
    } else {
      error.value = data.message || '操作失败'
    }
  } catch (e) {
    error.value = '服务器连接失败: ' + e.message
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
}
.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}
.login-card h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #2c3e50;
}
.toggle-link {
  text-align: center;
  margin-top: 12px;
  color: #3498db;
  cursor: pointer;
  font-size: 0.9rem;
}
.error-msg {
  color: #e74c3c;
  text-align: center;
  margin-top: 10px;
}
</style>