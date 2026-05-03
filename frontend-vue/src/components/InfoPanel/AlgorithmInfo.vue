<template>
    <section class="info-section">
        <h3><i class="fas fa-info-circle"></i> 算法信息</h3>
        <div class="algorithm-info">
            <h4>{{ info.name }}</h4>
            <p>{{ info.description }}</p>
            <ul>
                <li><strong>时间复杂度:</strong> {{ info.timeComplexity }}</li>
                <li><strong>空间复杂度:</strong> {{ info.spaceComplexity }}</li>
                <li><strong>稳定性:</strong> {{ info.stability }}</li>
                <li><strong>优点:</strong> {{ info.advantages }}</li>
            </ul>
        </div>
    </section>
</template>

<script setup>
import { computed } from 'vue'
import { useAlgorithmStore } from '../../stores/algorithm'

const algorithmStore = useAlgorithmStore()

const infoMap = {
    insertion: {
        name: '直接插入排序',
        description: '通过构建有序序列，对于未排序数据，在已排序序列中从后向前扫描，找到相应位置并插入。',
        timeComplexity: '最坏: O(n²), 平均: O(n²), 最好: O(n)',
        spaceComplexity: 'O(1)',
        stability: '稳定',
        advantages: '实现简单，对小规模数据或基本有序数据效率高'
    },
    shell: {
        name: '希尔排序',
        description: '是插入排序的一种更高效的改进版本，通过将原始数据分成多个子序列分别进行插入排序。',
        timeComplexity: '取决于增量序列，通常 O(n log n)',
        spaceComplexity: 'O(1)',
        stability: '不稳定',
        advantages: '比直接插入排序快，适用于中等规模数据'
    },
    bubble: {
        name: '冒泡排序',
        description: '重复地走访过要排序的数列，一次比较两个元素，如果它们的顺序错误就把它们交换过来。',
        timeComplexity: '最坏: O(n²), 平均: O(n²), 最好: O(n)',
        spaceComplexity: 'O(1)',
        stability: '稳定',
        advantages: '实现简单，适合教学演示'
    },
    quick: {
        name: '快速排序',
        description: '采用分治的思想，通过一趟排序将待排记录分隔成独立的两部分，其中一部分记录的关键字均比另一部分的关键字小。',
        timeComplexity: '最坏: O(n²), 平均: O(n log n), 最好: O(n log n)',
        spaceComplexity: 'O(log n)',
        stability: '不稳定',
        advantages: '平均性能最好，是实际应用中最常用的排序算法'
    },
    heap: {
        name: '堆排序',
        description: '利用堆这种数据结构所设计的一种排序算法，通过构建最大堆或最小堆来实现排序。',
        timeComplexity: 'O(n log n)',
        spaceComplexity: 'O(1)',
        stability: '不稳定',
        advantages: '时间复杂度稳定为O(n log n)，适合大数据排序'
    },
    merge: {
        name: '二路归并排序',
        description: '采用分治法，将已有序的子序列合并，得到完全有序的序列。',
        timeComplexity: 'O(n log n)',
        spaceComplexity: 'O(n)',
        stability: '稳定',
        advantages: '稳定排序，时间复杂度稳定，适合链表排序'
    }
}

const info = computed(() => infoMap[algorithmStore.currentAlgorithm] || infoMap.bubble)
</script>