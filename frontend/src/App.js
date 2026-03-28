import React, { useState } from 'react';
import AlipayPage from './pages/AlipayPage';
import YuebaoPage from './pages/YuebaoPage';
import './App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('alipay'); // alipay 或 yuebao

  // 页面切换函数，传递给子组件
  const switchToYuebao = () => setCurrentPage('yuebao');
  const switchToAlipay = () => setCurrentPage('alipay');

  return (
    <div className="App">
      {/* 根据当前页面显示对应组件，并传递切换函数 */}
      {currentPage === 'alipay' ?
        <AlipayPage onSwitchToYuebao={switchToYuebao} /> :
        <YuebaoPage onSwitchToAlipay={switchToAlipay} />
      }
    </div>
  );
}

export default App;