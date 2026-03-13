// 检测是否为移动设备
export const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

// 获取首页路径（移动端返回 /m，PC 端返回 /）
export const getHomePath = () => {
  return isMobile() ? '/m' : '/'
}
