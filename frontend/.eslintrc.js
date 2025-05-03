module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended'
  ],
  parserOptions: {
    ecmaVersion: 2020
  },
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    // 禁用组件名称必须是多词的规则
    'vue/multi-word-component-names': 'off',
    // 禁用未使用变量的规则
    'no-unused-vars': 'off',
    // 禁用不必要的转义字符规则
    'no-useless-escape': 'off'
  }
} 