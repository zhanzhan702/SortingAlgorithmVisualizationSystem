// 文件解析器模块
const FileParser = {
    // 解析文件
    parseFile: function (file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();

            reader.onload = function (event) {
                try {
                    const content = event.target.result;
                    const result = FileParser.parseContent(content);
                    resolve(result);
                } catch (error) {
                    reject(error);
                }
            };

            reader.onerror = function () {
                reject(new Error('读取文件失败'));
            };

            reader.readAsText(file, 'UTF-8');
        });
    },

    // 解析文件内容
    parseContent: function (content) {
        const lines = content.split('\n').map(line => line.trim()).filter(line => line);

        if (lines.length < 3) {
            throw new Error('文件格式错误：至少需要3行（数据个数、数据类型、至少一个数据）');
        }

        // 第一行：数据个数
        const count = parseInt(lines[0]);
        if (isNaN(count) || count <= 0) {
            throw new Error('第一行必须是正整数，表示数据个数');
        }

        if (count > 100) {
            throw new Error(`数据个数${count}超过100，可视化最多支持100个`);
        }

        // 第二行：数据类型
        const dataType = lines[1].toLowerCase();
        if (!['int', 'double', 'person'].includes(dataType)) {
            throw new Error('第二行必须是 int、double 或 Person（结构体名称）');
        }

        // 检查数据行数
        if (lines.length - 2 !== count) {
            throw new Error(`数据个数不匹配：声明了${count}个数据，实际有${lines.length - 2}个`);
        }

        // 解析数据
        const data = [];
        let hasError = false;
        let errorMessage = '';

        for (let i = 0; i < count; i++) {
            const line = lines[i + 2];

            try {
                if (dataType === 'int') {
                    const num = parseInt(line);
                    if (isNaN(num)) {
                        hasError = true;
                        errorMessage = `第${i + 3}行不是有效的整数: ${line}`;
                        break;
                    }
                    data.push(num);
                } else if (dataType === 'double') {
                    const num = parseFloat(line);
                    if (isNaN(num)) {
                        hasError = true;
                        errorMessage = `第${i + 3}行不是有效的浮点数: ${line}`;
                        break;
                    }
                    data.push(num);
                } else if (dataType === 'person') {
                    // 解析JSON格式的结构体
                    try {
                        const obj = JSON.parse(line);
                        // 验证Person结构体格式
                        if (!obj.id || !obj.name || !obj.age || !obj.email || obj.score === undefined) {
                            hasError = true;
                            errorMessage = `第${i + 3}行Person数据格式不正确: ${line}`;
                            break;
                        }
                        data.push(obj);
                    } catch (e) {
                        hasError = true;
                        errorMessage = `第${i + 3}行不是有效的JSON格式: ${line}`;
                        break;
                    }
                }
            } catch (error) {
                hasError = true;
                errorMessage = `解析第${i + 3}行时出错: ${error.message}`;
                break;
            }
        }

        if (hasError) {
            throw new Error(errorMessage);
        }

        return {
            count: count,
            dataType: dataType,
            data: data
        };
    },

    // 验证文件格式
    validateFileFormat: function (file) {
        return new Promise((resolve) => {
            const reader = new FileReader();

            reader.onload = function (event) {
                try {
                    const content = event.target.result;
                    const lines = content.split('\n').map(line => line.trim()).filter(line => line);

                    if (lines.length < 3) {
                        resolve({valid: false, message: '文件至少需要3行'});
                        return;
                    }

                    const count = parseInt(lines[0]);
                    if (isNaN(count) || count <= 0) {
                        resolve({valid: false, message: '第一行必须是正整数'});
                        return;
                    }

                    if (count > 100) {
                        resolve({valid: false, message: `数据个数${count}超过100，可视化最多支持100个`});
                        return;
                    }

                    const dataType = lines[1].toLowerCase();
                    if (!['int', 'double', 'person'].includes(dataType)) {
                        resolve({valid: false, message: '第二行必须是 int、double 或 Person'});
                        return;
                    }

                    if (lines.length - 2 !== count) {
                        resolve({valid: false, message: `数据个数不匹配：声明${count}个，实际${lines.length - 2}个`});
                        return;
                    }

                    resolve({valid: true, count: count, dataType: dataType});
                } catch (error) {
                    resolve({valid: false, message: error.message});
                }
            };

            reader.onerror = function () {
                resolve({valid: false, message: '读取文件失败'});
            };

            // 只读取前几行进行验证
            const blob = file.slice(0, 5000); // 读取前5KB
            reader.readAsText(blob, 'UTF-8');
        });
    }
};