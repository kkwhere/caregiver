<template>
  <view class="container">
    <!-- 状态筛选 -->
    <scroll-view scroll-x class="filter-bar">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="['filter-tab', activeStatus === tab.value ? 'active' : '']"
        @click="switchStatus(tab.value)"
      >
        {{ tab.label }}
      </view>
    </scroll-view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-box">
      <text>正在加载...</text>
    </view>

    <!-- 空状态 -->
    <view v-else-if="filteredContracts.length === 0" class="empty-box">
      <text>暂无签约记录</text>
    </view>

    <!-- 签约列表 -->
    <view v-else class="list-box">
      <view
        v-for="contract in filteredContracts"
        :key="contract.id"
        class="contract-card"
        @click="goToDetail(contract.id)"
      >
        <view class="card-header">
          <text class="contract-id">签约 #{{ contract.id }}</text>
          <text :class="['status-tag', getStatusClass(contract.status)]">
            {{ getStatusText(contract.status) }}
          </text>
        </view>
        <view class="card-body">
          <text class="info-line">
            {{ userType === 'client' ? '护理员ID' : '客户ID' }}：
            {{ userType === 'client' ? contract.caregiverId : contract.clientId }}
          </text>
          <text class="info-line">创建时间：{{ formatDate(contract.createTime) }}</text>
          <text v-if="contract.startTime" class="info-line">开始时间：{{ formatDate(contract.startTime) }}</text>
          <text v-if="contract.endTime" class="info-line">结束时间：{{ formatDate(contract.endTime) }}</text>
        </view>
        <view class="card-footer">
          <text class="arrow">查看详情 ›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { request } from '@/utils/request.js';

const userType = ref('client');
const userId = ref(null);
const contracts = ref([]);
const loading = ref(true);
const activeStatus = ref('all');

// 状态筛选标签
const statusTabs = [
  { label: '全部', value: 'all' },
  { label: '待接受', value: 'pending' },
  { label: '服务中', value: 'in_service' },
  { label: '待确认结束', value: 'end_requested' },
  { label: '已完成', value: 'completed' },
  { label: '已拒绝', value: 'rejected' }
];

// 过滤后的签约列表
const filteredContracts = computed(() => {
  if (activeStatus.value === 'all') {
    return contracts.value;
  }
  return contracts.value.filter(c => c.status === activeStatus.value);
});

// 加载签约列表
const loadContracts = async () => {
  loading.value = true;
  try {
    const endpoint = userType.value === 'client'
      ? `/contract/client/${userId.value}`
      : `/contract/caregiver/${userId.value}`;

    const res = await request({
      url: endpoint,
      method: 'GET'
    });
    contracts.value = res || [];
  } catch (err) {
    console.error('加载签约列表失败:', err);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 切换状态筛选
const switchStatus = (status) => {
  activeStatus.value = status;
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

// 获取状态样式类
const getStatusClass = (status) => {
  if (status === 'in_service') return 'status-in-service';
  if (status === 'completed') return 'status-completed';
  if (status === 'pending' || status === 'end_requested') return 'status-pending';
  if (status === 'rejected' || status === 'cancelled') return 'status-rejected';
  return 'status-default';
};

// 跳转到详情页
const goToDetail = (contractId) => {
  uni.navigateTo({
    url: `/pages/contract-detail/contract-detail?id=${contractId}`
  });
};

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  userType.value = currentPage.options.type || 'client';

  // 获取用户信息
  const userInfo = uni.getStorageSync('userInfo');
  if (userInfo && userInfo.id) {
    userId.value = userInfo.id;
    loadContracts();
  } else {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    setTimeout(() => {
      uni.redirectTo({
        url: '/pages/login/login'
      });
    }, 1500);
  }
});
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 筛选栏 */
.filter-bar {
  background: #fff;
  padding: 20rpx;
  white-space: nowrap;
  border-bottom: 1rpx solid #f0f0f0;
}

.filter-tab {
  display: inline-block;
  padding: 12rpx 24rpx;
  margin-right: 20rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  color: #666;
  background: #f5f5f5;
}

.filter-tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: bold;
}

/* 加载和空状态 */
.loading-box,
.empty-box {
  text-align: center;
  padding: 200rpx 0;
  color: #999;
}

/* 列表 */
.list-box {
  padding: 20rpx;
}

.contract-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.contract-id {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.status-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;
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

.status-rejected {
  background: #ff4d4f;
  color: #fff;
}

.status-default {
  background: #d9d9d9;
  color: #666;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.info-line {
  font-size: 28rpx;
  color: #666;
}

.card-footer {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  text-align: right;
}

.arrow {
  font-size: 28rpx;
  color: #667eea;
  font-weight: bold;
}
</style>
