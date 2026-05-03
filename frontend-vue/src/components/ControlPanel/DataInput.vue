<template>
    <section class="control-section">
        <h3><i class="fas fa-database"></i> 数据输入</h3>
        <div class="input-methods">
            <div class="method-tabs">
                <button :class="['method-tab', { active: activeTab === 'generate' }]"
                    @click="activeTab = 'generate'">生成数据</button>
                <button :class="['method-tab', { active: activeTab === 'file' }]"
                    @click="activeTab = 'file'">上传文件</button>
                <button :class="['method-tab', { active: activeTab === 'manual' }]"
                    @click="activeTab = 'manual'">手动输入</button>
            </div>

            <!-- 生成数据面板 -->
            <div v-show="activeTab === 'generate'" class="method-panel">
                <div class="form-group">
                    <label>数据规模</label>
                    <input type="range" v-model.number="dataStore.dataSize" min="5" max="100" />
                    <span>{{ dataStore.dataSize }}</span>
                </div>
                <div class="form-group">
                    <label>数据类型</label>
                    <select v-model="dataStore.dataType">
                        <option value="int">整数</option>
                        <option value="double">浮点数</option>
                        <option value="Person">Person结构体</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>数据分布</label>
                    <select v-model="dataStore.dataDistribution">
                        <option value="random">完全随机</option>
                        <option value="sorted">基本有序</option>
                        <option value="reverse">完全逆序</option>
                        <option value="duplicate">大量重复</option>
                        <option value="normal">正态分布</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>数值范围</label>
                    <div class="range-inputs">
                        <input type="number" v-model.number="dataStore.minValue" />
                        <span>到</span>
                        <input type="number" v-model.number="dataStore.maxValue" />
                    </div>
                </div>
                <button class="btn primary-btn" @click="generate">生成数据</button>
            </div>

            <!-- 文件上传面板（完善） -->
            <div v-show="activeTab === 'file'" class="method-panel">
                <div class="form-group">
                    <label>选择文件</label>
                    <input type="file" accept=".txt" @change="onFileSelect" />
                    <div class="file-info" v-if="selectedFile">
                        <div>文件名: {{ selectedFile.name }}</div>
                        <div>文件大小: {{ (selectedFile.size / 1024).toFixed(2) }} KB</div>
                    </div>
                </div>
                <div class="form-group">
                    <label>文件格式说明</label>
                    <div class="format-hint">
                        <p>第一行: 数据个数 (≤100)</p>
                        <p>第二行: 数据类型 (int/double/person)</p>
                        <p>第三行起: 每个数据占一行</p>
                    </div>
                </div>
                <button class="btn primary-btn" @click="parseFile" :disabled="!selectedFile">
                    <i class="fas fa-upload"></i> 解析文件
                </button>
            </div>

            <!-- 手动输入面板 -->
            <div v-show="activeTab === 'manual'" class="method-panel">
                <textarea v-model="manualText" placeholder="每行一个数据..."></textarea>
                <select v-model="manualType">
                    <option value="int">整数</option>
                    <option value="double">浮点数</option>
                </select>
                <button class="btn primary-btn" @click="parseManual">确认输入</button>
            </div>
        </div>
    </section>
</template>

<script setup>
import { ref } from 'vue'
import { useDataStore } from '../../stores/data'
import { FileParser } from '../../utils/fileParser'

const dataStore = useDataStore()
const activeTab = ref('generate')
const manualText = ref('')
const manualType = ref('int')
const selectedFile = ref(null)

const generate = () => {
    dataStore.generateNewData()
}

const onFileSelect = (event) => {
    selectedFile.value = event.target.files[0]
}

const parseFile = async () => {
    if (!selectedFile.value) return
    try {
        const result = await FileParser.parseFile(selectedFile.value)
        if (result.error) {
            alert(result.error)
            return
        }
        if (result.data.length > 100) {
            alert(`文件包含${result.data.length}个数据，可视化最多支持100个`)
            return
        }
        dataStore.setData(result.data, result.dataType)
    } catch (error) {
        alert(`文件解析失败: ${error.message}`)
    }
}

const parseManual = () => {
    const lines = manualText.value.trim().split('\n').filter(l => l.trim())
    const data = []
    for (let line of lines) {
        const num = manualType.value === 'int' ? parseInt(line) : parseFloat(line)
        if (isNaN(num)) {
            alert('无效数据')
            return
        }
        data.push(num)
    }
    if (data.length > 100) {
        alert('最多100个数据')
        return
    }
    dataStore.setData(data, manualType.value)
}
</script>