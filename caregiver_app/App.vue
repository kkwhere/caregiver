<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
    
    // ========== 新增：全局开启分享菜单 ==========
    uni.showShareMenu({
      menus: ['shareAppMessage', 'shareTimeline'], // 朋友+朋友圈
      withShareTicket: true,
      fail: (err) => {
        console.log('开启分享菜单失败：', err)
      }
    })
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  },
  
  // ========== 新增：全局分享给朋友（所有页面生效） ==========
  onShareAppMessage() {
    // 获取当前页面路径+参数（保证分享后打开当前页）
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    let path = `/${currentPage.route}`
    
    // 拼接参数（比如?id=123）
    if (Object.keys(currentPage.options).length > 0) {
      const query = Object.entries(currentPage.options)
        .map(([k, v]) => `${k}=${v}`)
        .join('&')
      path += `?${query}`
    }
    
    return {
      title: '陪伴家园 - 专业护理员匹配平台', // 分享标题
      path: path, // 分享当前页面
      imageUrl: '/static/logo.png' // 封面图（用你现有的logo）
    }
  },
  
  // ========== 新增：全局分享到朋友圈（所有页面生效） ==========
  onShareTimeline() {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    let query = ''
    
    if (Object.keys(currentPage.options).length > 0) {
      query = Object.entries(currentPage.options)
        .map(([k, v]) => `${k}=${v}`)
        .join('&')
    }
    
    return {
      title: '陪伴家园 - 专业护理员匹配平台',
      query: query, // 朋友圈分享参数
      imageUrl: '/static/logo.png'
    }
  }
}
</script>

<style>
/*每个页面公共css */
/* 新增：解决页面可拖动问题的全局样式 */
page {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>