<template>
    <section class="control-section">
        <h3><i class="fas fa-sort"></i> 排序选项</h3>
        <div class="form-group">
            <label>排序方向</label>
            <div class="sort-direction">
                <label class="radio-label">
                    <input type="radio" name="sort-direction" value="ascending" v-model="comparatorStore.direction" />
                    <span>升序 (从小到大)</span>
                </label>
                <label class="radio-label">
                    <input type="radio" name="sort-direction" value="descending" v-model="comparatorStore.direction" />
                    <span>降序 (从大到小)</span>
                </label>
            </div>
        </div>

        <div class="form-group">
            <label>比较方法</label>
            <select v-model="comparatorStore.method">
                <option value="numeric">数值比较</option>
                <option value="absolute">绝对值比较</option>
                <option value="reverse">反向比较</option>
            </select>
        </div>

        <!-- Person结构体字段选择器（仅当数据类型为Person时显示，由dataStore控制） -->
        <div class="form-group" v-if="dataStore.dataType === 'Person'" id="struct-field-container">
            <label>Person结构体排序字段</label>
            <select class="struct-field-selector" v-model="comparatorStore.structField">
                <option value="score">分数 (score)</option>
                <option value="age">年龄 (age)</option>
                <option value="id">ID (id)</option>
                <option value="name">姓名 (name)</option>
            </select>
        </div>
    </section>
</template>

<script setup>
import { useComparatorStore } from '../../stores/comparator'
import { useDataStore } from '../../stores/data'

const comparatorStore = useComparatorStore()
const dataStore = useDataStore()
</script>