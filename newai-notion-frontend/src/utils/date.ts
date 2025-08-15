/**
 * 日期工具函数
 */

/**
 * 格式化相对时间
 * @param date 日期
 * @returns 相对时间字符串
 */
export function formatRelativeTime(date: Date | string): string {
  const now = new Date()
  const targetDate = typeof date === 'string' ? new Date(date) : date
  const diffInSeconds = Math.floor((now.getTime() - targetDate.getTime()) / 1000)

  // 未来时间
  if (diffInSeconds < 0) {
    return '刚刚'
  }

  // 小于1分钟
  if (diffInSeconds < 60) {
    return '刚刚'
  }

  // 小于1小时
  if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60)
    return `${minutes}分钟前`
  }

  // 小于1天
  if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600)
    return `${hours}小时前`
  }

  // 小于7天
  if (diffInSeconds < 604800) {
    const days = Math.floor(diffInSeconds / 86400)
    return `${days}天前`
  }

  // 小于30天
  if (diffInSeconds < 2592000) {
    const weeks = Math.floor(diffInSeconds / 604800)
    return `${weeks}周前`
  }

  // 小于1年
  if (diffInSeconds < 31536000) {
    const months = Math.floor(diffInSeconds / 2592000)
    return `${months}个月前`
  }

  // 超过1年
  const years = Math.floor(diffInSeconds / 31536000)
  return `${years}年前`
}

/**
 * 格式化日期
 * @param date 日期
 * @param format 格式字符串
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: Date | string, format = 'YYYY-MM-DD'): string {
  const targetDate = typeof date === 'string' ? new Date(date) : date
  
  const year = targetDate.getFullYear()
  const month = String(targetDate.getMonth() + 1).padStart(2, '0')
  const day = String(targetDate.getDate()).padStart(2, '0')
  const hours = String(targetDate.getHours()).padStart(2, '0')
  const minutes = String(targetDate.getMinutes()).padStart(2, '0')
  const seconds = String(targetDate.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期时间
 * @param date 日期
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(date: Date | string): string {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化时间
 * @param date 日期
 * @returns 格式化后的时间字符串
 */
export function formatTime(date: Date | string): string {
  return formatDate(date, 'HH:mm:ss')
}

/**
 * 判断是否为今天
 * @param date 日期
 * @returns 是否为今天
 */
export function isToday(date: Date | string): boolean {
  const today = new Date()
  const targetDate = typeof date === 'string' ? new Date(date) : date
  
  return (
    today.getFullYear() === targetDate.getFullYear() &&
    today.getMonth() === targetDate.getMonth() &&
    today.getDate() === targetDate.getDate()
  )
}

/**
 * 判断是否为昨天
 * @param date 日期
 * @returns 是否为昨天
 */
export function isYesterday(date: Date | string): boolean {
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  const targetDate = typeof date === 'string' ? new Date(date) : date
  
  return (
    yesterday.getFullYear() === targetDate.getFullYear() &&
    yesterday.getMonth() === targetDate.getMonth() &&
    yesterday.getDate() === targetDate.getDate()
  )
}

/**
 * 判断是否为本周
 * @param date 日期
 * @returns 是否为本周
 */
export function isThisWeek(date: Date | string): boolean {
  const now = new Date()
  const targetDate = typeof date === 'string' ? new Date(date) : date
  
  const startOfWeek = new Date(now)
  startOfWeek.setDate(now.getDate() - now.getDay())
  startOfWeek.setHours(0, 0, 0, 0)
  
  const endOfWeek = new Date(startOfWeek)
  endOfWeek.setDate(startOfWeek.getDate() + 6)
  endOfWeek.setHours(23, 59, 59, 999)
  
  return targetDate >= startOfWeek && targetDate <= endOfWeek
}

/**
 * 获取友好的日期显示
 * @param date 日期
 * @returns 友好的日期字符串
 */
export function getFriendlyDate(date: Date | string): string {
  const targetDate = typeof date === 'string' ? new Date(date) : date
  
  if (isToday(targetDate)) {
    return `今天 ${formatTime(targetDate)}`
  }
  
  if (isYesterday(targetDate)) {
    return `昨天 ${formatTime(targetDate)}`
  }
  
  if (isThisWeek(targetDate)) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return `${weekdays[targetDate.getDay()]} ${formatTime(targetDate)}`
  }
  
  return formatDate(targetDate, 'MM-DD HH:mm')
}

/**
 * 计算两个日期之间的天数差
 * @param date1 日期1
 * @param date2 日期2
 * @returns 天数差
 */
export function getDaysDiff(date1: Date | string, date2: Date | string): number {
  const d1 = typeof date1 === 'string' ? new Date(date1) : date1
  const d2 = typeof date2 === 'string' ? new Date(date2) : date2
  
  const diffTime = Math.abs(d2.getTime() - d1.getTime())
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

/**
 * 获取日期范围的描述
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 日期范围描述
 */
export function getDateRangeDescription(startDate: Date | string, endDate: Date | string): string {
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate
  const end = typeof endDate === 'string' ? new Date(endDate) : endDate
  
  const daysDiff = getDaysDiff(start, end)
  
  if (daysDiff === 0) {
    return getFriendlyDate(start)
  }
  
  if (daysDiff === 1) {
    return `${getFriendlyDate(start)} - ${getFriendlyDate(end)}`
  }
  
  return `${formatDate(start)} - ${formatDate(end)} (${daysDiff}天)`
}