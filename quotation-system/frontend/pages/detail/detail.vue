<template>
  <view class="container">
    <!-- 加载中 -->
    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <!-- 报价单详情 -->
    <view v-else-if="quotation" class="detail">
      <!-- 商品信息 -->
      <view class="card">
        <view class="card-title">商品信息</view>
        <view class="info-row">
          <text class="label">商品名称:</text>
          <text class="value">{{ quotation.productName }}</text>
        </view>
        <view class="info-row">
          <text class="label">原始价格:</text>
          <text class="value">¥{{ quotation.originalPrice }}</text>
        </view>
        <view class="info-row">
          <text class="label">当前报价:</text>
          <text class="value price">¥{{ quotation.currentPrice }}</text>
        </view>
        <view class="info-row">
          <text class="label">最低可接受价:</text>
          <text class="value">¥{{ quotation.minAcceptablePrice }}</text>
        </view>
        <view class="info-row">
          <text class="label">库存数量:</text>
          <text class="value">{{ quotation.stockQuantity }}</text>
        </view>
        <view class="info-row">
          <text class="label">状态:</text>
          <text :class="['status-tag', getStatusClass(quotation.status)]">
            {{ getStatusText(quotation.status) }}
          </text>
        </view>
      </view>

      <!-- 操作区域 -->
      <view class="actions">
        <!-- 待处理状态：买家可以还价或接受 -->
        <view v-if="quotation.status === 'PENDING'">
          <button class="btn-primary" @click="showCounterOffer = true">我要还价</button>
          <button class="btn-success" @click="handleAccept">接受报价</button>
        </view>

        <!-- 已还价状态：卖家可以接受/拒绝，买家可以再次还价 -->
        <view v-if="quotation.status === 'COUNTER_OFFER'">
          <button class="btn-primary" @click="showCounterOffer = true">再次还价</button>
          <button class="btn-success" @click="handleAcceptSeller">接受还价（卖家）</button>
          <button class="btn-danger" @click="handleRejectSeller">拒绝还价（卖家）</button>
        </view>
        <!-- 待处理状态：买家可以拒绝 -->
        <view v-if="quotation.status === 'PENDING'">
          <button class="btn-warning" @click="handleRejectBuyer">拒绝报价</button>
        </view>


        <!-- 已接受/已拒绝：显示结果 -->
        <view v-if="quotation.status === 'ACCEPTED'" class="result accepted">
          <text>✓ 交易已达成</text>
        </view>
        <view v-if="quotation.status === 'REJECTED'" class="result rejected">
          <text>✗ 报价已拒绝</text>
        </view>
      </view>

      <!-- 还价弹窗 -->
      <view v-if="showCounterOffer" class="modal-overlay" @click="showCounterOffer = false">
        <view class="modal" @click.stop>
          <view class="modal-title">输入还价金额</view>
          <input 
            v-model="offerPrice" 
            type="number" 
            class="input" 
            placeholder="请输入还价金额"
          />
          <view class="modal-actions">
            <button class="btn-cancel" @click="showCounterOffer = false">取消</button>
            <button class="btn-confirm" @click="submitCounterOffer">确定</button>
          </view>
        </view>
      </view>

      <!-- 购买数量弹窗 -->
      <view v-if="showBuyDialog" class="modal-overlay" @click="showBuyDialog = false">
        <view class="modal" @click.stop>
          <view class="modal-title">输入购买数量</view>
          <input 
            v-model="buyQuantity" 
            type="number" 
            class="input" 
            placeholder="请输入购买数量"
          />
          <view class="modal-actions">
            <button class="btn-cancel" @click="showBuyDialog = false">取消</button>
            <button class="btn-confirm" @click="confirmBuy">确定</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { 
  getQuotation, 
  counterOffer, 
  acceptQuotation,
  acceptCounterOffer,
  rejectCounterOffer,
  rejectQuotation
} from '@/utils/api.js'

export default {
  data() {
    return {
      loading: false,
      quotation: null,
      showCounterOffer: false,
      showBuyDialog: false,
      showRejectDialog: false,
      offerPrice: '',
      buyQuantity: '1',
      rejectReason: ''
    }
  },
  onLoad(options) {
    if (options.id) {
      this.loadDetail(options.id)
    }
  },
  methods: {
    async loadDetail(id) {
      this.loading = true
      try {
        const data = await getQuotation(id)
        this.quotation = data
      } catch (err) {
        console.error('加载失败:', err)
      } finally {
        this.loading = false
      }
    },

    async submitCounterOffer() {
      if (!this.offerPrice || parseFloat(this.offerPrice) <= 0) {
        uni.showToast({ title: '请输入有效金额', icon: 'none' })
        return
      }

      try {
        await counterOffer(this.quotation.id, { newPrice: parseFloat(this.offerPrice) })
        uni.showToast({ title: '还价成功', icon: 'success' })
        this.showCounterOffer = false
        this.offerPrice = ''
        this.loadDetail(this.quotation.id)
      } catch (err) {
        console.error('还价失败:', err)
      }
    },

    handleAccept() {
      this.showBuyDialog = true
    },

    async confirmBuy() {
      if (!this.buyQuantity || parseInt(this.buyQuantity) <= 0) {
        uni.showToast({ title: '请输入有效数量', icon: 'none' })
        return
      }

      try {
        await acceptQuotation(this.quotation.id, parseInt(this.buyQuantity))
        uni.showToast({ title: '购买成功', icon: 'success' })
        this.showBuyDialog = false
        this.buyQuantity = '1'
        this.loadDetail(this.quotation.id)
      } catch (err) {
        console.error('购买失败:', err)
      }
    },

    async handleAcceptSeller() {
      try {
        await acceptCounterOffer(this.quotation.id)
        uni.showToast({ title: '已接受还价', icon: 'success' })
        this.loadDetail(this.quotation.id)
      } catch (err) {
        console.error('操作失败:', err)
      }
    },

    async handleRejectSeller() {
      try {
        await rejectCounterOffer(this.quotation.id)
        uni.showToast({ title: '已拒绝还价', icon: 'success' })
        this.loadDetail(this.quotation.id)
      } catch (err) {
        console.error('操作失败:', err)
      }
    },

    async handleRejectBuyer() {
      this.showRejectDialog = true
    },

    async confirmReject() {
      try {
        await rejectQuotation(this.quotation.id, this.rejectReason || '无理由拒绝')
        uni.showToast({ title: '已拒绝报价', icon: 'success' })
        this.showRejectDialog = false
        this.rejectReason = ''
        this.loadDetail(this.quotation.id)
      } catch (err) {
        console.error('操作失败:', err)
      }
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
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  color: #999;
}

.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  font-size: 28rpx;
}

.label {
  color: #666;
}

.value {
  color: #333;
}

.price {
  color: #fa5151;
  font-weight: bold;
  font-size: 32rpx;
}

.status-tag {
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.btn-primary {
  background-color: #07c160;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 30rpx;
}

.btn-success {
  background-color: #1890ff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 30rpx;
}

.btn-danger {
  background-color: #fa5151;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 30rpx;
}

.result {
  text-align: center;
  padding: 40rpx;
  border-radius: 8rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.result.accepted {
  background-color: #e6ffed;
  color: #07c160;
}

.result.rejected {
  background-color: #ffe6e6;
  color: #fa5151;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  width: 600rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 30rpx;
}

.input {
  border: 2rpx solid #eee;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 30rpx;
  margin-bottom: 30rpx;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
}

.btn-cancel {
  flex: 1;
  background-color: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.btn-confirm {
  flex: 1;
  background-color: #07c160;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
}
</style>
