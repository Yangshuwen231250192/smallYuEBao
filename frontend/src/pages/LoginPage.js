import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, message, Tabs, Modal } from 'antd';
import { UserOutlined, LockOutlined, PhoneOutlined, MailOutlined, QrcodeOutlined } from '@ant-design/icons';
import { QRCodeSVG } from 'qrcode.react';
import { getApiBaseUrl } from '../services/api';
import './LoginPage.css';

const { TabPane } = Tabs;

function LoginPage({ onLogin }) {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('login');
  const [showQRCode, setShowQRCode] = useState(false);
  const [localIP, setLocalIP] = useState('localhost');
  const [showQRCodeInLogin, setShowQRCodeInLogin] = useState(false);

  // 获取本地 IP 地址用于生成二维码
  useEffect(() => {
    const getAccessURL = async () => {
      try {
        // 优先使用环境变量中的 IP 地址（Docker 部署时使用）
        if (process.env.REACT_APP_SERVER_IP) {
          setLocalIP(process.env.REACT_APP_SERVER_IP);
          return;
        }

        // 尝试从后端获取真实 IP 地址
        try {
          const response = await fetch(`${getApiBaseUrl()}/ip`);
          const data = await response.json();
          if (data.ip && data.ip !== 'localhost' && data.ip !== '127.0.0.1') {
            setLocalIP(data.ip);
            console.log('获取到服务器真实 IP:', data.ip);
            return;
          }
        } catch (error) {
          console.log('后端 IP 获取失败，尝试其他方法');
        }

        // 尝试通过 WebRTC 获取本地 IP
        try {
          const ip = await getLocalIPViaWebRTC();
          if (ip && ip !== 'localhost' && ip !== '127.0.0.1') {
            setLocalIP(ip);
            console.log('通过 WebRTC 获取到本地 IP:', ip);
            return;
          }
        } catch (error) {
          console.log('WebRTC IP 获取失败');
        }

        // 如果以上方法都失败，使用当前访问地址的主机名
        const hostname = window.location.hostname;
        if (hostname && hostname !== 'localhost' && hostname !== '127.0.0.1') {
          setLocalIP(hostname);
          console.log('使用当前主机名:', hostname);
        } else {
          // 开发环境默认 localhost
          setLocalIP('localhost');
          console.log('使用默认 localhost');
        }
      } catch (error) {
        console.error('获取 IP 失败:', error);
        setLocalIP('localhost');
      }
    };

    getAccessURL();
  }, []);

  // 通过 WebRTC 获取本地 IP 地址
  const getLocalIPViaWebRTC = () => {
    return new Promise((resolve) => {
      const RTCPeerConnection = window.RTCPeerConnection || window.mozRTCPeerConnection || window.webkitRTCPeerConnection;

      if (!RTCPeerConnection) {
        resolve(null);
        return;
      }

      const pc = new RTCPeerConnection({ iceServers: [] });
      let ip = null;

      // 监听候选地址
      pc.onicecandidate = (ice) => {
        if (!ice.candidate) {
          // 所有候选地址收集完毕
          resolve(ip);
          return;
        }

        const candidate = ice.candidate.candidate;
        // 匹配 IPv4 地址
        const match = candidate.match(/([0-9]{1,3}(\.[0-9]{1,3}){3})/);
        if (match) {
          const candidateIP = match[1];
          // 排除本地回环地址和私有地址
          if (!candidateIP.startsWith('127.') &&
            !candidateIP.startsWith('192.168.') &&
            !candidateIP.startsWith('10.') &&
            !candidateIP.startsWith('172.')) {
            ip = candidateIP;
          }
        }
      };

      // 创建数据通道触发候选地址收集
      pc.createDataChannel('');
      pc.createOffer()
        .then(offer => pc.setLocalDescription(offer))
        .catch(() => resolve(null));

      // 设置超时
      setTimeout(() => resolve(ip), 1000);
    });
  };

  const onLoginFinish = async (values) => {
    setLoading(true);
    try {
      // 使用 JSON.stringify 确保正确的 JSON 格式
      const loginData = JSON.stringify({
        username: values.username,
        password: values.password
      });

      console.log('=== 登录调试信息 ===');
      console.log('请求 URL:', `${getApiBaseUrl()}/auth/login`);
      console.log('请求数据:', loginData);

      const response = await fetch(`${getApiBaseUrl()}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: loginData,
      });

      console.log('响应状态码:', response.status);
      console.log('响应头:', response.headers.get('content-type'));

      const responseData = await response.json();
      console.log('响应数据:', responseData);

      if (response.ok) {
        console.log('登录成功，token:', responseData.token);
        localStorage.setItem('token', responseData.token);
        localStorage.setItem('username', values.username);
        message.success('登录成功！');
        onLogin();
      } else {
        console.error('登录失败，错误信息:', responseData.error);
        message.error(responseData.error || '登录失败');
      }
    } catch (error) {
      console.error('登录异常:', error);
      message.error(`网络错误：${error.message}，请检查后端服务是否启动`);
    } finally {
      setLoading(false);
    }
  };

  const onRegisterFinish = async (values) => {
    setLoading(true);
    try {
      // 使用简单的JSON格式，避免转义字符问题
      const registerData = JSON.stringify({
        username: values.username,
        password: values.password,
        phone: values.phone,
        email: values.email,
        realName: values.realName
      });

      console.log('发送注册请求:', registerData); // 调试信息

      const response = await fetch(`${getApiBaseUrl()}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: registerData,
      });

      const data = await response.json();

      if (response.ok) {
        message.success('注册成功！请登录');
        setActiveTab('login');
      } else {
        message.error(data.error || '注册失败');
      }
    } catch (error) {
      message.error('网络错误，请检查后端服务是否启动');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-background">
        <Card
          className="login-card"
          title="小余额宝"
          extra={
            <Button
              type="text"
              icon={<QrcodeOutlined />}
              onClick={() => setShowQRCode(true)}
              style={{ fontSize: '16px', color: '#1677ff' }}
            >
              手机扫码
            </Button>
          }
        >
          <Tabs activeKey={activeTab} onChange={setActiveTab} centered>
            <TabPane tab="登录" key="login">
              <div className="login-form-container">
                <Form
                  name="login"
                  onFinish={onLoginFinish}
                  autoComplete="off"
                >
                  <Form.Item
                    name="username"
                    rules={[{ required: true, message: '请输入用户名!' }]}
                  >
                    <Input
                      prefix={<UserOutlined />}
                      placeholder="用户名"
                      size="large"
                    />
                  </Form.Item>

                  <Form.Item
                    name="password"
                    rules={[{ required: true, message: '请输入密码!' }]}
                  >
                    <Input.Password
                      prefix={<LockOutlined />}
                      placeholder="密码"
                      size="large"
                    />
                  </Form.Item>

                  <Form.Item>
                    <Button
                      type="primary"
                      htmlType="submit"
                      loading={loading}
                      size="large"
                      block
                    >
                      登录
                    </Button>
                  </Form.Item>
                </Form>

                <div className="qr-section">
                  <Button
                    type="dashed"
                    icon={<QrcodeOutlined />}
                    onClick={() => setShowQRCode(true)}
                    size="large"
                    block
                    style={{ marginTop: '20px' }}
                  >
                    手机扫码访问
                  </Button>
                  <p className="qr-tip-small">💡 手机和电脑需在同一网络</p>
                </div>
              </div>
            </TabPane>

            <TabPane tab="注册" key="register">
              <Form
                name="register"
                onFinish={onRegisterFinish}
                autoComplete="off"
              >
                <Form.Item
                  name="username"
                  rules={[
                    { required: true, message: '请输入用户名!' },
                    { min: 3, message: '用户名至少3个字符!' }
                  ]}
                >
                  <Input
                    prefix={<UserOutlined />}
                    placeholder="用户名"
                    size="large"
                  />
                </Form.Item>

                <Form.Item
                  name="password"
                  rules={[
                    { required: true, message: '请输入密码!' },
                    { min: 6, message: '密码至少6个字符!' }
                  ]}
                >
                  <Input.Password
                    prefix={<LockOutlined />}
                    placeholder="密码"
                    size="large"
                  />
                </Form.Item>

                <Form.Item
                  name="phone"
                  rules={[{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号!' }]}
                >
                  <Input
                    prefix={<PhoneOutlined />}
                    placeholder="手机号（可选）"
                    size="large"
                  />
                </Form.Item>

                <Form.Item
                  name="email"
                  rules={[{ type: 'email', message: '请输入正确的邮箱!' }]}
                >
                  <Input
                    prefix={<MailOutlined />}
                    placeholder="邮箱（可选）"
                    size="large"
                  />
                </Form.Item>

                <Form.Item
                  name="realName"
                >
                  <Input
                    placeholder="真实姓名（可选）"
                    size="large"
                  />
                </Form.Item>

                <Form.Item>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={loading}
                    size="large"
                    block
                  >
                    注册
                  </Button>
                </Form.Item>
              </Form>
            </TabPane>
          </Tabs>
        </Card>
      </div>

      {/* 二维码弹窗 */}
      <Modal
        title="📱 手机扫码访问"
        open={showQRCode}
        onCancel={() => setShowQRCode(false)}
        footer={null}
        width={350}
        centered
      >
        <div className="qr-modal-content">
          <div className="qr-code-container">
            <QRCodeSVG
              value={`http://${localIP}:3000`}
              size={220}
              level="M"
            />
          </div>
          <div className="qr-instructions">
            <p><strong>使用手机扫描二维码访问小余额宝</strong></p>
            <p className="qr-url">http://{localIP}:3000</p>
            <p className="qr-tip">💡 请确保手机和电脑在同一 WiFi 网络下</p>
            {localIP === 'localhost' && (
              <p className="qr-warning">
                ⚠️ 当前显示为 localhost，手机可能无法访问。
                <br />
                建议使用真实 IP 地址启动服务。
              </p>
            )}
          </div>
        </div>
      </Modal>
    </div>
  );
}

export default LoginPage;