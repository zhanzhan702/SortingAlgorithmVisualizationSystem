import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: true,
  },
  build: {
    outDir:
      'D:/Code/SortingAlgorithmVisualizationSystem/SortingAlgorithmVisualizationSystem/src/main/resources/static', // 相对路径
    emptyOutDir: true,
  },
})
