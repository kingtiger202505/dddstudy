/**
 * API 请求工具类
 */
const BASE_URL = '/api'

/**
 * 通用请求方法
 */
export function request(url, method = 'GET', data = null) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method: method,
      header: {
        'Content-Type': 'application/json'
      },
      data: data,
      success: (res) => {
        if (res.statusCode === 200 && res.data.success) {
          resolve(res.data.data)
        } else {
          uni.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          })
          reject(res.data)
        }
      },
      fail: (err) => {
        console.error('Request failed:', err)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

/**
 * GET 请求
 */
export function get(url, params = null) {
  return request(url, 'GET', params)
}

/**
 * POST 请求
 */
export function post(url, data = null) {
  return request(url, 'POST', data)
}

// ==================== 报价单相关 API ====================

/**
 * 创建报价单
 */
export function createQuotation(data) {
  return post('/quotations', data)
}

/**
 * 获取报价单详情
 */
export function getQuotation(id) {
  return get(`/quotations/${id}`)
}

/**
 * 获取报价单列表
 */
export function getQuotationList() {
  return get('/quotations')
}

/**
 * 买家还价
 */
export function counterOffer(id, data) {
  return post(`/quotations/${id}/counter-offer`, data)
}

/**
 * 卖家接受还价
 */
export function acceptCounterOffer(id) {
  return post(`/quotations/${id}/accept-counter`)
}

/**
 * 卖家拒绝还价
 */
export function rejectCounterOffer(id) {
  return post(`/quotations/${id}/reject-counter`)
}

/**
 * 买家接受报价
 */
export function acceptQuotation(id, quantity) {
  return post(`/quotations/${id}/accept?quantity=${quantity}`)
}

/**
 * 买家拒绝报价
 */
export function rejectQuotation(id, reason) {
  return post(`/quotations/${id}/reject`, { reason })
}
