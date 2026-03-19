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
        :disabled="!userType || loading"
      >
        <text v-if="loading">登录中...</text>
        <text v-else>微信一键登录</text>
      </button>

      <!-- ✅ 修改部分：隐私协议勾选框 -->
      <view class="agreement-section">
        <label class="agreement-label">
          <checkbox-group @change="onAgreeChange">
                <checkbox 
                  value="agree" 
                  color="#667eea"
                  style="transform: scale(0.8); margin-right: 10rpx;"
                />
          </checkbox-group>
          <text class="agreement-text">我已阅读并同意</text>
          <text class="agreement-link" @click.stop="openPage('/pages/protocol/user/user')">《用户协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link" @click.stop="openPage('/pages/protocol/privacy/privacy')">《隐私政策》</text>
        </label>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { request, saveToken } from '@/utils/request.js';

const userType = ref('');
const agreed = ref(false); // ✅ 默认必须为 false
const loading = ref(false);

// 选择用户类型
const selectUserType = (type) => {
  userType.value = type;
};

// 监听勾选框变化
const onAgreeChange = (e) => {

    console.log('checkbox change:', e.detail.value);
    agreed.value = e.detail.value.includes('agree');
    console.log('agreed.value:', agreed.value);
  
};

// 打开协议页面（需要根据你实际的路径修改）
const openPage = (url) => {
  uni.navigateTo({ url });
};

// 微信登录
const handleWxLogin = () => {
  // 1. 基础校验：身份选择
  if (!userType.value) {
    uni.showToast({ title: '请先选择身份', icon: 'none' });
    return;
  }

  // ✅ 核心合规校验：必须检查是否勾选协议
  if (!agreed.value) {
    uni.showModal({
      title: '提示',
      content: '请您先勾选并阅读《用户协议》和《隐私政策》',
      showCancel: false,
      confirmText: '我知道了'
    });
    return;
  }

  loading.value = true;

  // 2. 调用 getUserProfile (必须由用户点击直接触发)
  uni.getUserProfile({
    desc: '用于获取昵称头像完善资料',
    success: (profileRes) => {
      const { nickName, avatarUrl } = profileRes.userInfo;

      // 3. 授权成功后，再调用 uni.login 获取 code
      uni.login({
        provider: 'weixin',
        success: (loginRes) => {
          sendLoginRequest(loginRes.code, nickName, avatarUrl);
        },
        fail: (loginErr) => {
          console.error('微信登录失败:', loginErr);
          loading.value = false;
          uni.showToast({ title: '获取登录凭证失败', icon: 'none' });
        }
      });
    },
    fail: (authErr) => {
      console.error('用户拒绝授权:', authErr);
      loading.value = false;
      if (authErr.errMsg && authErr.errMsg.includes('cancel')) {
        uni.showToast({ title: '您取消了授权', icon: 'none' });
      } else {
        uni.showModal({
          title: '提示',
          content: '登录需要获取您的公开信息，请点击允许。若已拒绝，请删除小程序后重新进入。',
          showCancel: false
        });
      }
    }
  });
};

// 封装发送请求的逻辑
const sendLoginRequest = async (code, nickname, avatar) => {
  try {
    const result = await request({
      url: '/user/login',
      method: 'POST',
      data: {
        code: code,
        nickname: nickname,
        avatar: avatar,
        userType: userType.value
      }
    });

    // 保存登录信息
    uni.setStorageSync('userInfo', result.user);
    uni.setStorageSync('userType', userType.value);
    if (result.token) {
      saveToken(result.token);
    }

    uni.showToast({
      title: result.isNew ? '注册成功' : '登录成功',
      icon: 'success'
    });

    // 登录后统一进入设置昵称页，必须输入昵称后再进入护理员列表
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/set-nickname/set-nickname' });
    }, 1500);

  } catch (err) {
    console.error('登录请求异常:', err);
    uni.showToast({
      title: err.message || '登录失败，请重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
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
  display: flex;
  flex-direction: column;
  align-items: center;
}
.logo-section { text-align: center; margin-bottom: 60rpx; }
.logo { width: 160rpx; height: 160rpx; margin-bottom: 20rpx; }
.app-name { display: block; font-size: 48rpx; font-weight: bold; color: #333; margin-bottom: 10rpx; }
.app-desc { display: block; font-size: 28rpx; color: #999; }
.type-section { margin-bottom: 60rpx; width: 100%; }
.type-title { display: block; font-size: 32rpx; font-weight: bold; color: #333; text-align: center; margin-bottom: 30rpx; }
.type-buttons { display: flex; gap: 20rpx; }
.type-btn { flex: 1; background: #f5f5f5; border-radius: 16rpx; padding: 40rpx 20rpx; text-align: center; transition: all 0.3s; border: 3rpx solid transparent; }
.type-btn-active { background: #e8f4ff; border-color: #667eea; }
.type-icon { display: block; font-size: 60rpx; margin-bottom: 15rpx; }
.type-text { display: block; font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 8rpx; }
.type-desc { display: block; font-size: 24rpx; color: #999; }
.login-btn { width: 100%; height: 90rpx; line-height: 90rpx; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 45rpx; font-size: 32rpx; font-weight: bold; color: #fff; border: none; margin-bottom: 20rpx; }
.login-btn[disabled] { opacity: 0.5; }

/* ✅ 新增：协议区域样式 */
.agreement-section {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 10rpx;
}
.agreement-label {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
}
.agreement-text {
  color: #999;
  margin: 0 4rpx;
}
.agreement-link {
  color: #667eea;
  text-decoration: underline;
  padding: 0 4rpx;
  /* 防止点击穿透或事件冲突 */
  position: relative; 
  z-index: 10;
}
</style>