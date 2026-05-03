<template>
    <div class="loading-overlay" v-if="uiStore.isLoading">
        <div class="loading-spinner">
            <i class="fas fa-spinner fa-spin fa-3x"></i>
            <p>{{ uiStore.loadingMessage }}</p>
        </div>
    </div>
</template>

<script setup>
import { useUiStore } from '../../stores/ui'

const uiStore = useUiStore()
</script>