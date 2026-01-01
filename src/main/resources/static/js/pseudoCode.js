// 伪代码管理器模块
const PseudoCodeManager = {
    // 初始化伪代码显示
    init: function () {
        // 这个功能已经在Controller中实现
    },

    // 获取算法的伪代码
    getPseudoCode: function (algorithm) {
        const pseudoCodes = {
            insertion: `插入排序伪代码:
1. for i = 1 to n-1:
2.     key = arr[i]
3.     j = i-1
4.     while j >= 0 and arr[j] > key:
5.         arr[j+1] = arr[j]
6.         j = j-1
7.     arr[j+1] = key`,

            shell: `希尔排序伪代码:
1. n = length(arr)
2. gap = n/2
3. while gap > 0:
4.     for i = gap to n-1:
5.         temp = arr[i]
6.         j = i
7.         while j >= gap and arr[j-gap] > temp:
8.             arr[j] = arr[j-gap]
9.             j = j-gap
10.        arr[j] = temp
11.    gap = gap/2`,

            bubble: `冒泡排序伪代码:
1. for i = 0 to n-1:
2.     for j = 0 to n-i-2:
3.         if arr[j] > arr[j+1]:
4.             swap(arr[j], arr[j+1])`,

            quick: `快速排序伪代码:
1. function quickSort(arr, low, high):
2.     if low < high:
3.         pi = partition(arr, low, high)
4.         quickSort(arr, low, pi-1)
5.         quickSort(arr, pi+1, high)
6. 
7. function partition(arr, low, high):
8.     pivot = arr[high]
9.     i = low-1
10.    for j = low to high-1:
11.        if arr[j] < pivot:
12.            i = i+1
13.            swap(arr[i], arr[j])
14.    swap(arr[i+1], arr[high])
15.    return i+1`,

            heap: `堆排序伪代码:
1. function heapSort(arr):
2.     buildMaxHeap(arr)
3.     for i = n-1 downto 1:
4.         swap(arr[0], arr[i])
5.         heapSize = heapSize-1
6.         heapify(arr, 0, heapSize)
7. 
8. function buildMaxHeap(arr):
9.     heapSize = n
10.    for i = floor(n/2) downto 0:
11.        heapify(arr, i, heapSize)
12.
13. function heapify(arr, i, heapSize):
14.    largest = i
15.    left = 2*i+1
16.    right = 2*i+2
17.    if left < heapSize and arr[left] > arr[largest]:
18.        largest = left
19.    if right < heapSize and arr[right] > arr[largest]:
20.        largest = right
21.    if largest != i:
22.        swap(arr[i], arr[largest])
23.        heapify(arr, largest, heapSize)`,

            merge: `归并排序伪代码:
1. function mergeSort(arr, left, right):
2.     if left < right:
3.         mid = floor((left+right)/2)
4.         mergeSort(arr, left, mid)
5.         mergeSort(arr, mid+1, right)
6.         merge(arr, left, mid, right)
7. 
8. function merge(arr, left, mid, right):
9.     n1 = mid-left+1
10.    n2 = right-mid
11.    create L[0..n1] and R[0..n2]
12.    for i=0 to n1-1:
13.        L[i] = arr[left+i]
14.    for j=0 to n2-1:
15.        R[j] = arr[mid+1+j]
16.    i=0, j=0, k=left
17.    while i<n1 and j<n2:
18.        if L[i] <= R[j]:
19.            arr[k] = L[i]
20.            i = i+1
21.        else:
22.            arr[k] = R[j]
23.            j = j+1
24.        k = k+1
25.    while i < n1:
26.        arr[k] = L[i]
27.        i = i+1
28.        k = k+1
29.    while j < n2:
30.        arr[k] = R[j]
31.        j = j+1
32.        k = k+1`
        };

        return pseudoCodes[algorithm] || '伪代码未找到';
    }
};