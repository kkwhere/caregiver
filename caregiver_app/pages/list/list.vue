<template>
  <view class="container">
    <text class="title">护工列表</text>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-box">
      <text>正在加载数据...</text>
    </view>

    <!-- 错误提示 -->
    <view v-else-if="error" class="error-box">
      <text class="error-text">{{ error }}</text>
      <button type="warn" size="mini" @click="fetchList">重试</button>
    </view>

    <!-- 数据列表 -->
    <view v-else-if="caregiverList.length > 0" class="list-box">
      <view 
        v-for="(item, index) in caregiverList" 
        :key="item.id" 
        class="card"
        @click="handleClick(item)"
      >
        <view class="card-header">
          <text class="name">{{ item.name || '未知护工' }}</text>
          <text class="rating">⭐ {{ item.rating || 0 }}分</text>
        </view>
        <text class="skill">{{ item.skill || '暂无技能描述' }}</text>
        <text class="id-info">ID: {{ item.id }}</text>
      </view>
    </view>

    <!-- 空数据 -->
    <view v-else class="empty-box">
      <text>暂无护工数据</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

 import { request } from '@/utils/request.js'; 

const caregiverList = ref([]);
const loading = ref(true);
const error = ref('');

// 获取数据方法
const fetchList = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    // 方案 A：如果你创建了 utils/request.js
     const res = await request({ url: '/caregiver/list', method: 'GET' });
    
    console.log('后端返回数据:', res);
    
    if (Array.isArray(res)) {
      caregiverList.value = res;
    } else {
      caregiverList.value = res; 
    }
  } catch (err) {
    error.value = '加载失败，请检查后端是否启动';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleClick = (item) => {
  uni.showToast({
    title: `点击了：${item.name}`,
    icon: 'none'
  });
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
  text-align: center;
  margin-bottom: 30rpx;
  color: #333;
}
.loading-box, .empty-box {
  text-align: center;
  margin-top: 100rpx;
  color: #999;
}
.error-box {
  text-align: center;
  margin-top: 100rpx;
}
.error-text {
  color: #ff4d4f;
  display: block;
  margin-bottom: 20rpx;
}
.list-box {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.card {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}
.name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.rating {
  color: #ff9900;
  font-size: 28rpx;
  font-weight: bold;
}
.skill {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}
.id-info {
  font-size: 24rpx;
  color: #ccc;
}
</style>