<template>
  <view class="container">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-box">
      <text>正在加载签约详情...</text>
    </view>

    <!-- 签约详情 -->
    <view v-else-if="contract" class="detail-box">
      <!-- 状态卡片 -->
      <view class="status-card">
        <text :class="['status-text', getStatusClass(contract.status)]">
          {{ getStatusText(contract.status) }}
        </text>
        <text class="status-desc">{{ getStatusDesc(contract.status) }}</text>
      </view>

      <!-- 护理员信息 -->
      <view class="info-card" v-if="caregiverInfo">
        <text class="card-title">护理员信息</text>
        <view class="caregiver-box">
          <image :src="caregiverInfo.avatar || '/static/default-avatar.png'" class="avatar" mode="aspectFill"></image>
          <view class="caregiver-info">
            <text class="name">{{ caregiverInfo.name }}</text>
            <text class="hospital">{{ caregiverInfo.hospital }}</text>
            <text class="rating">⭐ {{ caregiverInfo.ratingAvg || 0 }}分</text>
          </view>
        </view>
      </view>

      <!-- 签约信息 -->
      <view class="info-card">
        <text class="card-title">签约信息</text>
        <view class="info-item">
          <text class="info-label">签约编号：</text>
          <text class="info-value">{{ contract.id }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">请求时间：</text>
          <text class="info-value">{{ formatDate(contract.requestTime) }}</text>
        </view>
        <view class="info-item" v-if="contract.acceptTime">
          <text class="info-label">接受时间：</text>
          <text class="info-value">{{ formatDate(contract.acceptTime) }}</text>
        </view>
        <view class="info-item" v-if="contract.startTime">
          <text class="info-label">开始时间：</text>
          <text class="info-value">{{ formatDate(contract.startTime) }}</text>
        </view>
        <view class="info-item" v-if="contract.endTime">
          <text class="info-label">结束时间：</text>
          <text class="info-value">{{ formatDate(contract.endTime) }}</text>
        </view>
        <view class="info-item" v-if="contract.remark">
          <text class="info-label">备注：</text>
          <text class="info-value">{{ contract.remark }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <!-- 护理员操作 -->
        <template v-if="userType === 'caregiver'">
          <button
            v-if="contract.status === 'pending'"
            class="action-btn accept-btn"
            @click="handleAccept"
          >
            接受签约
          </button>
          <button
            v-if="contract.status === 'pending'"
            class="action-btn reject-btn"
            @click="handleReject"
          >
            拒绝签约
          </button>
          <button
            v-if="contract.status === 'in_service'"
            class="action-btn"
            type="warn"
            @click="handleEndRequest"
          >
            请求结束服务
          </button>
        </template>

        <!-- 客户操作 -->
        <template v-if="userType === 'client'">
          <button
            v-if="contract.status === 'end_requested'"
            class="action-btn accept-btn"
            @click="handleEndAccept"
          >
            确认结束服务
          </button>
          <button
            v-if="contract.status === 'end_requested'"
            class="action-btn reject-btn"
            @click="handleEndReject"
          >
            拒绝结束服务
          </button>
          <button
            v-if="contract.status === 'completed' && !hasReview"
            class="action-btn"
            type="primary"
            @click="goToReview"
          >
            评价护理员
          </button>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const contract = ref(null);
const caregiverInfo = ref(null);
const loading = ref(true);
const contractId = ref(null);
const userType = ref('');
const hasReview = ref(false);

// 加载签约详情
const loadContractDetail = async () => {
  loading.value = true;

  try {
    const res = await request({
      url: `/contract/${contractId.value}`,
      method: 'GET'
    });
    contract.value = res;

    // 加载护理员信息
    await loadCaregiverInfo();

    // 检查是否已评价
    if (contract.value.status === 'completed') {
      await checkReview();
    }
  } catch (err) {
    console.error('加载签约详情失败:', err);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 加载护理员信息
const loadCaregiverInfo = async () => {
  try {
    const res = await request({
      url: `/caregiver/${contract.value.caregiverId}`,
      method: 'GET'
    });
    caregiverInfo.value = res;
  } catch (err) {
    console.error('加载护理员信息失败:', err);
  }
};

// 检查是否已评价
const checkReview = async () => {
  try {
    const res = await request({
      url: `/review/contract/${contractId.value}`,
      method: 'GET'
    });
    hasReview.value = !!res;
  } catch (err) {
    console.error('检查评价失败:', err);
  }
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
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

// 获取状态描述
const getStatusDesc = (status) => {
  const descMap = {
    pending: '等待护理员接受签约请求',
    accepted: '护理员已接受，等待开始服务',
    rejected: '护理员已拒绝签约请求',
    in_service: '服务进行中',
    end_requested: '护理员请求结束服务，等待客户确认',
    completed: '服务已完成',
    cancelled: '签约已取消'
  };
  return descMap[status] || '';
};

// 获取状态样式类
const getStatusClass = (status) => {
  if (status === 'in_service') return 'status-in-service';
  if (status === 'completed') return 'status-completed';
  if (status === 'pending' || status === 'end_requested') return 'status-pending';
  if (status === 'rejected' || status === 'cancelled') return 'status-rejected';
  return 'status-default';
};

// 护理员接受签约
const handleAccept = async () => {
  uni.showModal({
    title: '确认接受',
    content: '确定要接受这个签约请求吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/contract/accept/${contractId.value}`,
            method: 'PUT'
          });
          uni.showToast({
            title: '已接受签约',
            icon: 'success'
          });
          setTimeout(() => {
            loadContractDetail();
          }, 1500);
        } catch (err) {
          console.error('接受签约失败:', err);
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 护理员拒绝签约
const handleReject = async () => {
  uni.showModal({
    title: '确认拒绝',
    content: '确定要拒绝这个签约请求吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/contract/reject/${contractId.value}`,
            method: 'PUT'
          });
          uni.showToast({
            title: '已拒绝签约',
            icon: 'success'
          });
          setTimeout(() => {
            loadContractDetail();
          }, 1500);
        } catch (err) {
          console.error('拒绝签约失败:', err);
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 护理员请求结束服务
const handleEndRequest = async () => {
  uni.showModal({
    title: '确认结束',
    content: '确定要请求结束服务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/contract/end-request/${contractId.value}`,
            method: 'PUT'
          });
          uni.showToast({
            title: '已发送结束请求',
            icon: 'success'
          });
          setTimeout(() => {
            loadContractDetail();
          }, 1500);
        } catch (err) {
          console.error('请求结束失败:', err);
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 客户确认结束服务
const handleEndAccept = async () => {
  uni.showModal({
    title: '确认结束',
    content: '确定要确认结束服务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/contract/end-accept/${contractId.value}`,
            method: 'PUT'
          });
          uni.showToast({
            title: '服务已结束',
            icon: 'success'
          });
          setTimeout(() => {
            loadContractDetail();
          }, 1500);
        } catch (err) {
          console.error('确认结束失败:', err);
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 客户拒绝结束服务
const handleEndReject = async () => {
  uni.showModal({
    title: '拒绝结束',
    content: '确定要拒绝结束服务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/contract/end-reject/${contractId.value}`,
            method: 'PUT'
          });
          uni.showToast({
            title: '已拒绝结束',
            icon: 'success'
          });
          setTimeout(() => {
            loadContractDetail();
          }, 1500);
        } catch (err) {
          console.error('拒绝结束失败:', err);
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 跳转到评价页面
const goToReview = () => {
  uni.navigateTo({
    url: `/pages/review/review?contractId=${contractId.value}&caregiverId=${contract.value.caregiverId}`
  });
};

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  contractId.value = currentPage.options.id;

  // 获取用户类型
  userType.value = uni.getStorageSync('userType') || 'client';

  if (!contractId.value) {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
    return;
  }

  loadContractDetail();
});
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.loading-box {
  text-align: center;
  padding: 200rpx 0;
  color: #999;
}

.detail-box {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

/* 状态卡片 */
.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  border-radius: 16rpx;
  text-align: center;
}

.status-text {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.status-desc {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 信息卡片 */
.info-card {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

/* 护理员信息 */
.caregiver-box {
  display: flex;
  align-items: center;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50rpx;
  margin-right: 20rpx;
}

.caregiver-info {
  flex: 1;
}

.name {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.hospital {
  display: block;
  font-size: 26rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.rating {
  display: block;
  font-size: 26rpx;
  color: #ff9900;
  font-weight: bold;
}

/* 信息项 */
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

/* 操作按钮 */
.action-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.action-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}

.accept-btn {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}

.reject-btn {
  background: #fff;
  color: #ff4d4f;
  border: 2rpx solid #ff4d4f;
}
</style>
