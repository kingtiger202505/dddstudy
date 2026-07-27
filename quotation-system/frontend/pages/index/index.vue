<template>
  <view class="container">
    <!-- 头部 -->
    <view class="header">
      <text class="title">报价单列表</text>
      <button class="create-btn" @click="goToCreate">+</button>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <!-- 报价单列表 -->
    <view v-else-if="quotationList.length > 0" class="list">
      <view 
        v-for="item in quotationList" 
        :key="item.id" 
        class="card"
        @click="goToDetail(item.id)"
      >
        <view class="card-header">
          <text class="product-name">{{ item.productName }}</text>
          <text :class="['status-tag', getStatusClass(item.status)]">
            {{ getStatusText(item.status) }}
          </text>
        </view>
        <view class="card-body">
          <view class="row">
            <text class="label">当前报价:</text>
            <text class="price">¥{{ item.currentPrice }}</text>
          </view>
          <view class="row">
            <text class="label">库存:</text>
            <text>{{ item.stockQuantity }}</text>
          </view>
          <view class="row">
            <text class="label">更新时间:</text>
            <text class="time">{{ formatTime(item.updatedAt) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty">
      <text>暂无报价单</text>
      <button class="create-btn-large" @click="goToCreate">创建第一个报价单</button>
    </view>
  </view>
</template>

<script>
import { getQuotation, getQuotationList } from '@/utils/api.js'

export default {
  data() {
    return {
      loading: false,
      quotationList: []
    }
  },
  onShow() {
    this.loadQuotations()
  },
  methods: {
    // 加载报价单列表
    async loadQuotations() {
      this.loading = true
      try {
        // 调用后端列表 API
        const list = await getQuotationList()
        this.quotationList = list || []
      } catch (err) {
        console.error('加载失败:', err)
        this.quotationList = []
      } finally {
        this.loading = false
      }
    },
    
    goToCreate() {
      uni.navigateTo({ url: '/pages/create/create' })
    },
    
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
    },
    
    getStatusClass(status) {
      const map = {
        'PENDING': 'status-pending',
        'COUNTER_OFFER': 'status-counter-offer',
        'ACCEPTED': 'status-accepted',
        'REJECTED': 'status-rejected'
      }
      return map[status] || ''
    },
    
    getStatusText(status) {
      const map = {
        'PENDING': '待处理',
        'COUNTER_OFFER': '已还价',
        'ACCEPTED': '已接受',
        'REJECTED': '已拒绝'
      }
      return map[status] || status
    },
    
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #eee;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.create-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background-color: #07c160;
  color: #fff;
  font-size: 40rpx;
  line-height: 60rpx;
  text-align: center;
  padding: 0;
  border: none;
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  color: #999;
}

.create-btn-large {
  margin-top: 40rpx;
  background-color: #07c160;
  color: #fff;
  padding: 20rpx 60rpx;
  border-radius: 8rpx;
  border: none;
}

.list {
  padding: 20rpx;
}

.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.product-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.status-tag {
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.card-body .row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
  font-size: 28rpx;
}

.label {
  color: #666;
}

.price {
  color: #fa5151;
  font-weight: bold;
  font-size: 32rpx;
}

.time {
  color: #999;
}
</style>
