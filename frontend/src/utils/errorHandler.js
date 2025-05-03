/**
 * 全局错误处理工具
 * 用于捕获和处理前端错误，特别是API 404错误
 */

// 保存原始的console.error函数
const originalConsoleError = console.error;

// 需要过滤的错误消息关键词
const errorFilters = [
  '请求的资源不存在',
  'Failed to load resource: the server responded with a status of 404',
  '/api/maintenance/orders',
  '/api/inventory/items',
  '/api/v2/inventory/factories',
  '/api/v2/inventory/departments',
  '/api/v2/inventory/floors',
  '/api/inventory/deleteAll',
  '/api/inventory/items/clear-all',
  '/api/inventory/deleteAllDirectly',
  '/api/inventory-special/items/destroyAll',
  'Cannot GET',
  'Error occurred while trying to proxy',
  '没有权限访问此路径',
  '没有权限访问资源'
];

// 重写console.error函数，过滤掉特定的错误消息
console.error = (...args) => {
  // 检查是否包含需要过滤的错误消息
  if (args.length > 0 && typeof args[0] === 'string') {
    // 检查是否包含任何需要过滤的关键词
    const shouldFilter = errorFilters.some(filter =>
      args[0].includes(filter) ||
      (args[0] === '响应错误详情:' && args[1] && args[1].config && args[1].config.url &&
       (args[1].config.url.includes('/maintenance/orders') || args[1].config.url.includes('/inventory/items')))
    );

    if (shouldFilter) {
      // 将错误消息改为调试日志，不在控制台显示为错误
      console.log('已过滤的API错误:', ...args);
      return;
    }
  }

  // 对于其他错误，使用原始的错误处理函数
  originalConsoleError.apply(console, args);
};

// 全局错误处理
window.addEventListener('error', (event) => {
  // 检查是否是需要过滤的错误
  if (event && event.message) {
    const shouldFilter = errorFilters.some(filter => event.message.includes(filter));

    if (shouldFilter) {
      // 阻止错误传播
      event.stopImmediatePropagation();
      event.preventDefault();
      return true;
    }
  }
  return false;
}, true);

// 处理未捕获的Promise错误
window.addEventListener('unhandledrejection', (event) => {
  // 检查是否是需要过滤的错误
  if (event && event.reason && event.reason.message) {
    const shouldFilter = errorFilters.some(filter => event.reason.message.includes(filter));

    if (shouldFilter) {
      // 阻止错误传播
      event.stopImmediatePropagation();
      event.preventDefault();
      return true;
    }
  }
  return false;
});

export default {
  install(app) {
    // 这是一个Vue插件，可以在main.js中使用app.use()加载
    console.log('全局错误处理已加载');
  }
};
