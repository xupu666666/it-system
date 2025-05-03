/**
 * 存储工具类，用于安全地操作localStorage和sessionStorage
 */

/**
 * 安全地从localStorage获取数据
 * @param {string} key 存储键名
 * @param {any} defaultValue 默认值
 * @returns {any} 解析后的数据或默认值
 */
export function getLocalItem(key, defaultValue = null) {
  try {
    const value = localStorage.getItem(key);
    if (value === null || value === 'undefined') {
      return defaultValue;
    }
    return JSON.parse(value);
  } catch (error) {
    console.error(`Error reading localStorage key "${key}":`, error);
    // 如果解析失败，清除该项并返回默认值
    localStorage.removeItem(key);
    return defaultValue;
  }
}

/**
 * 安全地将数据存储到localStorage
 * @param {string} key 存储键名
 * @param {any} value 要存储的值
 */
export function setLocalItem(key, value) {
  try {
    if (value === undefined) {
      localStorage.removeItem(key);
    } else {
      localStorage.setItem(key, JSON.stringify(value));
    }
  } catch (error) {
    console.error(`Error saving to localStorage key "${key}":`, error);
  }
}

/**
 * 安全地从sessionStorage获取数据
 * @param {string} key 存储键名
 * @param {any} defaultValue 默认值
 * @returns {any} 解析后的数据或默认值
 */
export function getSessionItem(key, defaultValue = null) {
  try {
    const value = sessionStorage.getItem(key);
    if (value === null || value === 'undefined') {
      return defaultValue;
    }
    return JSON.parse(value);
  } catch (error) {
    console.error(`Error reading sessionStorage key "${key}":`, error);
    // 如果解析失败，清除该项并返回默认值
    sessionStorage.removeItem(key);
    return defaultValue;
  }
}

/**
 * 安全地将数据存储到sessionStorage
 * @param {string} key 存储键名
 * @param {any} value 要存储的值
 */
export function setSessionItem(key, value) {
  try {
    if (value === undefined) {
      sessionStorage.removeItem(key);
    } else {
      sessionStorage.setItem(key, JSON.stringify(value));
    }
  } catch (error) {
    console.error(`Error saving to sessionStorage key "${key}":`, error);
  }
}

/**
 * 清除所有本地存储
 */
export function clearAllStorage() {
  try {
    localStorage.clear();
    sessionStorage.clear();
    console.log('All storage cleared successfully');
  } catch (error) {
    console.error('Error clearing storage:', error);
  }
}

// 初始化时清理可能损坏的存储
export function cleanupStorage() {
  // 获取所有localStorage键
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    try {
      const value = localStorage.getItem(key);
      if (value === 'undefined' || value === undefined || value === null) {
        localStorage.removeItem(key);
        console.log(`Removed invalid localStorage item: ${key}`);
        continue;
      }
      
      // 尝试解析JSON
      JSON.parse(value);
    } catch (e) {
      // 如果解析失败，删除该项
      console.warn(`Removed corrupted localStorage item: ${key}`);
      localStorage.removeItem(key);
    }
  }
  
  // 获取所有sessionStorage键
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i);
    try {
      const value = sessionStorage.getItem(key);
      if (value === 'undefined' || value === undefined || value === null) {
        sessionStorage.removeItem(key);
        console.log(`Removed invalid sessionStorage item: ${key}`);
        continue;
      }
      
      // 尝试解析JSON
      JSON.parse(value);
    } catch (e) {
      // 如果解析失败，删除该项
      console.warn(`Removed corrupted sessionStorage item: ${key}`);
      sessionStorage.removeItem(key);
    }
  }
}
