<template>
    <section class="info-section">
        <h3><i class="fas fa-code"></i> 算法伪代码</h3>
        <div class="pseudo-code-container">
            <pre id="pseudo-code" class="pseudo-code" v-html="algorithmStore.pseudocode"></pre>
        </div>
    </section>
</template>

<script setup>
import { useAlgorithmStore } from '../../stores/algorithm'

const algorithmStore = useAlgorithmStore()
</script>