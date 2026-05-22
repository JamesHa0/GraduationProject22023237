/**
 * roleInfo 就绪后执行回调的 composable
 * 解决 roleInfo 异步加载导致页面获取不到正确角色ID的问题
 */
import { watch, onMounted } from 'vue'
import useUserStore from '@/store/modules/user'

/**
 * 等待 roleInfo 就绪后执行回调
 * @param {Function} callback - roleInfo 就绪后执行的函数
 * @param {Object} options - 配置项
 * @param {boolean} options.immediate - roleInfo 已就绪时是否立即执行，默认 true
 */
export function onRoleInfoReady(callback, options = {}) {
  const { immediate = true } = options
  const userStore = useUserStore()

  onMounted(() => {
    if (immediate && userStore?.roleInfo?.[0]?.id) {
      callback()
    } else {
      const stop = watch(
        () => userStore?.roleInfo,
        (val) => {
          if (val?.[0]?.id) {
            callback()
            stop()
          }
        },
        { deep: true }
      )
    }
  })
}

/**
 * 获取当前学生的 studentId（从 roleInfo 中获取，不 fallback 到 userId）
 * @returns {number|null}
 */
export function getStudentId() {
  const userStore = useUserStore()
  return userStore?.roleInfo?.[0]?.id || null
}

/**
 * 获取当前导师的 teacherId（从 roleInfo 中获取，不 fallback 到 userId）
 * @returns {number|null}
 */
export function getTeacherId() {
  const userStore = useUserStore()
  return userStore?.roleInfo?.[0]?.id || null
}

/**
 * 获取当前角色ID对应的角色表ID（学生表/教师表）
 * 不再 fallback 到 userStore.userId，因为 userId 是 user 表 ID，不是角色表 ID
 * @returns {number|null}
 */
export function getRoleId() {
  const userStore = useUserStore()
  return userStore?.roleInfo?.[0]?.id || null
}
