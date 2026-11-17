<template>
  <div class="cpw-overlay" v-if="visible" @click.self="$emit('close')">
    <div class="cpw-modal">
      <div class="cpw-header">
        <h3><i class="fas fa-key"></i> 修改密码</h3>
        <button class="cpw-close" @click="$emit('close')">&times;</button>
      </div>
      <div class="cpw-body">
        <form @submit.prevent="submit">
          <div class="cpw-field">
            <label>旧密码</label>
            <input v-model="oldPassword" type="password" required placeholder="请输入旧密码" />
          </div>
          <div class="cpw-field">
            <label>新密码</label>
            <input v-model="newPassword" type="password" required placeholder="至少 6 位" />
          </div>
          <div class="cpw-field">
            <label>确认新密码</label>
            <input v-model="confirmPassword" type="password" required placeholder="再次输入新密码" />
          </div>
          <p v-if="error" class="cpw-error">{{ error }}</p>
          <p v-if="success" class="cpw-success">{{ success }}</p>
          <div class="cpw-actions">
            <button type="button" class="cpw-btn cancel" @click="$emit('close')">取消</button>
            <button type="submit" class="cpw-btn primary" :disabled="submitting">
              {{ submitting ? '提交中...' : '确认修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { apiFetch } from '../../utils/api'

const props = defineProps({
  visible: { type: Boolean, default: false },
})
const emit = defineEmits(['close', 'changed'])

const authStore = useAuthStore()
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const submitting = ref(false)

watch(
  () => props.visible,
  (v) => {
    if (v) {
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
      error.value = ''
      success.value = ''
    }
  }
)

const submit = async () => {
  error.value = ''
  success.value = ''

  if (newPassword.value !== confirmPassword.value) {
    error.value = '两次输入的新密码不一致'
    return
  }
  if (!authStore.userId) {
    error.value = '未登录，无法修改密码'
    return
  }

  submitting.value = true
  try {
    const res = await apiFetch('/api/account/change-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        oldPassword: oldPassword.value,
        newPassword: newPassword.value,
      }),
    })
    const data = await res.json()
    if (data.success) {
      success.value = '密码修改成功，请重新登录'
      setTimeout(() => emit('changed'), 800)
    } else {
      error.value = data.message || '修改失败'
    }
  } catch (e) {
    error.value = '服务器连接失败: ' + e.message
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.cpw-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 3000;
}
.cpw-modal {
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 380px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
.cpw-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}
.cpw-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.05rem;
}
.cpw-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
}
.cpw-close:hover {
  color: #333;
}
.cpw-body {
  padding: 20px;
}
.cpw-field {
  margin-bottom: 14px;
}
.cpw-field label {
  display: block;
  margin-bottom: 6px;
  color: #2c3e50;
  font-size: 0.85rem;
}
.cpw-field input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  box-sizing: border-box;
}
.cpw-error {
  color: #e74c3c;
  font-size: 0.82rem;
  margin: 4px 0;
}
.cpw-success {
  color: #27ae60;
  font-size: 0.82rem;
  margin: 4px 0;
}
.cpw-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}
.cpw-btn {
  padding: 7px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}
.cpw-btn.cancel {
  background: #e9ecef;
  color: #2c3e50;
}
.cpw-btn.primary {
  background: #3498db;
  color: #fff;
}
.cpw-btn.primary:hover {
  background: #2980b9;
}
.cpw-btn:disabled {
  opacity: 0.6;
  cursor: default;
}
</style>
