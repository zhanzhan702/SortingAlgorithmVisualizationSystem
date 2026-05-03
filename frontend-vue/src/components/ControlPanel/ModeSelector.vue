<template>
    <section class="control-section">
        <h3><i class="fas fa-cogs"></i> 模式选择</h3>
        <div class="mode-selector">
            <button class="mode-btn" :class="{ active: uiStore.currentMode === 'teaching' }"
                @click="uiStore.switchMode('teaching')">
                <i class="fas fa-chalkboard-teacher"></i> 教学模式
            </button>
            <button class="mode-btn" :class="{ active: uiStore.currentMode === 'performance' }"
                @click="uiStore.switchMode('performance')">
                <i class="fas fa-tachometer-alt"></i> 性能模式
            </button>
        </div>
    </section>
</template>

<script setup>
import { useUiStore } from '../../stores/ui'
const uiStore = useUiStore()
</script>