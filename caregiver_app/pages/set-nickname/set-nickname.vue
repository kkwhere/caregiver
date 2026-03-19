<template>
  <view class="container">
    <view class="form">
      <text class="title">请设置您的昵称</text>
      <text class="hint">昵称为必填项，将显示在您的个人资料中</text>
      <input
        class="input"
        v-model="nickname"
        placeholder="请输入昵称（必填）"
        maxlength="20"
        :focus="true"
      />
      <button
        class="submit-btn"
        type="primary"
        @click="handleSubmit"
        :disabled="!nickname.trim()"
      >
        确定并进入
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const nickname = ref('');

onMounted(() => {
  // 若有已保存的昵称可预填，用户可修改或直接确认
  try {
    const userInfo = uni.getStorageSync('userInfo');
    if (userInfo && userInfo.nickname) {
      nickname.value = userInfo.nickname;
    }
  } catch (e) {
    console.error('读取 userInfo 失败', e);
  }
});

const handleSubmit = async () => {
  const value = nickname.value.trim();
  if (!value) {
    uni.showToast({ title: '请输入昵称', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '提交中...' });

  try {
    const result = await request({
      url: '/user/nickname',
      method: 'PATCH',
      data: { nickname: value }
    });

    uni.hideLoading();
    if (!result || !result.success) {
      uni.showToast({
        title: result?.message || '提交失败，请重试',
        icon: 'none'
      });
      return;
    }

    if (result.user) {
      uni.setStorageSync('userInfo', result.user);
    }

    uni.showToast({
      title: '昵称设置成功',
      icon: 'success'
    });

    setTimeout(() => {
      const userType = uni.getStorageSync('userType') || 'client';
      const url = userType === 'caregiver'
        ? '/pages/caregiver-center/caregiver-center'
        : '/pages/list/list';
      uni.redirectTo({ url });
    }, 1500);
  } catch (err) {
    uni.hideLoading();
    console.error('提交昵称失败:', err);
    uni.showToast({
      title: err.message || '提交失败，请重试',
      icon: 'none'
    });
  }
};
</script>

<style scoped>
.container {
  padding: 60rpx 40rpx;
  background: #fff;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.form {
  width: 100%;
  max-width: 600rpx;
}
.title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 16rpx;
  color: #333;
}
.hint {
  display: block;
  font-size: 26rpx;
  color: #999;
  text-align: center;
  margin-bottom: 40rpx;
}
.input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 32rpx;
  margin-bottom: 60rpx;
  box-sizing: border-box;
}
.submit-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 32rpx;
}
.submit-btn[disabled] {
  opacity: 0.5;
}
</style>