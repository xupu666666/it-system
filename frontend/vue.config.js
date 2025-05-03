/**
 * Vue CLI 配置文件
 *
 * 配置代理，解决前端和后端端口不匹配问题
 */

module.exports = {
  // 开发服务器配置
  devServer: {
    // 配置代理
    proxy: {
      // 将所有 /api 开头的请求代理到后端服务
      '/api': {
        target: 'http://localhost:8100',
        changeOrigin: true,
        // 不要移除 /api 前缀
        pathRewrite: null,
        // 添加更多调试信息
        logLevel: 'debug',
        // 添加自定义请求头
        headers: {
          'X-Forwarded-For': '127.0.0.1',
          'X-Forwarded-Proto': 'http',
          'X-Forwarded-Host': 'localhost'
        },
        // 启用凭证
        withCredentials: true,
        // 添加错误处理
        onError: (err, req, res) => {
          console.error('代理错误:', err);
        },
        // 添加代理事件处理
        onProxyReq: (proxyReq, req, res) => {
          console.log('代理请求:', req.method, req.url);
        },
        onProxyRes: (proxyRes, req, res) => {
          console.log('代理响应:', proxyRes.statusCode, req.url);
        }
      }
    }
  },

  // 生产环境配置
  configureWebpack: {
    // 在这里可以添加其他webpack配置
  }
}
