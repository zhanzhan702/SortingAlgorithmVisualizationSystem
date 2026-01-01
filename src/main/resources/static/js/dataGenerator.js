// 数据生成器模块
const DataGenerator = {
    // 生成数据
    generateData: function (size, type, distribution, min, max) {
        if (size <= 0) return [];
        if (min < 0) min = 0;
        let data = [];

        // 根据分布类型生成数据
        switch (distribution) {
            case 'random':
                data = this.generateRandomData(size, min, max);
                break;

            case 'sorted':
                data = this.generateSortedData(size, min, max);
                break;

            case 'reverse':
                data = this.generateReverseData(size, min, max);
                break;

            case 'duplicate':
                data = this.generateDuplicateData(size, min, max);
                break;

            case 'normal':
                data = this.generateNormalData(size, min, max);
                break;

            default:
                data = this.generateRandomData(size, min, max);
        }

        // 根据数据类型转换数据
        if (type === 'int') {
            data = data.map(value => Math.round(value));
        } else if (type === 'double') {
            data = data.map(value => parseFloat(value.toFixed(2)));
        } else if (type === 'Person') {
            data = this.generatePersonData(size);
        }

        return data;
    },

    // 生成随机数据
    generateRandomData: function (size, min, max) {
        const data = [];
        for (let i = 0; i < size; i++) {
            data.push(Math.random() * (max - min) + min);
        }
        return data;
    },

    // 生成有序数据
    generateSortedData: function (size, min, max) {
        const data = [];
        const step = (max - min) / size;

        for (let i = 0; i < size; i++) {
            data.push(min + i * step);
        }

        // 添加少量随机扰动
        for (let i = 0; i < size * 0.1; i++) {
            const index = Math.floor(Math.random() * size);
            const perturbation = (Math.random() - 0.5) * step * 2;
            data[index] = Math.max(min, Math.min(max, data[index] + perturbation));
        }

        return data;
    },

    // 生成逆序数据
    generateReverseData: function (size, min, max) {
        const data = this.generateSortedData(size, min, max);
        return data.reverse();
    },

    // 生成重复数据
    generateDuplicateData: function (size, min, max) {
        const data = [];
        const uniqueValues = Math.max(3, Math.floor(size * 0.3));

        // 先生成一些唯一值
        const uniqueNumbers = [];
        for (let i = 0; i < uniqueValues; i++) {
            uniqueNumbers.push(Math.random() * (max - min) + min);
        }

        // 用这些值填充数组
        for (let i = 0; i < size; i++) {
            const valueIndex = Math.floor(Math.random() * uniqueNumbers.length);
            data.push(uniqueNumbers[valueIndex]);
        }

        return data;
    },

    // 生成正态分布数据
    generateNormalData: function (size, min, max) {
        const mean = (min + max) / 2;
        const stdDev = (max - min) / 6; // 覆盖大部分范围

        const data = [];
        for (let i = 0; i < size; i++) {
            let u = 0, v = 0;
            while (u === 0) u = Math.random();
            while (v === 0) v = Math.random();
            const num = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
            data.push(Math.max(min, Math.min(max, mean + num * stdDev)));
        }
        return data;
    },

    // 生成Person结构体数据（统一的方法）
    generatePersonData: function (size, sortField = 'score') {
        const firstNames = ['张', '王', '李', '赵', '刘', '陈', '杨', '黄', '周', '吴'];
        const lastNames = ['伟', '芳', '娜', '秀英', '敏', '静', '丽', '强', '磊', '洋'];
        const domains = ['gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', '163.com'];

        const data = [];
        const usedScores = new Set();

        for (let i = 0; i < size; i++) {
            const firstName = firstNames[Math.floor(Math.random() * firstNames.length)];
            const lastName = lastNames[Math.floor(Math.random() * lastNames.length)];
            const name = firstName + lastName;
            const age = Math.floor(Math.random() * 50) + 18;
            const email = `${firstName.toLowerCase()}.${lastName.toLowerCase()}@${domains[Math.floor(Math.random() * domains.length)]}`;

            // 生成分数，确保不重复（如果是按分数排序）
            let score = Math.floor(Math.random() * 100);
            if (sortField === 'score') {
                while (usedScores.has(score) && usedScores.size < 100) {
                    score = Math.floor(Math.random() * 100);
                }
                usedScores.add(score);
            }

            data.push({
                id: i + 1,
                name: name,
                age: age,
                email: email,
                score: score
            });
        }

        return data;
    },

    // 获取数据分布选项
    getDistributionOptions: function () {
        return [
            {value: 'random', label: '完全随机'},
            {value: 'sorted', label: '基本有序'},
            {value: 'reverse', label: '完全逆序'},
            {value: 'duplicate', label: '大量重复'},
            {value: 'normal', label: '正态分布'}
        ];
    },

    // 获取数据类型选项
    getDataTypeOptions: function () {
        return [
            {value: 'int', label: '整数'},
            {value: 'double', label: '浮点数'},
            {value: 'Person', label: 'Person结构体'}
        ];
    },

    // 添加获取Person字段的方法
    getPersonFields: function () {
        return [
            {value: 'score', label: '分数', type: 'number'},
            {value: 'age', label: '年龄', type: 'number'},
            {value: 'id', label: 'ID', type: 'number'},
            {value: 'name', label: '姓名', type: 'string'}
        ];
    }
};