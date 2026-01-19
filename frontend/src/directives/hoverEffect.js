/**
 * 全局悬停效果指令
 * 用法：v-hover-effect 或 v-hover-effect="{ type: 'lift', shadow: true, border: true }"
 *
 * 支持的类型：
 * - 'lift': 上浮效果（transform translateY）
 * - 'scale': 缩放效果
 * - 'glow': 发光效果
 * - 'border': 边框颜色变化
 * - 'shadow': 阴影效果
 */

export default {
  mounted(el, binding) {
    const options = binding.value || {}
    const effectType = options.type || 'lift'

    const originalTransition = el.style.transition
    el.style.transition = options.transition || 'all 0.3s ease'

    const handleMouseEnter = () => {
      switch (effectType) {
        case 'lift':
          el.style.transform = 'translateY(-4px)'
          el.style.boxShadow = '0 8px 24px rgba(0, 0, 0, 0.12)'
          break
        case 'scale':
          el.style.transform = 'scale(1.02)'
          el.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.08)'
          break
        case 'glow':
          el.style.boxShadow = '0 0 20px rgba(24, 144, 255, 0.3)'
          el.style.borderColor = '#1890ff'
          el.style.borderWidth = '2px'
          break
        case 'border':
          el.style.borderColor = '#1890ff'
          el.style.borderWidth = '2px'
          break
        case 'shadow':
          el.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.1)'
          break
      }
    }

    const handleMouseLeave = () => {
      el.style.transform = ''
      el.style.boxShadow = ''
      el.style.borderColor = ''
      el.style.borderWidth = ''
    }

    el.addEventListener('mouseenter', handleMouseEnter)
    el.addEventListener('mouseleave', handleMouseLeave)

    el._hoverEffectCleanup = () => {
      el.style.transition = originalTransition
      el.removeEventListener('mouseenter', handleMouseEnter)
      el.removeEventListener('mouseleave', handleMouseLeave)
    }
  },

  unmounted(el) {
    if (el._hoverEffectCleanup) {
      el._hoverEffectCleanup()
      delete el._hoverEffectCleanup
    }
  }
}

// 如果需要简单版本的导出，可以将其作为命名导出来实现
export const simpleHoverEffect = {
  mounted(el, binding) {
    const options = binding.value || {}
    const type = options.type || 'lift'

    const originalTransition = el.style.transition

    el.style.transition = options.transition || 'all 0.3s ease'

    const handleMouseEnter = () => {
      switch (type) {
        case 'lift':
          el.style.transform = 'translateY(-4px)'
          el.style.boxShadow = '0 8px 24px rgba(0, 0, 0, 0.12)'
          break
        case 'scale':
          el.style.transform = 'scale(1.02)'
          el.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.08)'
          break
        case 'glow':
          el.style.boxShadow = '0 0 20px rgba(24, 144, 255, 0.3)'
          el.style.borderColor = '#1890ff'
          el.style.borderWidth = '2px'
          break
        case 'border':
          el.style.borderColor = '#1890ff'
          el.style.borderWidth = '2px'
          break
        case 'shadow':
          el.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.1)'
          break
      }
    }

    const handleMouseLeave = () => {
      el.style.transform = ''
      el.style.boxShadow = ''
      el.style.borderColor = ''
      el.style.borderWidth = ''
    }

    el.addEventListener('mouseenter', handleMouseEnter)
    el.addEventListener('mouseleave', handleMouseLeave)

    el._simpleHoverCleanup = () => {
      el.style.transition = originalTransition
      el.removeEventListener('mouseenter', handleMouseEnter)
      el.removeEventListener('mouseleave', handleMouseLeave)
    }
  },

  unmounted(el) {
    if (el._simpleHoverCleanup) {
      el._simpleHoverCleanup()
      delete el._simpleHoverCleanup
    }
  }
}