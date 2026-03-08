<template>
  <view class="container">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-box">
      <text>正在加载护理员详情...</text>
    </view>

    <!-- 错误提示 -->
    <view v-else-if="error" class="error-box">
      <text class="error-text">{{ error }}</text>
      <button type="warn" size="mini" @click="fetchDetail">重试</button>
    </view>

    <!-- 护理员详情 -->
    <view v-else-if="caregiver" class="detail-box">
      <!-- 头像和基本信息 -->
      <view class="header-section">
        <image
          :src="caregiver.avatar || '/static/logo.png'"
          class="avatar"
          mode="aspectFill"
        />

        <view class="header-info">
          <text class="name">{{ caregiver.name || '未知护理员' }}</text>
          <view class="rating-box">
            <text class="rating">⭐ {{ caregiver.ratingAvg || 0 }}分</text>
            <text class="review-count">({{ caregiver.reviewCount || 0 }}条评价)</text>
          </view>
          <text :class="['status', caregiver.status === 'available' ? 'status-available' : 'status-busy']">
            {{ caregiver.status === 'available' ? '空闲' : '护理中' }}
          </text>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="info-section">
        <text class="section-title">基本信息</text>
        <view class="info-item">
          <text class="info-label">性别：</text>
          <text class="info-value">{{ caregiver.gender === 'male' ? '男' : caregiver.gender === 'female' ? '女' : '未知' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">年龄：</text>
          <text class="info-value">{{ calculateAge(caregiver.birthDate) }}岁</text>
        </view>
        <view class="info-item" v-if="caregiver.city">
          <text class="info-label">所在城市：</text>
          <text class="info-value">{{ caregiver.province }} {{ caregiver.city }}</text>
        </view>
        <view class="info-item" v-if="caregiver.hospital">
          <text class="info-label">所属医院：</text>
          <text class="info-value">{{ caregiver.hospital }}</text>
        </view>
      </view>

      <!-- 个人简介 -->
      <view v-if="caregiver.intro" class="intro-section">
        <text class="section-title">个人简介</text>
        <text class="intro-text">{{ caregiver.intro }}</text>
      </view>

      <!-- 评价列表 -->
      <view class="review-section">
        <text class="section-title">用户评价</text>
        <view v-if="reviews.length > 0" class="review-list">
          <view v-for="review in reviews" :key="review.id" class="review-item">
            <view class="review-header">
              <text class="review-user">用户{{ review.userId }}</text>
              <text class="review-rating">{{ review.rating }}分</text>
            </view>
            <text class="review-content">{{ review.content }}</text>
            <view v-if="review.images" class="review-images">
              <image
                v-for="(img, index) in parseImages(review.images)"
                :key="index"
                :src="img"
                class="review-image"
                mode="aspectFill"
              />
            </view>
            <text class="review-time">{{ formatDate(review.createTime) }}</text>
          </view>
        </view>
        <view v-else class="empty-review">
          <text>暂无评价</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <button type="primary" class="action-btn" @click="handleContract" v-if="caregiver.status === 'available'">
          发起签约
        </button>
        <button type="default" class="action-btn" disabled v-else>
          护理中，暂不可签约
        </button>
        <button type="default" class="action-btn" @click="handleBack">返回列表</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const caregiver = ref(null);
const reviews = ref([]);
const loading = ref(true);
const error = ref('');
const caregiverId = ref(null);

// 获取护理员详情
const fetchDetail = async () => {
  loading.value = true;
  error.value = '';

  try {
    const res = await request({
      url: `/caregiver/${caregiverId.value}`,
      method: 'GET'
    });

    console.log('护理员详情数据:', res);
    caregiver.value = res;

    // 获取评价列表
    await fetchReviews();
  } catch (err) {
    error.value = '加载失败，请检查网络连接';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// 获取评价列表
const fetchReviews = async () => {
  try {
    const res = await request({
      url: `/review/caregiver/${caregiverId.value}`,
      method: 'GET'
    });
    reviews.value = res || [];
  } catch (err) {
    console.error('获取评价失败:', err);
  }
};

// 计算年龄
const calculateAge = (birthDate) => {
  if (!birthDate) return '未知';
  const birth = new Date(birthDate);
  const now = new Date();
  let age = now.getFullYear() - birth.getFullYear();
  const monthDiff = now.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birth.getDate())) {
    age--;
  }
  return age;
};

// 解析图片JSON
const parseImages = (imagesStr) => {
  try {
    return JSON.parse(imagesStr);
  } catch {
    return [];
  }
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 发起签约
const handleContract = async () => {
  // 检查是否登录
  const userInfo = uni.getStorageSync('userInfo');
  const userType = uni.getStorageSync('userType');

  if (!userInfo) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({
            url: '/pages/login/login'
          });
        }
      }
    });
    return;
  }

  // 检查用户类型
  if (userType !== 'client') {
    uni.showToast({
      title: '只有客户可以发起签约',
      icon: 'none'
    });
    return;
  }

  uni.showModal({
    title: '提示',
    content: '是否发起签约请求？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '提交中...' });

          await request({
            url: '/contract/request',
            method: 'POST',
            data: {
              clientId: userInfo.id,
              caregiverId: caregiverId.value
            }
          });

          uni.hideLoading();
          uni.showToast({
            title: '签约请求已发送',
            icon: 'success'
          });
        } catch (err) {
          uni.hideLoading();
          console.error('签约失败:', err);
          uni.showToast({
            title: '签约失败，请重试',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 返回列表
const handleBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  caregiverId.value = currentPage.options.id;

  if (!caregiverId.value) {
    error.value = '缺少护理员ID参数';
    loading.value = false;
    return;
  }

  fetchDetail();
});
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.loading-box {
  text-align: center;
  margin-top: 200rpx;
  color: #999;
  font-size: 28rpx;
}

.error-box {
  text-align: center;
  margin-top: 200rpx;
}

.error-text {
  color: #ff4d4f;
  display: block;
  margin-bottom: 20rpx;
  font-size: 28rpx;
}

.detail-box {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

/* 头部区域 */
.header-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  border: 4rpx solid #fff;
}

.header-info {
  flex: 1;
}

.name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 10rpx;
}

.rating-box {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.rating {
  color: #ffd700;
  font-size: 28rpx;
  font-weight: bold;
  margin-right: 10rpx;
}

.review-count {
  color: #fff;
  font-size: 24rpx;
}

.status {
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;
  display: inline-block;
}

.status-available {
  background: #52c41a;
  color: #fff;
}

.status-busy {
  background: #ff4d4f;
  color: #fff;
}

/* 信息区域 */
.info-section, .intro-section, .review-section {
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

.info-item {
  display: flex;
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

/* 简介区域 */
.intro-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  display: block;
}

/* 评价区域 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.review-item {
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.review-user {
  font-size: 26rpx;
  color: #666;
}

.review-rating {
  font-size: 26rpx;
  color: #ff9900;
  font-weight: bold;
}

.review-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  display: block;
  margin-bottom: 10rpx;
}

.review-images {
  display: flex;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.review-image {
  width: 150rpx;
  height: 150rpx;
  border-radius: 8rpx;
}

.review-time {
  font-size: 22rpx;
  color: #999;
  display: block;
}

.empty-review {
  text-align: center;
  color: #999;
  padding: 40rpx 0;
}

/* 操作按钮区域 */
.action-section {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.action-btn {
  flex: 1;
  border-radius: 12rpx;
}
</style>
