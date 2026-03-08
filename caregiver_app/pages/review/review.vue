<template>
  <view class="container">
    <view class="header">
      <text class="title">评价护理员</text>
    </view>

    <view class="form-box">
      <!-- 护理员信息 -->
      <view class="caregiver-info" v-if="caregiverInfo">
        <image :src="caregiverInfo.avatar || '/static/default-avatar.png'" class="avatar" mode="aspectFill"></image>
        <view class="info">
          <text class="name">{{ caregiverInfo.name }}</text>
          <text class="hospital">{{ caregiverInfo.hospital }}</text>
        </view>
      </view>

      <!-- 评分 -->
      <view class="rating-section">
        <text class="section-title">服务评分（10分制）</text>
        <view class="rating-box">
          <view
            v-for="score in 10"
            :key="score"
            :class="['rating-item', rating >= score ? 'rating-active' : '']"
            @click="selectRating(score)"
          >
            <text class="rating-text">{{ score }}</text>
          </view>
        </view>
        <view class="rating-tips">
          <text class="tip-item">6分：及格</text>
          <text class="tip-item">8分：良好</text>
          <text class="tip-item">10分：非常满意</text>
        </view>
      </view>

      <!-- 评价内容 -->
      <view class="content-section">
        <text class="section-title">评价内容</text>
        <textarea
          class="content-input"
          v-model="content"
          placeholder="请输入您的评价内容（选填）"
          maxlength="500"
          :show-confirm-bar="false"
        ></textarea>
        <text class="char-count">{{ content.length }}/500</text>
      </view>

      <!-- 上传图片 -->
      <view class="image-section">
        <text class="section-title">上传图片（选填）</text>
        <view class="image-list">
          <view
            v-for="(img, index) in images"
            :key="index"
            class="image-item"
          >
            <image :src="img" class="image" mode="aspectFill"></image>
            <view class="image-delete" @click="deleteImage(index)">
              <text>×</text>
            </view>
          </view>
          <view class="image-add" @click="chooseImage" v-if="images.length < 6">
            <text class="add-icon">+</text>
            <text class="add-text">添加图片</text>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <button class="submit-btn" type="primary" @click="handleSubmit" :disabled="submitting">
        <text v-if="submitting">提交中...</text>
        <text v-else>提交评价</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const caregiverInfo = ref(null);
const contractId = ref(null);
const caregiverId = ref(null);
const userId = ref(null);
const rating = ref(0);
const content = ref('');
const images = ref([]);
const submitting = ref(false);

// 加载护理员信息
const loadCaregiverInfo = async () => {
  try {
    const res = await request({
      url: `/caregiver/${caregiverId.value}`,
      method: 'GET'
    });
    caregiverInfo.value = res;
  } catch (err) {
    console.error('加载护理员信息失败:', err);
  }
};

// 选择评分
const selectRating = (score) => {
  rating.value = score;
};

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 6 - images.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      images.value = images.value.concat(res.tempFilePaths);
    }
  });
};

// 删除图片
const deleteImage = (index) => {
  images.value.splice(index, 1);
};

// 提交评价
const handleSubmit = async () => {
  if (rating.value === 0) {
    uni.showToast({
      title: '请选择评分',
      icon: 'none'
    });
    return;
  }

  submitting.value = true;

  try {
    // TODO: 如果有图片，需要先上传图片到服务器，获取图片URL
    // 这里暂时使用本地路径
    const imagesJson = images.value.length > 0 ? JSON.stringify(images.value) : null;

    await request({
      url: '/review/create',
      method: 'POST',
      data: {
        contractId: contractId.value,
        caregiverId: caregiverId.value,
        userId: userId.value,
        rating: rating.value,
        content: content.value,
        images: imagesJson
      }
    });

    uni.showToast({
      title: '评价成功',
      icon: 'success'
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (err) {
    console.error('提交评价失败:', err);
    uni.showToast({
      title: '提交失败，请重试',
      icon: 'none'
    });
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  contractId.value = currentPage.options.contractId;
  caregiverId.value = currentPage.options.caregiverId;

  // 获取用户信息
  const user = uni.getStorageSync('userInfo');
  if (user) {
    userId.value = user.id;
  } else {
    uni.redirectTo({
      url: '/pages/login/login'
    });
    return;
  }

  if (!contractId.value || !caregiverId.value) {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
    return;
  }

  loadCaregiverInfo();
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

.form-box {
  padding: 20rpx;
}

/* 护理员信息 */
.caregiver-info {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50rpx;
  margin-right: 20rpx;
}

.info {
  flex: 1;
}

.name {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.hospital {
  display: block;
  font-size: 26rpx;
  color: #999;
}

/* 评分区域 */
.rating-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.rating-box {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.rating-item {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.rating-active {
  background: #ff9900;
}

.rating-text {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.rating-active .rating-text {
  color: #fff;
}

.rating-tips {
  display: flex;
  justify-content: space-around;
}

.tip-item {
  font-size: 24rpx;
  color: #999;
}

/* 评价内容 */
.content-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.content-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.5;
  margin-bottom: 10rpx;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
}

/* 图片上传 */
.image-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.image-item {
  width: 200rpx;
  height: 200rpx;
  position: relative;
}

.image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}

.image-delete {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  background: #ff4d4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}

.image-add {
  width: 200rpx;
  height: 200rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2rpx dashed #d9d9d9;
}

.add-icon {
  font-size: 60rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.add-text {
  font-size: 24rpx;
  color: #999;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  border: none;
}

.submit-btn[disabled] {
  opacity: 0.5;
}
</style>
