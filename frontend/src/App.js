import React, { useState, useEffect } from 'react';
import LoginPage from './pages/LoginPage';
import AlipayPage from './pages/AlipayPage';
import YuebaoPage from './pages/YuebaoPage';
import { Layout, Button, message } from 'antd';
import { LogoutOutlined } from '@ant-design/icons';
import './App.css';

const { Header, Content } = Layout;

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentPage, setCurrentPage] = useState('alipay'); // alipay 或 yuebao
  const [username, setUsername] = useState('');

  // 检查本地存储的登录状态
  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUsername = localStorage.getItem('username');

    if (token && savedUsername) {
      setIsLoggedIn(true);
      setUsername(savedUsername);
    }
  }, []);

  // 页面切换函数，传递给子组件
  const switchToYuebao = () => setCurrentPage('yuebao');
  const switchToAlipay = () => setCurrentPage('alipay');

  // 登录成功回调
  const handleLogin = () => {
    const savedUsername = localStorage.getItem('username');
    setUsername(savedUsername);
    setIsLoggedIn(true);
  };

  // 退出登录
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setIsLoggedIn(false);
    setUsername('');
    message.success('已退出登录');
  };

  if (!isLoggedIn) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <Layout className="app-layout">
      <Header className="app-header">
        <div className="header-content">
          <div className="header-left">
            <h1 className="app-title">小余额宝</h1>
            <span className="welcome-text">欢迎，{username}</span>
          </div>
          <div className="header-right">
            <Button
              type="primary"
              icon={<LogoutOutlined />}
              onClick={handleLogout}
              size="small"
              className="logout-btn"
            >
              退出
            </Button>
          </div>
        </div>
      </Header>

      <Content className="app-content">
        {/* 根据当前页面显示对应组件，并传递切换函数 */}
        {currentPage === 'alipay' ?
          <AlipayPage onSwitchToYuebao={switchToYuebao} /> :
          <YuebaoPage onSwitchToAlipay={switchToAlipay} />
        }
      </Content>
    </Layout>
  );
}

export default App;