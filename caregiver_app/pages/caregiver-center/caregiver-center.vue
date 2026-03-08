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

    <!-- 护理员信息卡片 -->
    <view class="info-card" v-if="caregiverInfo">
      <view class="card-header">
        <text class="card-title">护理员信息</text>
        <button class="edit-btn" size="mini" type="primary" @click="handleEdit">编辑资料</button>
      </view>
      <view class="info-item">
        <text class="info-label">姓名：</text>
        <text class="info-value">{{ caregiverInfo.name || '未设置' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">性别：</text>
        <text class="info-value">{{ caregiverInfo.gender === 'male' ? '男' : caregiverInfo.gender === 'female' ? '女' : '未设置' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">年龄：</text>
        <text class="info-value">{{ calculateAge(caregiverInfo.birthDate) }}岁</text>
      </view>
      <view class="info-item">
        <text class="info-label">所在城市：</text>
        <text class="info-value">{{ caregiverInfo.city || '未设置' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">所属医院：</text>
        <text class="info-value">{{ caregiverInfo.hospital || '未设置' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">状态：</text>
        <text :class="['status', caregiverInfo.status === 'available' ? 'status-available' : 'status-busy']">
          {{ caregiverInfo.status === 'available' ? '空闲' : '护理中' }}
        </text>
      </view>
      <view class="info-item">
        <text class="info-label">评分：</text>
        <text class="rating">⭐ {{ caregiverInfo.ratingAvg || 0 }}分 ({{ caregiverInfo.reviewCount || 0 }}条评价)</text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
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
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const userInfo = ref({});
const caregiverInfo = ref(null);
const unreadMessageCount = ref(0);
const unreadContractCount = ref(0);

// 加载用户信息
const loadUserInfo = () => {
  const user = uni.getStorageSync('userInfo');
  if (user) {
    userInfo.value = user;
    loadCaregiverInfo();
    loadUnreadCounts();
  } else {
    // 未登录，跳转到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    });
  }
};

// 加载护理员信息
const loadCaregiverInfo = async () => {
  try {
    const res = await request({
      url: `/caregiver/user/${userInfo.value.id}`,
      method: 'GET'
    });
    caregiverInfo.value = res;
  } catch (err) {
    console.error('加载护理员信息失败:', err);
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
    const contracts = await request({
      url: `/contract/caregiver/${caregiverInfo.value?.id || 0}`,
      method: 'GET'
    });
    unreadContractCount.value = contracts.filter(c => c.status === 'pending').length;
  } catch (err) {
    console.error('加载未读数量失败:', err);
  }
};

// 计算年龄
const calculateAge = (birthDate) => {
  if (!birthDate) return '未设置';
  const birth = new Date(birthDate);
  const now = new Date();
  let age = now.getFullYear() - birth.getFullYear();
  const monthDiff = now.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birth.getDate())) {
    age--;
  }
  return age;
};

// 编辑资料
const handleEdit = () => {
  uni.navigateTo({
    url: '/pages/caregiver-edit/caregiver-edit'
  });
};

// 查看签约
const goToContracts = () => {
  uni.navigateTo({
    url: '/pages/contract-list/contract-list?type=caregiver'
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
    url: `/pages/review-list/review-list?caregiverId=${caregiverInfo.value.id}`
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

/* 信息卡片 */
.info-card {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.edit-btn {
  background: #667eea;
  color: #fff;
  border: none;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 15rpx;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 28rpx;
  color: #999;
  min-width: 160rpx;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.status {
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 24rpx;
}

.status-available {
  background: #52c41a;
  color: #fff;
}

.status-busy {
  background: #ff4d4f;
  color: #fff;
}

.rating {
  font-size: 28rpx;
  color: #ff9900;
  font-weight: bold;
}

/* 菜单区域 */
.menu-section {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
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
</style>
