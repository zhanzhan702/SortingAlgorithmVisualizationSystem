-- ============================================================
-- 排序算法可视化教学与实验数据管理平台 — 初始数据
-- ============================================================

USE sorting_visualization;

-- ----------------------------
-- 管理员用户（密码: admin123，BCrypt 加密）
-- ----------------------------
INSERT INTO users (user_id, username, password_hash, role) VALUES
('a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'admin');

-- ----------------------------
-- 6 种排序算法元数据
-- ----------------------------
INSERT INTO algorithms (algo_code, algo_name, category, time_complexity, space_complexity, is_stable, pseudocode, description, advantages) VALUES
('BUBBLE', '冒泡排序',   'exchange',   'O(n²)',               'O(1)',     TRUE,  'function bubbleSort(arr):\n    for i = 0 to n-1:\n        for j = 0 to n-i-2:\n            if arr[j] > arr[j+1]:\n                swap(arr[j], arr[j+1])', '重复走访数列，依次比较相邻元素并交换顺序错误的元素。', '实现简单，适合教学演示'),
('QUICK', '快速排序',   'exchange',   'O(n log n) ~ O(n²)',  'O(log n)', FALSE, 'function quickSort(arr, low, high):\n    if low < high:\n        pi = partition(arr, low, high)\n        quickSort(arr, low, pi-1)\n        quickSort(arr, pi+1, high)', '分治法：选基准元素，将数组分为小于和大于基准的两部分，递归排序。', '平均性能最好，实际应用最广'),
('INSERTION', '直接插入排序','insertion',  'O(n²)',               'O(1)',     TRUE,  'function insertionSort(arr):\n    for i = 1 to n-1:\n        key = arr[i]\n        j = i-1\n        while j >= 0 and arr[j] > key:\n            arr[j+1] = arr[j]\n            j = j-1\n        arr[j+1] = key', '构建有序序列，将未排序数据逐个插入已排序序列的合适位置。', '小规模数据效率高，基本有序时接近 O(n)'),
('SHELL', '希尔排序',   'insertion',  'O(n log n)',          'O(1)',     FALSE, 'function shellSort(arr):\n    gap = n/2\n    while gap > 0:\n        for i = gap to n-1:\n            temp = arr[i]\n            j = i\n            while j >= gap and arr[j-gap] > temp:\n                arr[j] = arr[j-gap]\n                j = j-gap\n            arr[j] = temp\n        gap = gap/2', '插入排序的改进版，按增量分组进行插入排序，逐步缩小增量。', '中等规模数据效率优于直接插入'),
('HEAP', '堆排序',     'selection',  'O(n log n)',          'O(1)',     FALSE, 'function heapSort(arr):\n    buildMaxHeap(arr)\n    for i = n-1 downto 1:\n        swap(arr[0], arr[i])\n        heapSize--\n        heapify(arr, 0, heapSize)', '利用最大堆数据结构，每次取堆顶（最大值）放到末尾，重新调整堆。', 'O(n log n) 稳定，适合大数据'),
('MERGE', '归并排序',   'merge',      'O(n log n)',          'O(n)',     TRUE,  'function mergeSort(arr, left, right):\n    if left < right:\n        mid = floor((left+right)/2)\n        mergeSort(arr, left, mid)\n        mergeSort(arr, mid+1, right)\n        merge(arr, left, mid, right)', '分治法：递归地将数组分成两半分别排序，再合并两个有序数组。', '稳定、时间复杂度稳定，适合链表排序');
