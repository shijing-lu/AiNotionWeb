/**
 * Vue 3 Composition API 组合式函数
 */

import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import type { Ref, ComputedRef } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'

/**
 * 加载状态管理
 */
export function useLoading(initialState = false) {
  const loading = ref(initialState)
  
  const setLoading = (state: boolean) => {
    loading.value = state
  }
  
  const withLoading = async <T>(fn: () => Promise<T>): Promise<T> => {
    setLoading(true)
    try {
      return await fn()
    } finally {
      setLoading(false)
    }
  }
  
  return {
    loading: readonly(loading),
    setLoading,
    withLoading
  }
}

/**
 * 分页管理
 */
export function usePagination(initialPageSize = 20) {
  const pagination = reactive({
    current: 1,
    pageSize: initialPageSize,
    total: 0
  })
  
  const totalPages = computed(() => Math.ceil(pagination.total / pagination.pageSize))
  
  const hasNext = computed(() => pagination.current < totalPages.value)
  const hasPrev = computed(() => pagination.current > 1)
  
  const setPage = (page: number) => {
    pagination.current = Math.max(1, Math.min(page, totalPages.value))
  }
  
  const setPageSize = (size: number) => {
    pagination.pageSize = size
    pagination.current = 1
  }
  
  const setTotal = (total: number) => {
    pagination.total = total
  }
  
  const nextPage = () => {
    if (hasNext.value) {
      pagination.current++
    }
  }
  
  const prevPage = () => {
    if (hasPrev.value) {
      pagination.current--
    }
  }
  
  const reset = () => {
    pagination.current = 1
    pagination.total = 0
  }
  
  return {
    pagination: readonly(pagination),
    totalPages,
    hasNext,
    hasPrev,
    setPage,
    setPageSize,
    setTotal,
    nextPage,
    prevPage,
    reset
  }
}

/**
 * 表单验证
 */
export function useFormValidation() {
  const errors = ref<Record<string, string>>({})
  
  const setError = (field: string, message: string) => {
    errors.value[field] = message
  }
  
  const clearError = (field: string) => {
    delete errors.value[field]
  }
  
  const clearAllErrors = () => {
    errors.value = {}
  }
  
  const hasError = (field: string) => {
    return !!errors.value[field]
  }
  
  const getError = (field: string) => {
    return errors.value[field] || ''
  }
  
  const isValid = computed(() => {
    return Object.keys(errors.value).length === 0
  })
  
  return {
    errors: readonly(errors),
    setError,
    clearError,
    clearAllErrors,
    hasError,
    getError,
    isValid
  }
}

/**
 * 搜索功能
 */
