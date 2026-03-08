<template>
  <view class="container">
    <view class="header">
      <text class="title">消息通知</text>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-box">
      <text>正在加载消息...</text>
    </view>

    <!-- 消息列表 -->
    <view v-else-if="messages.length > 0" class="message-list">
      <view
        v-for="message in messages"
        :key="message.id"
        :class="['message-item', message.isRead === 0 ? 'message-unread' : '']"
        @click="handleMessageClick(message)"
      >
        <view class="message-icon">
          <text>{{ getMessageIcon(message.messageType) }}</text>
        </view>
        <view class="message-content">
          <text class="message-title">{{ getMessageTitle(message.messageType) }}</text>
          <text class="message-text">{{ message.content }}</text>
          <text class="message-time">{{ formatDate(message.createTime) }}</text>
        </view>
        <view v-if="message.isRead === 0" class="unread-dot"></view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-box">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无消息</text>
    </view>

    <!-- 底部操作栏 -->
    <view class="footer" v-if="messages.length > 0">
      <button class="footer-btn" size="mini" @click="markAllRead">全部标记为已读</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const messages = ref([]);
const loading = ref(true);
const userInfo = ref({});

// 加载消息列表
const loadMessages = async () => {
  loading.value = true;

  try {
    const user = uni.getStorageSync('userInfo');
    if (!user) {
      uni.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }

    userInfo.value = user;

    const res = await request({
      url: `/message/user/${user.id}`,
      method: 'GET'
    });

    messages.value = res || [];
  } catch (err) {
    console.error('加载消息失败:', err);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 获取消息图标
const getMessageIcon = (type) => {
  const iconMap = {
    contract_request: '📝',
    contract_accept: '✅',
    contract_reject: '❌',
    service_end_request: '🏁',
    service_end_accept: '✅',
    service_end_reject: '❌'
  };
  return iconMap[type] || '📬';
};

// 获取消息标题
const getMessageTitle = (type) => {
  const titleMap = {
    contract_request: '签约请求',
    contract_accept: '签约已接受',
    contract_reject: '签约已拒绝',
    service_end_request: '结束服务请求',
    service_end_accept: '服务已结束',
    service_end_reject: '拒绝结束服务'
  };
  return titleMap[type] || '系统消息';
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚';
  }
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`;
  }
  // 小于1天
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`;
  }
  // 小于7天
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`;
  }

  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 点击消息
const handleMessageClick = async (message) => {
  // 标记为已读
  if (message.isRead === 0) {
    try {
      await request({
        url: `/message/read/${message.id}`,
        method: 'PUT'
      });
      message.isRead = 1;
    } catch (err) {
      console.error('标记已读失败:', err);
    }
  }

  // 跳转到签约详情
  if (message.contractId) {
    uni.navigateTo({
      url: `/pages/contract-detail/contract-detail?id=${message.contractId}`
    });
  }
};

// 全部标记为已读
const markAllRead = async () => {
  const unreadIds = messages.value.filter(m => m.isRead === 0).map(m => m.id);

  if (unreadIds.length === 0) {
    uni.showToast({
      title: '没有未读消息',
      icon: 'none'
    });
    return;
  }

  try {
    await request({
      url: '/message/read-batch',
      method: 'PUT',
      data: unreadIds
    });

    messages.value.forEach(m => {
      m.isRead = 1;
    });

    uni.showToast({
      title: '已全部标记为已读',
      icon: 'success'
    });
  } catch (err) {
    console.error('标记已读失败:', err);
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    });
  }
};

onMounted(() => {
  loadMessages();
});
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background: #fff;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.loading-box {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}

/* 消息列表 */
.message-list {
  padding: 20rpx;
}

.message-item {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: flex-start;
  position: relative;
}

.message-unread {
  background: #e8f4ff;
}

.message-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.message-content {
  flex: 1;
}

.message-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.message-text {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 10rpx;
}

.message-time {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  background: #ff4d4f;
  border-radius: 50%;
  position: absolute;
  top: 30rpx;
  right: 30rpx;
}

/* 空状态 */
.empty-box {
  text-align: center;
  padding: 150rpx 0;
}

.empty-icon {
  display: block;
  font-size: 120rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 底部操作栏 */
.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  justify-content: center;
}

.footer-btn {
  background: #667eea;
  color: #fff;
  border: none;
}
</style>
