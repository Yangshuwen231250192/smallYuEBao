const express = require('express');
const path = require('path');
const os = require('os'); // 用于获取网络接口

const app = express();
const port = 3000;

// 获取本机局域网IPv4地址（非回环）
// 获取本机真实局域网 IPv4 地址（严格排除虚拟网卡）
// server.js 修改部分

// 获取本机局域网 IPv4 地址
function getLocalIpAddress() {
    // 【新增】优先检查环境变量 HOST_IP (用于 Docker 场景)
    const envIp = process.env.HOST_IP;
    if (envIp) {
        console.log(`[Docker Mode] Using host IP from environment: ${envIp}`);
        return envIp;
    }

    const interfaces = os.networkInterfaces();
    const candidates = [];

    for (const name of Object.keys(interfaces)) {
        const lowerName = name.toLowerCase();

        // 过滤虚拟网卡 (保留原有逻辑)
        if (lowerName.includes('veth') ||
            lowerName.includes('wsl') ||
            lowerName.includes('hyper-v') ||
            lowerName.includes('vmware') ||
            lowerName.includes('virtualbox') ||
            lowerName.includes('docker')) {
            continue;
        }

        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                const ip = iface.address;
                candidates.push({ name, ip });

                // 优先级：常见局域网段
                if (ip.startsWith('192.168.') ||
                    ip.startsWith('10.') ||
                    (ip.startsWith('172.') && parseInt(ip.split('.')[1]) >= 16 && parseInt(ip.split('.')[1]) <= 31)) {
                    return ip;
                }
            }
        }
    }

    if (candidates.length > 0) {
        return candidates[0].ip;
    }

    return '127.0.0.1';
}

// ... 其他代码保持不变 ...
// server.js 修改部分

// 获取本机局域网 IPv4 地址
function getLocalIpAddress() {
    // 【新增】优先检查环境变量 HOST_IP (用于 Docker 场景)
    const envIp = process.env.HOST_IP;
    if (envIp) {
        console.log(`[Docker Mode] Using host IP from environment: ${envIp}`);
        return envIp;
    }

    const interfaces = os.networkInterfaces();
    const candidates = [];

    for (const name of Object.keys(interfaces)) {
        const lowerName = name.toLowerCase();

        // 过滤虚拟网卡 (保留原有逻辑)
        if (lowerName.includes('veth') ||
            lowerName.includes('wsl') ||
            lowerName.includes('hyper-v') ||
            lowerName.includes('vmware') ||
            lowerName.includes('virtualbox') ||
            lowerName.includes('docker')) {
            continue;
        }

        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                const ip = iface.address;
                candidates.push({ name, ip });

                // 优先级：常见局域网段
                if (ip.startsWith('192.168.') ||
                    ip.startsWith('10.') ||
                    (ip.startsWith('172.') && parseInt(ip.split('.')[1]) >= 16 && parseInt(ip.split('.')[1]) <= 31)) {
                    return ip;
                }
            }
        }
    }

    if (candidates.length > 0) {
        return candidates[0].ip;
    }

    return '127.0.0.1';
}

// ... 其他代码保持不变 ...


// 使用 JSON 解析中间件
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// 初始数据
let balance = 1000;
let yuebao = 0;

// API: 获取当前账户余额
app.get('/api/accounts', (req, res) => {
    res.json({ balance, yuebao });
});

// API: 获取服务器局域网IP
app.get('/api/local-ip', (req, res) => {
    const ip = getLocalIpAddress();
    res.json({ ip });
});

// API: 转入
app.post('/api/transferIn', (req, res) => {
    const { amount } = req.body;
    const num = parseFloat(amount);
    if (isNaN(num) || num <= 0) {
        return res.status(400).json({ error: '金额必须为正数' });
    }
    if (num > balance) {
        return res.status(400).json({ error: '转入金额不能超过余额账户的余额' });
    }
    balance -= num;
    yuebao += num;
    res.json({ balance, yuebao });
});

// API: 转出
app.post('/api/transferOut', (req, res) => {
    const { amount } = req.body;
    const num = parseFloat(amount);
    if (isNaN(num) || num <= 0) {
        return res.status(400).json({ error: '金额必须为正数' });
    }
    if (num > yuebao) {
        return res.status(400).json({ error: '转出金额不能超过余额宝账户的余额' });
    }
    yuebao -= num;
    balance += num;
    res.json({ balance, yuebao });
});

// API: 下一天
app.post('/api/nextDay', (req, res) => {
    yuebao = parseFloat((yuebao * 1.1).toFixed(2));
    res.json({ balance, yuebao });
});

// 启动服务器
app.listen(port, '0.0.0.0', () => {
    console.log(`Server running at http://localhost:${port}`);
    console.log(`Local IP: ${getLocalIpAddress()}`);
});