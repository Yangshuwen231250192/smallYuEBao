import axios from 'axios';

// 动态获取 API 基础地址
const getApiBaseUrl = () => {
  // 判断是否为Docker环境（通过nginx代理）
  // 在Docker容器中，前端和后端都在同一个容器内，通过nginx代理
  if (window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
    // 如果是标准HTTP/HTTPS端口（80/443），使用相对路径通过nginx代理
    if (window.location.port === '' || window.location.port === '80' || window.location.port === '443') {
      return '/api';
    }
    // 如果是其他端口（如3000），可能是开发环境或特殊部署，使用当前主机+8080端口
    const currentHost = window.location.hostname;
    return `http://${currentHost}:8080/api`;
  }

  // 本地开发环境（localhost）
  // 如果是标准开发端口（3000），使用localhost:8080
  if (window.location.port === '3000') {
    return 'http://localhost:8080/api';
  }

  // 其他情况（如Docker环境但访问80端口），使用相对路径
  return '/api';
};

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 获取账户信息
 */
export const getAccounts = async () => {
  try {
    const response = await apiClient.get('/accounts');
    return response.data;
  } catch (error) {
    console.error('获取账户信息失败:', error);
    throw error;
  }
};

/**
 * 转入操作
 */
export const transferIn = async (amount) => {
  try {
    const response = await apiClient.post('/transferIn', { amount });
    return response.data;
  } catch (error) {
    if (error.response && error.response.data) {
      throw new Error(error.response.data.error || '转入失败');
    }
    throw new Error('网络请求失败');
  }
};

/**
 * 转出操作
 */
export const transferOut = async (amount) => {
  try {
    const response = await apiClient.post('/transferOut', { amount });
    return response.data;
  } catch (error) {
    if (error.response && error.response.data) {
      throw new Error(error.response.data.error || '转出失败');
    }
    throw new Error('网络请求失败');
  }
};