export function useSearch<T>(searchFn: (query: string) => Promise<T[]>, delay = 300) {
  const query = ref('')
  const results = ref<T[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  let searchTimeout: NodeJS.Timeout | null = null
  
  const search = async (searchQuery?: string) => {
    const q = searchQuery ?? query.value
    
    if (!q.trim()) {
      results.value = []
      return
    }
    
    loading.value = true
    error.value = null
    
    try {
      const data = await searchFn(q)
      results.value = data
    } catch (err) {
      error.value = err instanceof Error ? err.message : '搜索失败'
      results.value = []
    } finally {
      loading.value = false
    }
  }
  
  const debouncedSearch = () => {
    if (searchTimeout) {
      clearTimeout(searchTimeout)
    }
    
    searchTimeout = setTimeout(() => {
      search()
    }, delay)
  }
  
  const clear = () => {
    query.value = ''
    results.value = []
    error.value = null
  }
  
  watch(query, debouncedSearch)
  
  onUnmounted(() => {
    if (searchTimeout) {
      clearTimeout(searchTimeout)
    }
  })
  
  return {
    query,
    results: readonly(results),
    loading: readonly(loading),
    error: readonly(error),
    search,
    clear
  }
}

/**
 * 本地存储
 */
export function useLocalStorage<T>(key: string, defaultValue: T) {
  const storedValue = localStorage.getItem(key)
  const initialValue = storedValue ? JSON.parse(storedValue) : defaultValue
  
  const value = ref<T>(initialValue)
  
  const setValue = (newValue: T) => {
    value.value = newValue
    localStorage.setItem(key, JSON.stringify(newValue))
  }
  
  const removeValue = () => {
    localStorage.removeItem(key)
    value.value = defaultValue
  }
  
  watch(
    value,
    (newValue) => {
      localStorage.setItem(key, JSON.stringify(newValue))
    },
    { deep: true }
  )
  
  return {
    value,
    setValue,
    removeValue
  }
}

/**
 * 会话存储
 */
export function useSessionStorage<T>(key: string, defaultValue: T) {
  const storedValue = sessionStorage.getItem(key)
  const initialValue = storedValue ? JSON.parse(storedValue) : defaultValue
  
  const value = ref<T>(initialValue)
  
  const setValue = (newValue: T) => {
    value.value = newValue
    sessionStorage.setItem(key, JSON.stringify(newValue))
  }
  
  const removeValue = () => {
    sessionStorage.removeItem(key)
    value.value = defaultValue
  }
  
  watch(
    value,
    (newValue) => {
      sessionStorage.setItem(key, JSON.stringify(newValue))
    },
    { deep: true }
  )
  
  return {
    value,
    setValue,
    removeValue
  }
}

/**
 * 窗口大小监听
 */
export function useWindowSize() {
  const width = ref(window.innerWidth)
  const height = ref(window.innerHeight)
  
  const updateSize = () => {
    width.value = window.innerWidth
    height.value = window.innerHeight
  }
  
  onMounted(() => {
    window.addEventListener('resize', updateSize)
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', updateSize)
  })
  
  return {
    width: readonly(width),
    height: readonly(height)
  }
}

/**
 * 网络状态监听
 */
export function useOnlineStatus() {
  const isOnline = ref(navigator.onLine)
  
  const updateOnlineStatus = () => {
    isOnline.value = navigator.onLine
  }
  
  onMounted(() => {
    window.addEventListener('online', updateOnlineStatus)
    window.addEventListener('offline', updateOnlineStatus)
  })
  
  onUnmounted(() => {
    window.removeEventListener('online', updateOnlineStatus)
    window.removeEventListener('offline', updateOnlineStatus)
  })
  
  return {
    isOnline: readonly(isOnline)
  }
}

/**
 * 剪贴板操作
 */
export function useClipboard() {
  const isSupported = computed(() => {
    return navigator && 'clipboard' in navigator
  })
  
  const copy = async (text: string): Promise<boolean> => {
    if (!isSupported.value) {
      return false
    }
    
    try {
      await navigator.clipboard.writeText(text)
      ElMessage.success('复制成功')
      return true
    } catch (error) {
      ElMessage.error('复制失败')
      return false
    }
  }
  
  const read = async (): Promise<string> => {
    if (!isSupported.value) {
      throw new Error('剪贴板不支持')
    }
    
    try {
      return await navigator.clipboard.readText()
    } catch (error) {
      throw new Error('读取剪贴板失败')
    }
  }
  
  return {
    isSupported,
    copy,
    read
  }
}

/**
 * 确认对话框
 */
export function useConfirm() {
  const confirm = async (
    message: string,
    title = '确认',
    options: {
      confirmButtonText?: string
      cancelButtonText?: string
      type?: 'warning' | 'info' | 'success' | 'error'
    } = {}
  ): Promise<boolean> => {
    try {
      await ElMessageBox.confirm(message, title, {
        confirmButtonText: options.confirmButtonText || '确定',
        cancelButtonText: options.cancelButtonText || '取消',
        type: options.type || 'warning'
      })
      return true
    } catch {
      return false
    }
  }
  
  return {
    confirm
  }
}

/**
 * 通知消息
 */
export function useNotification() {
  const success = (message: string, title = '成功') => {
    ElNotification.success({
      title,
      message
    })
  }
  
  const error = (message: string, title = '错误') => {
    ElNotification.error({
      title,
      message
    })
  }
  
  const warning = (message: string, title = '警告') => {
    ElNotification.warning({
      title,
      message
    })
  }
  
  const info = (message: string, title = '信息') => {
    ElNotification.info({
      title,
      message
    })
  }
  
  return {
    success,
    error,
    warning,
    info
  }
}

/**
 * 文件上传
 */
export function useFileUpload() {
  const files = ref<File[]>([])
  const uploading = ref(false)
  const progress = ref(0)
  
  const selectFiles = (accept?: string, multiple = false): Promise<File[]> => {
    return new Promise((resolve) => {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = accept || '*'
      input.multiple = multiple
      
      input.onchange = (e) => {
        const target = e.target as HTMLInputElement
        const selectedFiles = Array.from(target.files || [])
        files.value = selectedFiles
        resolve(selectedFiles)
      }
      
      input.click()
    })
  }
  
  const uploadFile = async (
    file: File,
    uploadFn: (file: File, onProgress?: (progress: number) => void) => Promise<any>
  ) => {
    uploading.value = true
    progress.value = 0
    
    try {
      const result = await uploadFn(file, (p) => {
        progress.value = p
      })
      return result
    } finally {
      uploading.value = false
      progress.value = 0
    }
  }
  
  const clearFiles = () => {
    files.value = []
  }
  
  return {
    files: readonly(files),
    uploading: readonly(uploading),
    progress: readonly(progress),
    selectFiles,
    uploadFile,
    clearFiles
  }
}

/**
 * 拖拽排序
 */
export function useDragSort<T>(list: Ref<T[]>) {
  const draggedItem = ref<T | null>(null)
  const draggedIndex = ref(-1)
  
  const onDragStart = (item: T, index: number) => {
    draggedItem.value = item
    draggedIndex.value = index
  }
  
  const onDragOver = (e: DragEvent) => {
    e.preventDefault()
  }
  
  const onDrop = (targetIndex: number) => {
    if (draggedIndex.value === -1 || draggedIndex.value === targetIndex) {
      return
    }
    
    const items = [...list.value]
    const draggedItemValue = items[draggedIndex.value]
    
    // 移除拖拽的项目
    items.splice(draggedIndex.value, 1)
    
    // 插入到新位置
    items.splice(targetIndex, 0, draggedItemValue)
    
    list.value = items
    
    // 重置状态
    draggedItem.value = null
    draggedIndex.value = -1
  }
  
  const onDragEnd = () => {
    draggedItem.value = null
    draggedIndex.value = -1
  }
  
  return {
    draggedItem: readonly(draggedItem),
    draggedIndex: readonly(draggedIndex),
    onDragStart,
    onDragOver,
    onDrop,
    onDragEnd
  }
}

/**
 * 无限滚动
 */
export function useInfiniteScroll(
  loadMore: () => Promise<void>,
  options: {
    distance?: number
    disabled?: Ref<boolean>
  } = {}
) {
  const { distance = 100, disabled } = options
  const loading = ref(false)
  
  const checkScroll = async () => {
    if (disabled?.value || loading.value) {
      return
    }
    
    const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
    const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
    const clientHeight = document.documentElement.clientHeight || window.innerHeight
    
    if (scrollTop + clientHeight >= scrollHeight - distance) {
      loading.value = true
      try {
        await loadMore()
      } finally {
        loading.value = false
      }
    }
  }
  
  onMounted(() => {
    window.addEventListener('scroll', checkScroll)
  })
  
  onUnmounted(() => {
    window.removeEventListener('scroll', checkScroll)
  })
  
  return {
    loading: readonly(loading)
  }
}

/**
 * 路由查询参数
 */
export function useRouteQuery() {
  const route = useRoute()
  const router = useRouter()
  
  const getQuery = (key: string, defaultValue?: string): string => {
    const value = route.query[key]
    if (Array.isArray(value)) {
      return value[0] || defaultValue || ''
    }
    return value || defaultValue || ''
  }
  
  const setQuery = (params: Record<string, string | undefined>, replace = false) => {
    const query = { ...route.query }
    
    Object.entries(params).forEach(([key, value]) => {
      if (value === undefined) {
        delete query[key]
      } else {
        query[key] = value
      }
    })
    
    const method = replace ? 'replace' : 'push'
    router[method]({
      path: route.path,
      query
    })
  }
  
  return {
    getQuery,
    setQuery
  }
}

/**
 * 防抖
 */
export function useDebounce<T extends (...args: any[]) => any>(
  fn: T,
  delay = 300
) {
  let timeoutId: NodeJS.Timeout | null = null
  
  const debouncedFn = (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    timeoutId = setTimeout(() => {
      fn(...args)
    }, delay)
  }
  
  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }
  
  onUnmounted(() => {
    cancel()
  })
  
  return {
    debouncedFn,
    cancel
  }
}

/**
 * 节流
 */
export function useThrottle<T extends (...args: any[]) => any>(
  fn: T,
  delay = 300
) {
  let lastCall = 0
  
  const throttledFn = (...args: Parameters<T>) => {
    const now = Date.now()
    
    if (now - lastCall >= delay) {
      lastCall = now
      fn(...args)
    }
  }
  
  return {
    throttledFn
  }
}