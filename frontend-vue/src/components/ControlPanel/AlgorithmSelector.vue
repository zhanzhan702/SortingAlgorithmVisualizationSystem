<template>
    <section class="control-section">
        <h3><i class="fas fa-code"></i> 排序算法</h3>
        <div class="algorithm-selector">
            <button v-for="algo in algorithmStore.availableAlgorithms" :key="algo.id" class="algorithm-btn"
                :class="{ active: algorithmStore.currentAlgorithm === algo.id }"
                @click="algorithmStore.selectAlgorithm(algo.id)">
                <div>{{ algo.name }}</div>
                <small>{{ algo.complexity }}</small>
            </button>
        </div>
    </section>
</template>

<script setup>
import { useAlgorithmStore } from '../../stores/algorithm'

const algorithmStore = useAlgorithmStore()
</script>