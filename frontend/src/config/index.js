/**
 * 全局配置文件
 * 集中管理所有API和其他配置
 */

const config = {
  // API相关配置
  api: {
    // API基础路径
    baseUrl: process.env.VUE_APP_API_PREFIX || '',
    // 请求超时时间（毫秒）
    timeout: 15000,
    // 是否发送凭证
    withCredentials: true
  },

  // 系统信息
  system: {
    name: 'ICT系统管理平台',
    version: 'v2',
    copyright: `© ${new Date().getFullYear()} ICT系统管理平台 版权所有`
  }
}

export default config
