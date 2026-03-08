<template>
  <view class="container">
    <!-- 头部信息 -->
    <view class="header-section">
      <image :src="userInfo.avatar || '/static/default-avatar.png'" class="avatar" mode="aspectFill"></image>
      <view class="header-info">
        <text class="nickname">{{ userInfo.nickname || '未设置昵称' }}</text>
        <text class="phone">{{ userInfo.phone || '未绑定手机' }}</text>
      </view>
      <button class="logout-btn" size="mini" @click="handleLogout">退出</button>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-item" @click="goToHome">
        <text class="menu-icon">🏠</text>
        <text class="menu-text">浏览护理员</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToContracts">
        <text class="menu-icon">📋</text>
        <text class="menu-text">我的签约</text>
        <text class="menu-badge" v-if="unreadContractCount > 0">{{ unreadContractCount }}</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToMessages">
        <text class="menu-icon">💬</text>
        <text class="menu-text">消息通知</text>
        <text class="menu-badge" v-if="unreadMessageCount > 0">{{ unreadMessageCount }}</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToReviews">
        <text class="menu-icon">⭐</text>
        <text class="menu-text">我的评价</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 我的签约列表 -->
    <view class="contract-section">
      <text class="section-title">最近签约</text>
      <view v-if="contracts.length > 0" class="contract-list">
        <view
          v-for="contract in contracts"
          :key="contract.id"
          class="contract-item"
          @click="goToContractDetail(contract.id)"
        >
          <view class="contract-header">
            <text class="contract-caregiver">护理员ID: {{ contract.caregiverId }}</text>
            <text :class="['contract-status', getStatusClass(contract.status)]">
              {{ getStatusText(contract.status) }}
            </text>
          </view>
          <text class="contract-time">创建时间: {{ formatDate(contract.createTime) }}</text>
        </view>
      </view>
      <view v-else class="empty-box">
        <text>暂无签约记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const userInfo = ref({});
const contracts = ref([]);
const unreadMessageCount = ref(0);
const unreadContractCount = ref(0);

// 加载用户信息
const loadUserInfo = () => {
  const user = uni.getStorageSync('userInfo');
  if (user) {
    userInfo.value = user;
    loadContracts();
    loadUnreadCounts();
  } else {
    // 未登录，跳转到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    });
  }
};

// 加载签约列表
const loadContracts = async () => {
  try {
    const res = await request({
      url: `/contract/client/${userInfo.value.id}`,
      method: 'GET'
    });
    contracts.value = res.slice(0, 5); // 只显示最近5条
  } catch (err) {
    console.error('加载签约列表失败:', err);
  }
};

// 加载未读数量
const loadUnreadCounts = async () => {
  try {
    // 获取未读消息数量
    const messages = await request({
      url: `/message/unread/${userInfo.value.id}`,
      method: 'GET'
    });
    unreadMessageCount.value = messages.length;

    // 获取待处理的签约数量
    unreadContractCount.value = contracts.value.filter(c => c.status === 'end_requested').length;
  } catch (err) {
    console.error('加载未读数量失败:', err);
  }
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待接受',
    accepted: '已接受',
    rejected: '已拒绝',
    in_service: '服务中',
    end_requested: '待确认结束',
    completed: '已完成',
    cancelled: '已取消'
  };
  return statusMap[status] || status;
};

// 获取状态样式类
const getStatusClass = (status) => {
  if (status === 'in_service') return 'status-in-service';
  if (status === 'completed') return 'status-completed';
  if (status === 'pending' || status === 'end_requested') return 'status-pending';
  return 'status-default';
};

// 跳转到首页
const goToHome = () => {
  uni.switchTab({
    url: '/pages/list/list'
  });
};

// 查看签约
const goToContracts = () => {
  uni.navigateTo({
    url: '/pages/contract-list/contract-list?type=client'
  });
};

// 查看消息
const goToMessages = () => {
  uni.navigateTo({
    url: '/pages/message/message'
  });
};

// 查看评价
const goToReviews = () => {
  uni.navigateTo({
    url: `/pages/review-list/review-list?userId=${userInfo.value.id}`
  });
};

// 查看签约详情
const goToContractDetail = (contractId) => {
  uni.navigateTo({
    url: `/pages/contract-detail/contract-detail?id=${contractId}`
  });
};

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('userInfo');
        uni.removeStorageSync('userType');
        uni.redirectTo({
          url: '/pages/login/login'
        });
      }
    }
  });
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

/* 头部区域 */
.header-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  border: 4rpx solid #fff;
  margin-right: 20rpx;
}

.header-info {
  flex: 1;
}

.nickname {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.phone {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
}

/* 菜单区域 */
.menu-section {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.menu-badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 20rpx;
  margin-right: 10rpx;
}

.menu-arrow {
  font-size: 40rpx;
  color: #ccc;
}

/* 签约列表 */
.contract-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.contract-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.contract-item {
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.contract-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.contract-caregiver {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.contract-status {
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;
}

.status-in-service {
  background: #1890ff;
  color: #fff;
}

.status-completed {
  background: #52c41a;
  color: #fff;
}

.status-pending {
  background: #faad14;
  color: #fff;
}

.status-default {
  background: #d9d9d9;
  color: #666;
}

.contract-time {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.empty-box {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
}
</style>
