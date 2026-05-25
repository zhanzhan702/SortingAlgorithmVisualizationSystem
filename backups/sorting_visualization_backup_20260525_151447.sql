-- ========================================
-- 数据库备份: sorting_visualization
-- 导出时间: 2026-05-25T15:14:47.625901500
-- ========================================

USE sorting_visualization;

-- 表: users (1 行)
INSERT INTO users VALUES (User(userId=1, username=admin, passwordHash=admin123, role=admin, email=admin@sortviz.com, createdAt=2026-05-24T22:47:58));

-- 表: algorithms (6 行)
INSERT INTO algorithms VALUES (AlgorithmEntity(algoId=1, algoCode=BUBBLE, algoName=冒泡排序, category=exchange, timeComplexity=O(n²), spaceComplexity=O(1), isStable=true, pseudocode=function bubbleSort(arr):
    for i = 0 to n-1:
        for j = 0 to n-i-2:
            if arr[j] > arr[j+1]:
                swap(arr[j], arr[j+1]), description=重复走访数列，依次比较相邻元素并交换顺序错误的元素。, advantages=实现简单，适合教学演示));
INSERT INTO algorithms VALUES (AlgorithmEntity(algoId=2, algoCode=QUICK, algoName=快速排序, category=exchange, timeComplexity=O(n log n) ~ O(n²), spaceComplexity=O(log n), isStable=false, pseudocode=function quickSort(arr, low, high):
    if low < high:
        pi = partition(arr, low, high)
        quickSort(arr, low, pi-1)
        quickSort(arr, pi+1, high), description=分治法：选基准元素，将数组分为小于和大于基准的两部分，递归排序。, advantages=平均性能最好，实际应用最广));
INSERT INTO algorithms VALUES (AlgorithmEntity(algoId=3, algoCode=INSERTION, algoName=直接插入排序, category=insertion, timeComplexity=O(n²), spaceComplexity=O(1), isStable=true, pseudocode=function insertionSort(arr):
    for i = 1 to n-1:
        key = arr[i]
        j = i-1
        while j >= 0 and arr[j] > key:
            arr[j+1] = arr[j]
            j = j-1
        arr[j+1] = key, description=构建有序序列，将未排序数据逐个插入已排序序列的合适位置。, advantages=小规模数据效率高，基本有序时接近 O(n)));
INSERT INTO algorithms VALUES (AlgorithmEntity(algoId=4, algoCode=SHELL, algoName=希尔排序, category=insertion, timeComplexity=O(n log n), spaceComplexity=O(1), isStable=false, pseudocode=function shellSort(arr):
    gap = n/2
    while gap > 0:
        for i = gap to n-1:
            temp = arr[i]
            j = i
            while j >= gap and arr[j-gap] > temp:
                arr[j] = arr[j-gap]
                j = j-gap
            arr[j] = temp
        gap = gap/2, description=插入排序的改进版，按增量分组进行插入排序，逐步缩小增量。, advantages=中等规模数据效率优于直接插入));
INSERT INTO algorithms VALUES (AlgorithmEntity(algoId=5, algoCode=HEAP, algoName=堆排序, category=selection, timeComplexity=O(n log n), spaceComplexity=O(1), isStable=false, pseudocode=function heapSort(arr):
    buildMaxHeap(arr)
    for i = n-1 downto 1:
        swap(arr[0], arr[i])
        heapSize--
        heapify(arr, 0, heapSize), description=利用最大堆数据结构，每次取堆顶（最大值）放到末尾，重新调整堆。, advantages=O(n log n) 稳定，适合大数据));
INSERT INTO algorithms VALUES (AlgorithmEntity(algoId=6, algoCode=MERGE, algoName=归并排序, category=merge, timeComplexity=O(n log n), spaceComplexity=O(n), isStable=true, pseudocode=function mergeSort(arr, left, right):
    if left < right:
        mid = floor((left+right)/2)
        mergeSort(arr, left, mid)
        mergeSort(arr, mid+1, right)
        merge(arr, left, mid, right), description=分治法：递归地将数组分成两半分别排序，再合并两个有序数组。, advantages=稳定、时间复杂度稳定，适合链表排序));

