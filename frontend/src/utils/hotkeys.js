/**
 * 全局快捷键管理器
 * 用于注册和管理全局键盘快捷键
 */

class HotkeysManager {
  constructor() {
    this.hotkeys = new Map()
    this.isEnabled = true
    this.bindings = new Map()
  }

  /**
   * 注册快捷键
   * @param {string} keys - 快捷键组合，如 'Ctrl+S', 'Cmd+Enter', 'Escape'
   * @param {Function} handler - 处理函数
   * @param {Object} options - 选项
   * @param {string} options.scope - 作用域（可选，用于分组）
   * @param {string} options.description - 描述
   * @param {boolean} options.preventDefault - 是否阻止默认行为（默认true）
   * @param {boolean} options.stopPropagation - 是否停止冒泡（默认false）
   */
  register(keys, handler, options = {}) {
    const {
      scope = 'global',
      description = '',
      preventDefault = true,
      stopPropagation = false
    } = options

    const normalizedKeys = this.normalizeKeys(keys)

    if (!this.hotkeys.has(scope)) {
      this.hotkeys.set(scope, new Map())
    }

    const scopeMap = this.hotkeys.get(scope)
    scopeMap.set(normalizedKeys, {
      handler,
      description,
      preventDefault,
      stopPropagation
    })

    // 如果是全局作用域，立即绑定
    if (scope === 'global' && this.isEnabled) {
      this.bind(normalizedKeys, handler, { preventDefault, stopPropagation })
    }

    return () => this.unregister(keys, scope)
  }

  /**
   * 注销快捷键
   * @param {string} keys - 快捷键组合
   * @param {string} scope - 作用域（默认'global'）
   */
  unregister(keys, scope = 'global') {
    const normalizedKeys = this.normalizeKeys(keys)

    if (this.hotkeys.has(scope)) {
      const scopeMap = this.hotkeys.get(scope)
      scopeMap.delete(normalizedKeys)

      // 如果是全局作用域，解绑
      if (scope === 'global') {
        this.unbind(normalizedKeys)
      }
    }
  }

  /**
   * 启用快捷键
   */
  enable() {
    if (this.isEnabled) return
    this.isEnabled = true
    this.bindAll()
  }

  /**
   * 禁用快捷键
   */
  disable() {
    if (!this.isEnabled) return
    this.isEnabled = false
    this.unbindAll()
  }

  /**
   * 绑定所有全局快捷键
   */
  bindAll() {
    const globalMap = this.hotkeys.get('global')
    if (!globalMap) return

    globalMap.forEach((config, keys) => {
      this.bind(keys, config.handler, config)
    })
  }

  /**
   * 解绑所有快捷键
   */
  unbindAll() {
    this.bindings.forEach((handler, keys) => {
      document.removeEventListener('keydown', handler)
    })
    this.bindings.clear()
  }

  /**
   * 绑定单个快捷键
   */
  bind(keys, handler, config) {
    const keydownHandler = (event) => {
      if (!this.isEnabled) return
      if (!this.matchKeys(event, keys)) return

      if (config.preventDefault) {
        event.preventDefault()
      }
      if (config.stopPropagation) {
        event.stopPropagation()
      }

      handler(event)
    }

    this.bindings.set(keys, keydownHandler)
    document.addEventListener('keydown', keydownHandler)
  }

  /**
   * 解绑单个快捷键
   */
  unbind(keys) {
    const handler = this.bindings.get(keys)
    if (handler) {
      document.removeEventListener('keydown', handler)
      this.bindings.delete(keys)
    }
  }

  /**
   * 标准化快捷键格式
   * 转换：Ctrl+S -> ctrl+s, Cmd+Enter -> cmd+enter
   */
  normalizeKeys(keys) {
    return keys.toLowerCase().replace(/\s+/g, '').trim()
  }

