import React, { useState, useEffect } from 'react';
import { message, Modal } from 'antd';
import { QRCodeSVG } from 'qrcode.react';
import { getAccounts, transferIn, transferOut } from '../services/api';
import './YuebaoPage.css';

function YuebaoPage({ onSwitchToAlipay }) {
  const [balance, setBalance] = useState(0);      // 支付宝余额
  const [yuebao, setYuebao] = useState(0);        // 余额宝金额
  const [amount, setAmount] = useState('');       // 输入金额
  const [loading, setLoading] = useState(false);
  // eslint-disable-next-line no-unused-vars
  const [lastUpdate, setLastUpdate] = useState(new Date());
  const [showQRCode, setShowQRCode] = useState(false);
  const [localIP, setLocalIP] = useState('');

  // 获取访问地址
  useEffect(() => {
    const getAccessURL = async () => {
      // 优先使用环境变量中的IP地址
      if (process.env.REACT_APP_SERVER_IP) {
        setLocalIP(process.env.REACT_APP_SERVER_IP);
        return;
      }

      // 如果没有环境变量，尝试获取后端返回的IP
      try {
        const response = await fetch('http://localhost:8080/api/ip');
        const data = await response.json();
        setLocalIP(data.ip);
      } catch (error) {
        // 如果后端也无法获取，使用当前主机名
        setLocalIP(window.location.hostname);
      }
    };
    getAccessURL();
  }, []);

  // 获取最新数据
  const fetchData = async () => {
    try {
      const data = await getAccounts();
      setBalance(parseFloat(data.balance));
      setYuebao(parseFloat(data.yuebao));
      setLastUpdate(new Date());
    } catch (error) {
      console.error('获取数据失败:', error);
      message.error('数据加载失败，请检查后端服务是否启动');
    }
  };

  useEffect(() => {
    fetchData();
    const timer = setInterval(fetchData, 5000);
    return () => clearInterval(timer);
  }, []);

  // 转入余额宝
  const handleTransferIn = async () => {
    if (!amount || parseFloat(amount) <= 0) {
      message.warning('请输入有效的正数金额');
      return;
    }

    setLoading(true);
    try {
      await transferIn(amount);
      message.success('✅ 转入成功！收益将自动计算');
      setAmount('');
      fetchData();
    } catch (error) {
      message.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  // 转出到余额
  const handleTransferOut = async () => {
    if (!amount || parseFloat(amount) <= 0) {
      message.warning('请输入有效的正数金额');
      return;
    }

    setLoading(true);
    try {
      await transferOut(amount);
      message.success('✅ 转出成功！');
      setAmount('');
      fetchData();
    } catch (error) {
      message.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  // 显示二维码
  const handleShowQRCode = () => {
    setShowQRCode(true);
  };

  // 计算昨日收益
  const yesterdayIncome = yuebao * 0.0005;

  // 计算累计收益（只计算理财收益，不包含本金）
  const totalIncome = yuebao * 0.0005 * 30; // 假设30天累计收益

  return (
    <div className="yuebao-container">
      {/* 状态栏 */}
      <div className="status-bar">
        <span className="time">{new Date().toLocaleTimeString('zh-CN', { hour12: false })}</span>
        <div className="status-icons">
          <span>📶</span>
          <span>🔋</span>
        </div>
      </div>

      {/* 余额宝头部 */}
      <header className="yuebao-header">
        <div className="header-top">
          <button className="back-btn" onClick={onSwitchToAlipay}>
            ←
          </button>
          <h1 className="app-name">余额宝</h1>
          <button className="scan-btn" onClick={handleShowQRCode}>📱</button>
        </div>
        <div className="header-subtitle">让零钱也能生钱</div>
      </header>

      {/* 余额宝资产卡片 - 橙色主题 */}
      <div className="yuebao-asset-card">
        <div className="card-bg"></div>
        <div className="card-content">
          <div className="asset-label">余额宝资产（元）</div>
          <div className="asset-amount">¥ {yuebao.toFixed(2)}</div>
          <div className="asset-stats">
            <div className="stat-item">
              <span className="stat-label">昨日收益</span>
              <span className="stat-value income">+{yesterdayIncome.toFixed(2)}</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">年化收益率</span>
              <span className="stat-value">1.85%</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">累计收益</span>
              <span className="stat-value">+{totalIncome.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* 简化后的快速操作 - 四个小按键 */}
      <div className="quick-actions-simple">
        <div className="action-item-small">
          <div className="action-icon-small">📈</div>
          <div className="action-text-small">基金</div>
        </div>
        <div className="action-item-small" onClick={onSwitchToAlipay}>
          <div className="action-icon-small">💙</div>
          <div className="action-text-small">支付宝</div>
        </div>
        <div className="action-item-small">
          <div className="action-icon-small">📊</div>
          <div className="action-text-small">收益明细</div>
        </div>
        <div className="action-item-small">
          <div className="action-icon-small">⚙️</div>
          <div className="action-text-small">设置</div>
        </div>
      </div>

      {/* 简化的资金操作面板 - 只保留金额框和两个按钮 */}
      <div className="operation-panel-simple">
        <div className="input-section-simple">
          <label>操作金额</label>
          <div className="input-wrapper-simple">
            <span className="currency-simple">¥</span>
            <input
              type="number"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="amount-input-simple"
              step="0.01"
              min="0.01"
            />
          </div>
        </div>

        <div className="operation-buttons-simple">
          <button
            className="btn-simple btn-yuebao-in-simple"
            onClick={handleTransferIn}
            disabled={loading || !amount}
          >
            转入余额宝
          </button>
          <button
            className="btn-simple btn-yuebao-out-simple"
            onClick={handleTransferOut}
            disabled={loading || !amount}
          >
            转出到余额
          </button>
        </div>
      </div>

      {/* 简化的账户信息 */}
      <div className="account-info-simple">
        <div className="info-item-simple">
          <span className="info-icon-simple">💳</span>
          <div className="info-content-simple">
            <div className="info-label-simple">支付宝余额</div>
            <div className="info-value-simple">¥ {balance.toFixed(2)}</div>
          </div>
        </div>
        <div className="info-item-simple">
          <span className="info-icon-simple">💰</span>
          <div className="info-content-simple">
            <div className="info-label-simple">余额宝资产</div>
            <div className="info-value-simple">¥ {yuebao.toFixed(2)}</div>
          </div>
        </div>
      </div>

      {/* 底部导航 */}
      <nav className="yuebao-nav">
        <div className="nav-item active">
          <span className="nav-icon">💰</span>
          <span className="nav-text">余额宝</span>
        </div>
        <div className="nav-item">
          <span className="nav-icon">📊</span>
          <span className="nav-text">收益</span>
        </div>
        <div className="nav-item">
          <span className="nav-icon">📝</span>
          <span className="nav-text">明细</span>
        </div>
        <div className="nav-item">
          <span className="nav-icon">👤</span>
          <span className="nav-text">我的</span>
        </div>
      </nav>

      {/* 二维码弹窗 */}
      <Modal
        title="扫码访问"
        open={showQRCode}
        onCancel={() => setShowQRCode(false)}
        footer={null}
        width={300}
      >
        <div className="qr-content">
          <QRCodeSVG value={`http://${localIP}:3000`} size={200} />
          <p>手机扫描二维码访问</p>
          <p className="qr-url">http://{localIP}:3000</p>
        </div>
      </Modal>
    </div>
  );
}

export default YuebaoPage;