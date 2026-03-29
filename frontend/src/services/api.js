import axios from 'axios';

// 动态获取 API 基础地址
const getApiBaseUrl = () => {
  // 获取当前访问的主机名和端口
  const { hostname, port } = window.location;

  // 如果是本地开发环境
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8080/api';
  }

  // 如果是通过IP地址访问（手机访问场景）
  // 使用相同的主机名，但后端端口是8080
  if (hostname && hostname !== 'localhost' && hostname !== '127.0.0.1') {
    return `http://${hostname}:8080/api`;
  }

  // 生产环境：通过nginx代理
  return '/api';
};

// 获取认证头信息
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  if (token) {
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
  }
  return {
    'Content-Type': 'application/json'
  };
};

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  headers: getAuthHeaders()
});

// 请求拦截器 - 自动添加认证头
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理认证错误
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 认证失败，清除本地存储并跳转到登录页
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      window.location.reload();
    }
    return Promise.reject(error);
  }
);

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
 * 转入余额宝
 */
export const transferIn = async (amount) => {
  try {
    const response = await apiClient.post('/transfer/in', { amount });
    return response.data;
  } catch (error) {
    console.error('转入操作失败:', error);
    throw error;
  }
};

/**
 * 转出余额宝
 */
export const transferOut = async (amount) => {
  try {
    const response = await apiClient.post('/transfer/out', { amount });
    return response.data;
  } catch (error) {
    console.error('转出操作失败:', error);
    throw error;
  }
};

/**
 * 获取服务器IP地址
 */
export const getServerIP = async () => {
  try {
    const response = await apiClient.get('/ip');
    return response.data.ip;
  } catch (error) {
    console.error('获取服务器IP失败:', error);
    return 'localhost';
  }
};

/**
 * 获取交易记录
 */
export const getTransactions = async () => {
  try {
    const response = await apiClient.get('/transactions');
    return response.data;
  } catch (error) {
    console.error('获取交易记录失败:', error);
    throw error;
  }
};

export { getApiBaseUrl };
export default apiClient;