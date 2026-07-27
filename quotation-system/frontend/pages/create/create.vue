<template>
  <view class="container">
    <view class="card">
      <view class="title">创建报价单</view>
      
      <form @submit.prevent="handleSubmit">
        <view class="form-item">
          <text class="label">商品名称</text>
          <input 
            v-model="form.productName" 
            class="input" 
            placeholder="请输入商品名称"
          />
        </view>

        <view class="form-item">
          <text class="label">原始价格 (¥)</text>
          <input 
            v-model="form.originalPrice" 
            type="digit" 
            class="input" 
            placeholder="请输入原始价格"
          />
        </view>

        <view class="form-item">
          <text class="label">最低可接受价格 (¥)</text>
          <input 
            v-model="form.minAcceptablePrice" 
            type="digit" 
            class="input" 
            placeholder="请输入最低可接受价格"
          />
        </view>

        <view class="form-item">
          <text class="label">库存数量</text>
          <input 
            v-model="form.stockQuantity" 
            type="number" 
            class="input" 
            placeholder="请输入库存数量"
          />
        </view>

        <view class="actions">
          <button class="btn-cancel" @click="goBack">取消</button>
          <button class="btn-submit" formType="submit">提交</button>
        </view>
      </form>
    </view>
  </view>
</template>

<script>
import { createQuotation } from '@/utils/api.js'

export default {
  data() {
    return {
      form: {
        productId: 1, // 模拟商品 ID，实际应从商品选择器获取
        productName: '',
        originalPrice: '',
        minAcceptablePrice: '',
        stockQuantity: ''
      }
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },

    async handleSubmit() {
      // 验证表单
      if (!this.form.productName) {
        uni.showToast({ title: '请输入商品名称', icon: 'none' })
        return
      }
      if (!this.form.originalPrice || parseFloat(this.form.originalPrice) <= 0) {
        uni.showToast({ title: '请输入有效的原始价格', icon: 'none' })
        return
      }
      if (!this.form.minAcceptablePrice || parseFloat(this.form.minAcceptablePrice) <= 0) {
        uni.showToast({ title: '请输入有效的最低价格', icon: 'none' })
        return
      }
      if (!this.form.stockQuantity || parseInt(this.form.stockQuantity) <= 0) {
        uni.showToast({ title: '请输入有效的库存数量', icon: 'none' })
        return
      }

      try {
        const quotationId = await createQuotation({
          productId: this.form.productId,
          productName: this.form.productName,
          originalPrice: parseFloat(this.form.originalPrice),
          minAcceptablePrice: parseFloat(this.form.minAcceptablePrice),
          stockQuantity: parseInt(this.form.stockQuantity)
        })

        uni.showToast({ 
          title: '创建成功', 
          icon: 'success',
          duration: 1500
        })

        // 延迟跳转到详情页
        setTimeout(() => {
          uni.redirectTo({ url: `/pages/detail/detail?id=${quotationId}` })
        }, 1500)

      } catch (err) {
        console.error('创建失败:', err)
      }
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

.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.input {
  border: 2rpx solid #eee;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 30rpx;
  background-color: #fafafa;
}

.actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.btn-cancel {
  flex: 1;
  background-color: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 30rpx;
}

.btn-submit {
  flex: 1;
  background-color: #07c160;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 30rpx;
}
</style>
