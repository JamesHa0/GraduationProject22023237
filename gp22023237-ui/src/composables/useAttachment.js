import { ref } from 'vue'
import { getSignedUrl, downloadFile } from '@/api/academic'
import { saveAs } from 'file-saver'

/**
 * 附件处理 composable
 * 提取自 submit/index.vue 和 Detail 组件中的重复代码
 */
export function useAttachment() {
  // 签名URL缓存
  const signedUrls = ref({})

  /**
   * 从附件项中提取URL（兼容 url|fileName 和纯 url 两种格式）
   */
  function getUrlFromItem(item) {
    const pipeIdx = item.indexOf('|')
    return pipeIdx > -1 ? item.substring(0, pipeIdx) : item
  }

  /**
   * 从附件项中提取原始文件名
   */
  function getFileNameFromItem(item) {
    const pipeIdx = item.indexOf('|')
    if (pipeIdx > -1) return item.substring(pipeIdx + 1)
    // 兼容旧格式：从URL中提取
    const url = item
    const name = url.substring(url.lastIndexOf('/') + 1)
    return decodeURIComponent(name.split('?')[0]) || '附件'
  }

  /**
   * 解析附件路径为列表（兼容逗号分隔和JSON数组格式）
   */
  function getAttachmentPath(path) {
    if (!path) return []
    const trimmed = path.trim()
    // 尝试JSON数组格式
    if (trimmed.startsWith('[')) {
      try {
        const arr = JSON.parse(trimmed)
        return arr.filter(u => u && u.trim()).map(u => u.trim())
      } catch (e) {
        // 解析失败，按逗号分隔处理
      }
    }
    return trimmed.split(',').filter(u => u.trim()).map(u => u.trim())
  }

  /**
   * 获取附件列表（getAttachmentPath的别名）
   */
  function getAttachmentList(path) {
    return getAttachmentPath(path)
  }

  /**
   * 判断附件项是否为图片文件
   */
  function isImageFile(item) {
    const url = getUrlFromItem(item)
    const ext = url.split('.').pop().toLowerCase().split('?')[0]
    return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)
  }

  /**
   * 获取附件列表中的图片项
   */
  function getImageList(path) {
    return getAttachmentPath(path).filter(u => isImageFile(u))
  }

  /**
   * 获取图片在图片列表中的索引
   */
  function getImageListIndex(path, item) {
    return getImageList(path).indexOf(item)
  }

  /**
   * 批量获取签名URL（带并发控制）
   */
  async function fetchSignedUrls(paths, maxConcurrent = 5) {
    const allUrls = []
    paths.forEach(path => {
      if (path) {
        getAttachmentPath(path).forEach(item => {
          const url = getUrlFromItem(item)
          if (!signedUrls.value[url]) {
            allUrls.push(url)
          }
        })
      }
    })
    const uniqueUrls = [...new Set(allUrls)]

    // 并发控制：限制同时请求的数量
    const results = []
    for (let i = 0; i < uniqueUrls.length; i += maxConcurrent) {
      const batch = uniqueUrls.slice(i, i + maxConcurrent)
      const batchResults = await Promise.allSettled(
        batch.map(async url => {
          try {
            const res = await getSignedUrl(url)
            if (res.data && res.data.url) {
              signedUrls.value[url] = res.data.url
              return { url, signedUrl: res.data.url }
            }
          } catch (e) {
            console.warn('获取签名URL失败:', url, e)
          }
          return null
        })
      )
      results.push(...batchResults)
    }
    return results
  }

  /**
   * 获取签名后的图片预览列表
   */
  function getSignedImageList(path) {
    return getImageList(path).map(item => signedUrls.value[getUrlFromItem(item)] || getUrlFromItem(item))
  }

  /**
   * 点击下载非图片附件（通过后端代理下载，指定原文件名）
   */
  async function handleDownload(item) {
    try {
      const url = getUrlFromItem(item)
      const fileName = getFileNameFromItem(item)
      const blob = await downloadFile(url, fileName)
      saveAs(blob, fileName)
    } catch (e) {
      console.error('下载失败:', e)
    }
  }

  return {
    signedUrls,
    getUrlFromItem,
    getFileNameFromItem,
    getAttachmentPath,
    getAttachmentList,
    isImageFile,
    getImageList,
    getImageListIndex,
    fetchSignedUrls,
    getSignedImageList,
    handleDownload
  }
}
