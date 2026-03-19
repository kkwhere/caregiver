<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="top-nav">
      <text class="app-title">陪伴家园</text>
      <!-- 已登录：显示用户头像昵称 + 退出按钮 -->
      <view v-if="userInfo" class="user-section">
        <view class="user-info">
          <image 
            :src="userInfo.avatar || '/static/logo.png'" 
            class="user-avatar" 
            mode="aspectFill"
          ></image>
          <text class="user-nickname">{{ userInfo.nickname || '用户' }}</text>
        </view>
        <button class="logout-btn" size="mini" type="default" @click="handleLogout">退出</button>
      </view>
      <!-- 未登录：显示登录按钮 -->
      <button v-else class="login-btn" size="mini" type="primary" @click="goToLogin">登录</button>
    </view>

    <!-- 广告位 -->
    <view v-if="advertisements.length > 0" class="ad-section">
      <swiper class="ad-swiper" indicator-dots autoplay circular>
        <swiper-item v-for="ad in advertisements" :key="ad.id">
          <image :src="ad.imageUrl" class="ad-image" mode="aspectFill"></image>
        </swiper-item>
      </swiper>
    </view>

    <!-- 热门护理员 -->
    <view v-if="hotCaregivers.length > 0" class="hot-section">
      <text class="section-title">🔥 热门护理员</text>
      <scroll-view scroll-x class="hot-scroll">
        <view
          v-for="item in hotCaregivers"
          :key="item.id"
          class="hot-card"
          @click="handleClick(item)"
        >
          <image :src="item.avatar || '/static/logo.png'" class="hot-avatar" mode="aspectFill"></image>
          <text class="hot-name">{{ item.name }}</text>
          <text class="hot-rating">⭐ {{ item.ratingAvg || 0 }}分</text>
          <text class="hot-hospital">{{ item.hospital || '暂无医院' }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-section">
      <text class="section-title">筛选条件</text>
      <view class="filter-row">
        <picker mode="selector" :range="genderOptions" range-key="label" @change="onGenderChange">
          <view class="filter-item">
            <text>性别：{{ selectedGenderLabel }}</text>
          </view>
        </picker>
        <picker mode="selector" :range="ageOptions" range-key="label" @change="onAgeChange">
          <view class="filter-item">
            <text>年龄：{{ selectedAgeLabel }}</text>
          </view>
        </picker>
      </view>
      <view class="filter-row">
        <input
          class="filter-input"
          placeholder="输入城市"
          v-model="filterCity"
          @confirm="fetchList"
        />
        <input
          class="filter-input"
          placeholder="输入医院"
          v-model="filterHospital"
          @confirm="fetchList"
        />
      </view>
      <button class="filter-btn" size="mini" type="primary" @click="fetchList">搜索</button>
      <button class="filter-btn" size="mini" @click="resetFilter">重置</button>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-box">
      <text>正在加载数据...</text>
    </view>

    <!-- 错误提示 -->
    <view v-else-if="error" class="error-box">
      <text class="error-text">{{ error }}</text>
      <button type="warn" size="mini" @click="fetchList">重试</button>
    </view>

    <!-- 护理员列表 -->
    <view v-else-if="caregiverList.length > 0" class="list-box">
      <text class="section-title">全部护理员</text>
      <view
        v-for="(item, index) in caregiverList"
        :key="item.id"
        class="card"
        @click="handleClick(item)"
      >
        <view class="card-left">
          <image :src="item.avatar || '/static/logo.png'" class="avatar" mode="aspectFill"></image>
        </view>
        <view class="card-right">
          <view class="card-header">
            <text class="name">{{ item.name || '未知护理员' }}</text>
            <text class="rating">⭐ {{ item.ratingAvg || 0 }}分</text>
          </view>
          <text class="info">{{ item.gender === 'male' ? '男' : item.gender === 'female' ? '女' : '未知' }} | {{ calculateAge(item.birthDate) }}岁</text>
          <text class="hospital">🏥 {{ item.hospital || '暂无医院' }}</text>
          <text class="intro">{{ item.intro || '暂无介绍' }}</text>
          <view class="card-footer">
            <text :class="['status', item.status === 'available' ? 'status-available' : 'status-busy']">
              {{ item.status === 'available' ? '空闲' : '护理中' }}
            </text>
            <text class="review-count">{{ item.reviewCount || 0 }}条评价</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空数据 -->
    <view v-else class="empty-box">
      <text>暂无护理员数据</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request.js';

// 用户信息
const userInfo = ref(null);
const userType = ref('');

const caregiverList = ref([]);
const hotCaregivers = ref([]);
const advertisements = ref([]);
const loading = ref(true);
const error = ref('');

// 筛选条件
const filterGender = ref('');
const filterCity = ref('');
const filterHospital = ref('');
const filterMinAge = ref(null);
const filterMaxAge = ref(null);

const genderOptions = ref([
  { label: '全部', value: '' },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' }
]);

const ageOptions = ref([
  { label: '全部', value: '' },
  { label: '20-30岁', value: '20-30' },
  { label: '30-40岁', value: '30-40' },
  { label: '40-50岁', value: '40-50' },
  { label: '50岁以上', value: '50-100' }
]);

const selectedGenderLabel = ref('全部');
const selectedAgeLabel = ref('全部');

// 获取热门护理员
const fetchHotCaregivers = async () => {
  try {
    const res = await request({ url: '/caregiver/hot', method: 'GET' });
    hotCaregivers.value = res || [];
  } catch (err) {
    console.error('获取热门护理员失败:', err);
  }
};

// 获取广告
const fetchAdvertisements = async () => {
  try {
    const res = await request({ url: '/advertisement/position/home_top', method: 'GET' });
    advertisements.value = res || [];
  } catch (err) {
    console.error('获取广告失败:', err);
  }
};

// 获取护理员列表
const fetchList = async () => {
  loading.value = true;
  error.value = '';

  try {
    let url = '/caregiver/search?page=1&size=100';

    if (filterGender.value) {
      url += `&gender=${filterGender.value}`;
    }
    if (filterCity.value) {
      url += `&city=${filterCity.value}`;
    }
    if (filterHospital.value) {
      url += `&hospital=${filterHospital.value}`;
    }
    if (filterMinAge.value) {
      url += `&minAge=${filterMinAge.value}`;
    }
    if (filterMaxAge.value) {
      url += `&maxAge=${filterMaxAge.value}`;
    }

    const res = await request({ url, method: 'GET' });

    if (res && res.records) {
      caregiverList.value = res.records;
    } else if (Array.isArray(res)) {
      caregiverList.value = res;
    } else {
      caregiverList.value = [];
    }
  } catch (err) {
    error.value = '加载失败，请检查后端是否启动';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// 性别选择
const onGenderChange = (e) => {
  const index = e.detail.value;
  filterGender.value = genderOptions.value[index].value;
  selectedGenderLabel.value = genderOptions.value[index].label;
};

// 年龄选择
const onAgeChange = (e) => {
  const index = e.detail.value;
  const value = ageOptions.value[index].value;
  selectedAgeLabel.value = ageOptions.value[index].label;

  if (value) {
    const [min, max] = value.split('-');
    filterMinAge.value = parseInt(min);
    filterMaxAge.value = parseInt(max);
  } else {
    filterMinAge.value = null;
    filterMaxAge.value = null;
  }
};

// 重置筛选
const resetFilter = () => {
  filterGender.value = '';
  filterCity.value = '';
  filterHospital.value = '';
  filterMinAge.value = null;
  filterMaxAge.value = null;
  selectedGenderLabel.value = '全部';
  selectedAgeLabel.value = '全部';
  fetchList();
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

const handleClick = (item) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${item.id}`
  });
};

const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/login/login'
  });
};

onMounted(() => {
  // 从本地存储获取用户信息和用户类型
  const storedUserInfo = uni.getStorageSync('userInfo');
  const storedUserType = uni.getStorageSync('userType');
  
  if (storedUserInfo) {
	userInfo.value = storedUserInfo;
  }
  if (storedUserType) {
	userType.value = storedUserType;
  }
	
	
	
  fetchHotCaregivers();
  fetchAdvertisements();
  fetchList();
});

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除本地存储的用户信息
        uni.removeStorageSync('userInfo');
        uni.removeStorageSync('userType');
        
        // 清除 token（如果有的话，根据你的实际情况）
        // 比如：uni.removeStorageSync('token'); 或调用你封装的清除方法
        
        // 将 userInfo 设为 null，触发视图更新
        userInfo.value = null;
        
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        });
      }
    }
  });
};
</script>

<style scoped>
/* 修改后 - 推荐写法 */
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100%; 
  box-sizing: border-box; 
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}
.app-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}
.login-btn {
  background: #fff;
  color: #667eea;
  border: none;
  border-radius: 20rpx;
  padding: 10rpx 30rpx;
  font-size: 26rpx;
}

/* 广告位 */
.ad-section {
  margin-bottom: 30rpx;
}
.ad-swiper {
  height: 300rpx;
  border-radius: 16rpx;
  overflow: hidden;
}
.ad-image {
  width: 100%;
  height: 100%;
}

/* 热门护理员 */
.hot-section {
  margin-bottom: 30rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 20rpx;
  color: #333;
}
.hot-scroll {
  white-space: nowrap;
}
.hot-card {
  display: inline-block;
  width: 200rpx;
  background: #fff;
  padding: 20rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}
.hot-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-bottom: 10rpx;
}
.hot-name {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 5rpx;
}
.hot-rating {
  display: block;
  color: #ff9900;
  font-size: 24rpx;
  margin-bottom: 5rpx;
}
.hot-hospital {
  display: block;
  font-size: 22rpx;
  color: #999;
}

/* 筛选栏 */
.filter-section {
  background: #fff;
  padding: 20rpx;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}
.filter-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.filter-item {
  flex: 1;
  padding: 15rpx 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  margin-right: 20rpx;
  font-size: 26rpx;
}
.filter-item:last-child {
  margin-right: 0;
}
.filter-input {
  flex: 1;
  padding: 15rpx 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  margin-right: 20rpx;
  font-size: 26rpx;
}
.filter-input:last-child {
  margin-right: 0;
}
.filter-btn {
  margin-right: 20rpx;
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

/* 护理员列表 */
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
  display: flex;
}
.card-left {
  margin-right: 20rpx;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
}
.card-right {
  flex: 1;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}
.name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.rating {
  color: #ff9900;
  font-size: 26rpx;
  font-weight: bold;
}
.info {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-bottom: 8rpx;
}
.hospital {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 8rpx;
}
.intro {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status {
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;
}
.status-available {
  background: #52c41a;
  color: #fff;
}
.status-busy {
  background: #ff4d4f;
  color: #fff;
}
.review-count {
  font-size: 22rpx;
  color: #999;
}

/* 用户信息（已登录状态） */
.user-info {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
}
.user-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 25rpx;
  margin-right: 10rpx;
  border: 2rpx solid #fff;
}
.user-nickname {
  font-size: 26rpx;
  color: #fff;
  max-width: 150rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 已登录时的用户信息 + 退出按钮 区域 */
.user-section {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  gap: 20rpx; /* 用户信息与退出按钮之间的间距 */
}

/* 退出登录按钮 */
.logout-btn {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  border: 1rpx solid rgba(255, 255, 255, 0.5);
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
}
</style>