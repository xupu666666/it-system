<template>
  <component :is="type" v-bind="linkProps(to)">
    <slot />
  </component>
</template>

<script setup>
import { computed } from 'vue'
import { isExternal } from '@/utils/validate'
import { useRouter } from 'vue-router'

const props = defineProps({
  to: {
    type: String,
    required: true
  }
})

const router = useRouter()

// 确定组件类型：a标签或者router-link
const type = computed(() => {
  if (isExternal(props.to)) {
    return 'a'
  }
  return 'router-link'
})

// 根据链接类型返回不同的属性
const linkProps = (to) => {
  if (isExternal(to)) {
    return {
      href: to,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    to: to
  }
}
</script> 