import { getToken } from '@/utils/auth'
import { ref, onUnmounted } from 'vue'

const SSE_URL = import.meta.env.VITE_APP_BASE_API + '/system/sse/subscribe'

let eventSource = null
let reconnectTimer = null
let noticeCallbacks = []
let connected = ref(false)

/**
 * 建立SSE连接
 */
function connect() {
  disconnect()

  const token = getToken()
  if (!token) {
    console.warn('[SSE] 无token，跳过连接')
    return
  }

  const url = SSE_URL + '?token=' + encodeURIComponent(token)

  eventSource = new EventSource(url)

  eventSource.addEventListener('connected', () => {
    console.log('[SSE] 连接成功')
    connected.value = true
  })

  eventSource.addEventListener('notice', (event) => {
    try {
      const data = JSON.parse(event.data)
      noticeCallbacks.forEach(cb => cb(data))
    } catch (e) {
      console.warn('[SSE] 解析通知数据失败', e)
    }
  })

  eventSource.onerror = () => {
    console.warn('[SSE] 连接断开，5秒后重连')
    connected.value = false
    disconnect()
    reconnectTimer = setTimeout(() => {
      connect()
    }, 5000)
  }
}

/**
 * 断开SSE连接
 */
function disconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  connected.value = false
}

/**
 * 注册通知回调
 */
function onNotice(callback) {
  noticeCallbacks.push(callback)
  return () => {
    noticeCallbacks = noticeCallbacks.filter(cb => cb !== callback)
  }
}

/**
 * SSE连接管理composable
 */
export function useSse() {
  return {
    connected,
    connect,
    disconnect,
    onNotice
  }
}
