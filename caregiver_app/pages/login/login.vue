<template>
  <view class="container">
    <view class="login-box">
      <!-- Logo -->
      <view class="logo-section">
        <image src="/static/logo.png" class="logo" mode="aspectFit"></image>
        <text class="app-name">陪伴家园</text>
        <text class="app-desc">专业护理服务平台</text>
      </view>

      <!-- 用户类型选择 -->
      <view class="type-section">
        <text class="type-title">请选择您的身份</text>
        <view class="type-buttons">
          <view
            :class="['type-btn', userType === 'client' ? 'type-btn-active' : '']"
            @click="selectUserType('client')"
          >
            <text class="type-icon">👤</text>
            <text class="type-text">我是客户</text>
            <text class="type-desc">需要护理服务</text>
          </view>
          <view
            :class="['type-btn', userType === 'caregiver' ? 'type-btn-active' : '']"
            @click="selectUserType('caregiver')"
          >
            <text class="type-icon">👨‍⚕️</text>
            <text class="type-text">我是护理员</text>
            <text class="type-desc">提供护理服务</text>
          </view>
        </view>
      </view>

      <!-- 登录按钮 -->
      <button
        class="login-btn"
        type="primary"
        @click="handleWxLogin"
        :disabled="!userType"
      >
        <text v-if="loading">登录中...</text>
        <text v-else>微信一键登录</text>
      </button>

      <!-- 提示信息 -->
      <view class="tips">
        <text class="tips-text">登录即表示同意</text>
        <text class="tips-link">《用户协议》</text>
        <text class="tips-text">和</text>
        <text class="tips-link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { request, saveToken } from '@/utils/request.js';

const userType = ref('');
const loading = ref(false);

// 选择用户类型
const selectUserType = (type) => {
  userType.value = type;
};

// 微信登录
const handleWxLogin = () => {
  if (!userType.value) {
    uni.showToast({
      title: '请先选择身份',
      icon: 'none'
    });
    return;
  }

  loading.value = true;

  // 微信登录
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      console.log('微信登录成功:', loginRes);

      // 获取用户信息
      uni.getUserInfo({
        provider: 'weixin',
        success: async (infoRes) => {
          console.log('获取用户信息成功:', infoRes);

          try {
            // 调用后端登录接口
            const result = await request({
              url: '/user/login',
              method: 'POST',
              data: {
                openid: loginRes.code, // 实际应该用code换取openid
                nickname: infoRes.userInfo.nickName,
                avatar: infoRes.userInfo.avatarUrl,
                userType: userType.value
              }
            });

            console.log('登录结果:', result);

            // 保存用户信息和token到本地
            uni.setStorageSync('userInfo', result.user);
            uni.setStorageSync('userType', userType.value);
            saveToken(result.token);

            uni.showToast({
              title: result.isNew ? '注册成功' : '登录成功',
              icon: 'success'
            });

            // 跳转到对应的页面
            setTimeout(() => {
              if (userType.value === 'caregiver') {
                uni.redirectTo({
                  url: '/pages/caregiver-center/caregiver-center'
                });
              } else {
                uni.redirectTo({
                  url: '/pages/list/list'
                });
              }
            }, 1500);
          } catch (err) {
            console.error('登录失败:', err);
            uni.showToast({
              title: '登录失败，请重试',
              icon: 'none'
            });
          } finally {
            loading.value = false;
          }
        },
        fail: (err) => {
          console.error('获取用户信息失败:', err);
          uni.showToast({
            title: '获取用户信息失败',
            icon: 'none'
          });
          loading.value = false;
        }
      });
    },
    fail: (err) => {
      console.error('微信登录失败:', err);
      uni.showToast({
        title: '微信登录失败',
        icon: 'none'
      });
      loading.value = false;
    }
  });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-box {
  width: 100%;
  background: #fff;
  border-radius: 32rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
}

/* Logo区域 */
.logo-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 20rpx;
}

.app-name {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.app-desc {
  display: block;
  font-size: 28rpx;
  color: #999;
}

/* 用户类型选择 */
.type-section {
  margin-bottom: 60rpx;
}

.type-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 30rpx;
}

.type-buttons {
  display: flex;
  gap: 20rpx;
}

.type-btn {
  flex: 1;
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 40rpx 20rpx;
  text-align: center;
  transition: all 0.3s;
  border: 3rpx solid transparent;
}

.type-btn-active {
  background: #e8f4ff;
  border-color: #667eea;
}

.type-icon {
  display: block;
  font-size: 60rpx;
  margin-bottom: 15rpx;
}

.type-text {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.type-desc {
  display: block;
  font-size: 24rpx;
  color: #999;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  border: none;
  margin-bottom: 30rpx;
}

.login-btn[disabled] {
  opacity: 0.5;
}

/* 提示信息 */
.tips {
  text-align: center;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
}

.tips-link {
  font-size: 24rpx;
  color: #667eea;
}
</style>
