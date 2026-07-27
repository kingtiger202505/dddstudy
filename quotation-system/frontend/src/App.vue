<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style>
/* 全局样式 */
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
}

.container {
  padding: 20rpx;
}

.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.btn-primary {
  background-color: #07c160;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
}

.btn-secondary {
  background-color: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
}

.btn-danger {
  background-color: #fa5151;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
}

.status-pending {
  color: #ff9f43;
  background-color: #fff5e6;
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.status-counter-offer {
  color: #2d98da;
  background-color: #e6f7ff;
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.status-accepted {
  color: #07c160;
  background-color: #e6ffed;
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.status-rejected {
  color: #fa5151;
  background-color: #ffe6e6;
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}
</style>
