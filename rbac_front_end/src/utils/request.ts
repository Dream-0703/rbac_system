import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import { type AxiosRequestHeaders } from 'axios';
import { getCurrentUser } from '@/utils/auth';
import type { ApiRequest, User } from '@/types/api';

// 创建实例，基础路径对应后端接口前缀
const request = axios.create({
  baseURL: '/api/v1',  // 与后端 @RequestMapping("/api/v1") 匹配
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  } as AxiosRequestHeaders  // 明确headers类型
});

// 请求拦截器：添加Token和User对象（对应后端AuthProxy的token校验和user信息）
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 确保headers存在
    if (!config.headers) {
      config.headers = {} as AxiosRequestHeaders;
    }
    
    // 添加token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.token = token;
    }
    
    // 统一添加user对象到非登录请求体
    if (config.data) {
      let requestData: ApiRequest;
      const isStringData = typeof config.data === 'string';
      
      // 处理请求体可能是字符串或对象的情况
      if (isStringData) {
        requestData = JSON.parse(config.data) as ApiRequest;
      } else {
        requestData = config.data as ApiRequest;
      }
      
      // 非登录请求添加user对象
      if (requestData.permCode !== 'system:user:own:login') {
        // 直接从请求体的token字段获取username，因为login时token就是username
        const username = requestData.token || '';
        // 强制设置user对象，只包含username字段
        requestData.user = { username } as User;
        
        // 更新请求体
        if (isStringData) {
          config.data = JSON.stringify(requestData);
        } else {
          config.data = requestData;
        }
      }
    } else {
      // 如果没有请求体，创建一个新的ApiRequest对象
      const token = localStorage.getItem('token') || '';
      // 直接从token获取username
      const username = token;
      
      config.data = {
        token: token,
        permCode: '',
        bizParams: '{}',
        user: { username } as User
      } as ApiRequest;
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器：处理后端统一响应格式（ApiResponse）
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;
    // 后端返回success=false时，直接抛出错误（便于页面捕获）
    if (!res.success) {
      return Promise.reject(new Error(res.msg || '后端请求失败'));
    }
    return res;  // 返回完整的ApiResponse对象
  },
  (error) => Promise.reject(error)
);

export default request;