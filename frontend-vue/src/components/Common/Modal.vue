<template>
    <div class="modal" :class="{ active: uiStore.showError }" @click.self="uiStore.hideErrorModal()">
        <div class="modal-content">
            <div class="modal-header">
                <h3><i class="fas fa-exclamation-circle"></i> 错误提示</h3>
                <button class="close-modal" @click="uiStore.hideErrorModal()">&times;</button>
            </div>
            <div class="modal-body">
                <p>{{ uiStore.errorMessage }}</p>
            </div>
            <div class="modal-footer">
                <button class="btn primary-btn" @click="uiStore.hideErrorModal()">确定</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useUiStore } from '../../stores/ui'

const uiStore = useUiStore()
</script>