  /**
   * 检查事件是否匹配快捷键
   */
  matchKeys(event, keys) {
    const normalizedKeys = this.normalizeKeys(keys)
    const parts = normalizedKeys.split('+')

    // 检查修饰键
    if (parts.includes('ctrl') !== event.ctrlKey) return false
    if (parts.includes('alt') !== event.altKey) return false
    if (parts.includes('shift') !== event.shiftKey) return false
    if (parts.includes('meta') !== event.metaKey) return false
    // Mac 的 Cmd 键也映射为 meta
    if (parts.includes('cmd') !== event.metaKey) return false

    // 获取主键（排除修饰键）
    const mainKey = parts.find(p =>
      !['ctrl', 'alt', 'shift', 'meta', 'cmd'].includes(p)
    )

    if (!mainKey) return false

    // 特殊键映射
    const specialKeys = {
      'escape': 'Escape',
      'enter': 'Enter',
      'space': ' ',
      'tab': 'Tab',
      'backspace': 'Backspace',
      'delete': 'Delete',
      'insert': 'Insert',
      'home': 'Home',
      'end': 'End',
      'pageup': 'PageUp',
      'pagedown': 'PageDown',
      'arrowup': 'ArrowUp',
      'arrowdown': 'ArrowDown',
      'arrowleft': 'ArrowLeft',
      'arrowright': 'ArrowRight',
      'f1': 'F1',
      'f2': 'F2',
      'f3': 'F3',
      'f4': 'F4',
      'f5': 'F5',
      'f6': 'F6',
      'f7': 'F7',
      'f8': 'F8',
      'f9': 'F9',
      'f10': 'F10',
      'f11': 'F11',
      'f12': 'F12'
    }

    const eventKey = specialKeys[mainKey] || mainKey.toLowerCase()
    return event.key.toLowerCase() === eventKey || event.key === specialKeys[mainKey]
  }

  /**
   * 获取所有已注册的快捷键列表
   * @param {string} scope - 作用域（默认返回所有）
   */
  getHotkeys(scope = null) {
    if (scope) {
      return Array.from(this.hotkeys.get(scope)?.entries() || [])
        .map(([keys, config]) => ({
          keys,
          ...config
        }))
    }

    // 返回所有作用域的快捷键
    const result = []
    this.hotkeys.forEach((scopeMap, scopeName) => {
      scopeMap.forEach((config, keys) => {
        result.push({
          keys,
          scope: scopeName,
          ...config
        })
      })
    })
    return result
  }

  /**
   * 清空所有快捷键
   * @param {string} scope - 作用域（可选，默认清空所有）
   */
  clear(scope = null) {
    if (scope) {
      if (scope === 'global') {
        this.unbindAll()
      }
      this.hotkeys.delete(scope)
    } else {
      this.unbindAll()
      this.hotkeys.clear()
    }
  }
}

// 创建全局实例
const hotkeys = new HotkeysManager()

// 导出工具函数
export const registerHotkey = (keys, handler, options) => hotkeys.register(keys, handler, options)
export const unregisterHotkey = (keys, scope) => hotkeys.unregister(keys, scope)
export const enableHotkeys = () => hotkeys.enable()
export const disableHotkeys = () => hotkeys.disable()
export const getHotkeys = (scope) => hotkeys.getHotkeys(scope)
export const clearHotkeys = (scope) => hotkeys.clear(scope)

// 默认快捷键定义
export const defaultHotkeys = [
  {
    keys: 'Ctrl+S',
    cmd: 'Cmd+S',
    description: '保存',
    action: () => console.log('保存操作')
  },
  {
    keys: 'Ctrl+Z',
    cmd: 'Cmd+Z',
    description: '撤销',
    action: () => console.log('撤销操作')
  },
  {
    keys: 'Ctrl+Y',
    cmd: 'Cmd+Shift+Z',
    description: '重做',
    action: () => console.log('重做操作')
  },
  {
    keys: 'Escape',
    description: '关闭对话框',
    action: () => console.log('关闭操作')
  },
  {
    keys: 'Delete',
    description: '删除选中项',
    action: () => console.log('删除操作')
  },
  {
    keys: 'Ctrl+F',
    cmd: 'Cmd+F',
    description: '搜索',
    action: () => console.log('搜索操作')
  },
  {
    keys: 'Ctrl+N',
    cmd: 'Cmd+N',
    description: '新建',
    action: () => console.log('新建操作')
  }
]

export default hotkeys
