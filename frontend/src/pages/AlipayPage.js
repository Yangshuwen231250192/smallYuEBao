import React, { useState, useEffect } from 'react';
import { message, Modal } from 'antd';
import { QRCodeSVG } from 'qrcode.react';
import { getAccounts, transferIn, transferOut, getApiBaseUrl } from '../services/api';
import './AlipayPage.css';

function AlipayPage({ onSwitchToYuebao }) {
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
        const response = await fetch(`${getApiBaseUrl()}/ip`);
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
  const handleTransferToYuebao = async () => {
    if (!amount || parseFloat(amount) <= 0) {
      message.warning('请输入有效的正数金额');
      return;
    }

    setLoading(true);
    try {
      await transferIn(amount);
      message.success('✅ 转入余额宝成功！');
      setAmount('');
      fetchData();
    } catch (error) {
      message.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  // 从余额宝转出
  const handleTransferFromYuebao = async () => {
    if (!amount || parseFloat(amount) <= 0) {
      message.warning('请输入有效的正数金额');
      return;
    }

    setLoading(true);
    try {
      await transferOut(amount);
      message.success('✅ 转出到余额成功！');
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

  return (
    <div className="alipay-container">
      {/* 顶部状态栏 - 仿支付宝 */}
      <div className="status-bar">
        <span className="time">{new Date().toLocaleTimeString('zh-CN', { hour12: false })}</span>
        <div className="status-icons">
          <span>📶</span>
          <span>🔋</span>
        </div>
      </div>

      {/* 支付宝头部 */}
      <header className="alipay-header">
        <div className="header-top">
          <h1 className="app-name">支付宝</h1>
          <button className="scan-btn" onClick={handleShowQRCode}>📱</button>
        </div>
        <div className="user-info">
          <span className="user-name">用户账户</span>
          <span className="account-no">**** 8888</span>
        </div>
      </header>

      {/* 余额卡片 - 仿支付宝蓝色 */}
      <div className="balance-card">
        <div className="card-bg"></div>
        <div className="card-content">
          <div className="balance-label">总资产（元）</div>
          <div className="balance-amount">¥ {balance.toFixed(2)}</div>
          <div className="balance-detail">
            <span>余额：¥ {balance.toFixed(2)}</span>
            <span>余额宝：¥ {yuebao.toFixed(2)}</span>
          </div>
        </div>
      </div>

      {/* 简化后的功能入口 - 四个小按键 */}
      <div className="feature-grid-simple">
        <div className="feature-item-small">
          <div className="feature-icon-small">💳</div>
          <div className="feature-name-small">收付款</div>
        </div>
        <div className="feature-item-small" onClick={onSwitchToYuebao}>
          <div className="feature-icon-small">💰</div>
          <div className="feature-name-small">余额宝</div>
        </div>
        <div className="feature-item-small">
          <div className="feature-icon-small">🏦</div>
          <div className="feature-name-small">银行卡</div>
        </div>
        <div className="feature-item-small">
          <div className="feature-icon-small">📊</div>
          <div className="feature-name-small">理财</div>
        </div>
      </div>

      {/* 简化的资金管理面板 - 只保留金额框和两个按钮 */}
      <div className="management-panel-simple">
        <div className="input-group-simple">
          <label>操作金额</label>
          <div className="amount-input-wrapper-simple">
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

        <div className="action-buttons-simple">
          <button
            className="btn-simple btn-primary-simple"
            onClick={handleTransferToYuebao}
            disabled={loading || !amount}
          >
            转入余额宝
          </button>
          <button
            className="btn-simple btn-secondary-simple"
            onClick={handleTransferFromYuebao}
            disabled={loading || !amount}
          >
            余额宝转出
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

      {/* 底部导航栏 */}
      <nav className="bottom-nav">
        <div className="nav-item active">
          <span className="nav-icon">🏠</span>
          <span className="nav-text">首页</span>
        </div>
        <div className="nav-item">
          <span className="nav-icon">💰</span>
          <span className="nav-text">理财</span>
        </div>
        <div className="nav-item">
          <span className="nav-icon">👥</span>
          <span className="nav-text">朋友</span>
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

export default AlipayPage;