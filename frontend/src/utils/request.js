import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/v2/auth'
import config from '@/config'

// 创建axios实例
const service = axios.create({
  baseURL: '', // 不使用baseURL，直接在URL中指定完整路径
  timeout: 30000, // 请求超时时间
  withCredentials: true, // 强制启用凭证，解决跨域问题
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  },
  // 添加重试配置
  retry: 2, // 重试次数
  retryDelay: 1000 // 重试延迟
})

// 打印baseURL，便于调试
console.log('API基础URL:', service.defaults.baseURL)

// 请求拦截器
service.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      // 让每个请求携带token
      config.headers['Authorization'] = `Bearer ${authStore.token}`
    }

    // 打印完整URL
    const fullUrl = (config.baseURL || '') + config.url
    console.log('完整请求URL:', fullUrl)
    console.log('请求方法:', config.method.toUpperCase())
    console.log('请求头:', JSON.stringify(config.headers, null, 2))

    // 打印请求参数
    if (config.params) {
      console.log('请求参数:', JSON.stringify(config.params, null, 2))
    }

    // 检查是否是文件上传请求
    if (config.headers['Content-Type'] && config.headers['Content-Type'].includes('multipart/form-data')) {
      console.log('检测到文件上传请求')
      // 对于FormData，不打印内容，会导致循环引用错误
      if (config.data instanceof FormData) {
        console.log('FormData内容:')
        for (let pair of config.data.entries()) {
          if (pair[1] instanceof File) {
            console.log(`  ${pair[0]}: File(name=${pair[1].name}, type=${pair[1].type}, size=${pair[1].size}字节)`)
          } else {
            console.log(`  ${pair[0]}: ${pair[1]}`)
          }
        }
      }
    } else if (config.data) {
      console.log('请求数据:', JSON.stringify(config.data, null, 2))
    }

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 添加请求重试拦截器
axios.interceptors.response.use(undefined, function axiosRetryInterceptor(err) {
  const config = err.config;
  // 如果配置了重试，并且不是取消请求，则进行重试
  if (config && config.retry && !axios.isCancel(err)) {
    // 设置重试计数器
    config.__retryCount = config.__retryCount || 0;

    // 检查是否已经达到最大重试次数
    if (config.__retryCount < config.retry) {
      // 增加重试计数
      config.__retryCount += 1;
      console.log(`请求重试 (${config.__retryCount}/${config.retry}): ${config.url}`);

      // 创建新的Promise来处理重试延迟
      const backoff = new Promise(function(resolve) {
        setTimeout(function() {
          console.log(`重试延迟后重新请求: ${config.url}`);
          resolve();
        }, config.retryDelay || 1000);
      });

      // 返回重试请求的Promise
      return backoff.then(function() {
        return axios(config);
      });
    }
  }

  // 如果已达到最大重试次数或未配置重试，则拒绝Promise
  return Promise.reject(err);
});

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 检查是否是blob类型响应
    const isBlob = response.config.responseType === 'blob';

    if (isBlob) {
      console.log('收到blob响应:', response.status, response.headers);

      // 检查内容类型
      const contentType = response.headers['content-type'];

      // 如果是文本类型的错误消息，而不是预期的Excel文件
      if (contentType && contentType.includes('text/plain')) {
        // 将Blob转换为文本并显示错误消息
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            const errorMessage = reader.result;
            console.error('Blob错误消息:', errorMessage);

            // 显示错误消息
            ElMessage({
              message: errorMessage || '导出失败',
              type: 'error',
              duration: 5 * 1000
            });

            // 创建一个类似于普通错误响应的对象
            const error = new Error(errorMessage);
            error.response = response;
            reject(error);
          };
          reader.onerror = () => {
            reject(new Error('无法读取错误响应'));
          };
          reader.readAsText(response.data);
        });
      }

      // 正常的blob响应，直接返回
      return response.data;
    }

    // 非blob响应的处理
    console.log('响应成功:', response.status, response.data);
    console.log('完整响应对象:', response);

    // 直接返回响应数据
    return response.data;
  },
  error => {
    console.error('响应错误详情:', error);

    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误数据:', error.response.data);

      // 检查是否是blob类型响应
      const isBlob = error.response.config && error.response.config.responseType === 'blob';

      if (isBlob && error.response.data instanceof Blob) {
        // 将Blob转换为文本并显示错误消息
        return new Promise((_, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            const errorMessage = reader.result;
            console.error('Blob错误消息:', errorMessage);

            // 显示错误消息
            ElMessage({
              message: errorMessage || '导出失败',
              type: 'error',
              duration: 5 * 1000
            });

            // 更新错误对象
            error.message = errorMessage;
            reject(error);
          };
          reader.onerror = () => {
            reject(error);
          };
          reader.readAsText(error.response.data);
        });
      }
    } else if (error.request) {
      console.error('没有收到响应:', error.request);
    } else {
      console.error('请求配置错误:', error.message);
    }
    console.error('请求配置:', error.config);

    // 处理错误响应
    const { response } = error;

    if (response && response.status) {
      const authStore = useAuthStore();

      switch (response.status) {
        case 401: // 未授权
          ElMessageBox.confirm(
            '登录状态已过期，请重新登录',
            '系统提示',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            authStore.logout();
            window.location.reload();
          });
          break;

        case 403: // 禁止访问
          // 检查是否是需要静默处理的API
          if (error.config && error.config.url) {
            const url = error.config.url;
            // 需要静默处理的API路径列表
            const silentPaths = [
              '/api/inventory/deleteAll',
              '/api/inventory/items/clear-all',
              '/api/inventory/deleteAllDirectly',
              '/api/inventory-special/items/destroyAll'
            ];

            // 检查URL是否包含任何需要静默处理的路径
            const shouldSilent = silentPaths.some(path => url.includes(path));

            if (shouldSilent) {
              // 不显示错误消息，静默处理
              console.log(`静默处理403错误: ${url}`);
            } else {
              // 其他403错误正常显示，但使用console.log而不是ElMessage
              console.log(`没有权限访问资源: ${response.data && response.data.message ? response.data.message : '权限不足'}`);
            }
          } else {
            // 使用console.log而不是ElMessage，避免在UI上显示错误
            console.log(`没有权限访问资源: ${response.data && response.data.message ? response.data.message : '权限不足'}`);
          }
          break;

        case 404: // 资源不存在
          // 检查是否是需要静默处理的API
          if (error.config && error.config.url) {
            const url = error.config.url;
            // 需要静默处理的API路径列表
            const silentPaths = [
              '/api/v2/inventory/factories',
              '/api/v2/inventory/departments',
              '/api/v2/inventory/floors',
              '/api/inventory/deleteAll',
              '/api/inventory/items/clear-all',
              '/api/inventory/deleteAllDirectly',
              '/api/inventory-special/items/destroyAll'
            ];

            // 检查URL是否包含任何需要静默处理的路径
            const shouldSilent = silentPaths.some(path => url.includes(path));

            if (shouldSilent) {
              // 不显示错误消息，静默处理
              console.log(`静默处理404错误: ${url}`);
            } else {
              // 其他404错误正常显示
              console.log(`显示404错误: ${url}`);
              // 使用console.log而不是ElMessage，避免在UI上显示错误
            }
          } else {
            // 使用console.log而不是ElMessage，避免在UI上显示错误
            console.log(`请求的资源不存在`);
          }
          break;

        case 500: // 服务器错误
          // 检查是否是资产盘点相关的错误
          if (error.config && error.config.url &&
              (error.config.url.includes('/api/inventory/check') ||
               error.config.url.includes('/api/v2/inventory/check'))) {

            // 检查错误消息是否包含currentUser相关内容
            const errorMsg = response.data && response.data.message ? response.data.message : '';
            if (errorMsg.includes('currentUser') || errorMsg.includes('getUsername')) {
              console.log('检测到盘点操作中的用户会话问题，静默处理');
              // 不显示错误消息，静默处理
            } else {
              // 使用console.log而不是ElMessage，避免在UI上显示错误
              console.log(`服务器错误: ${errorMsg || '内部服务器错误'}`);
            }
          } else {
            // 其他500错误正常显示
            ElMessage({
              message: `服务器错误: ${response.data && response.data.message ? response.data.message : '内部服务器错误'}`,
              type: 'error',
              duration: 5 * 1000
            });
          }
          break;

        default:
          ElMessage({
            message: (response.data && response.data.message) ? response.data.message : `未知错误 (${response.status})`,
            type: 'error',
            duration: 5 * 1000
          });
      }
    } else {
      // 处理网络错误
      ElMessage({
        message: `网络错误: ${error.message}，请检查后端服务是否正常运行`,
        type: 'error',
        duration: 5 * 1000
      });
    }

    return Promise.reject(error);
  }
)

export default service
