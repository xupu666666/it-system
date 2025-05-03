/**
 * 修复 ResizeObserver 循环错误
 * 这个错误通常不会影响应用程序功能，但会在控制台显示
 */

// 保存原始的错误处理函数
const originalConsoleError = console.error;

// 重写错误处理函数，忽略 ResizeObserver 相关错误
console.error = (...args) => {
  // 检查错误消息是否包含 ResizeObserver 循环错误
  if (args.length > 0 && 
      typeof args[0] === 'string' && 
      (args[0].includes('ResizeObserver loop') || 
       args[0].includes('ResizeObserver loop completed with undelivered notifications'))) {
    // 忽略这个错误
    return;
  }
  
  // 对于其他错误，使用原始的错误处理函数
  originalConsoleError.apply(console, args);
};

// 全局错误处理
window.addEventListener('error', (event) => {
  if (event && event.message && 
      (event.message.includes('ResizeObserver loop') || 
       event.message.includes('ResizeObserver loop completed with undelivered notifications'))) {
    // 阻止错误传播
    event.stopImmediatePropagation();
    event.preventDefault();
    return true;
  }
  return false;
}, true);

export default {
  install(app) {
    // 这是一个Vue插件，可以在main.js中使用app.use()加载
    console.log('ResizeObserver错误修复已加载');
  }
};
