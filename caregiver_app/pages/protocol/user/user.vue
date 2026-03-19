<template>
  <view class="protocol">
    <view class="header">
      <text class="title">隐私政策</text>
    </view>
    <view class="content">
      <text class="section-title">「陪伴家园」隐私保护指引</text>

      <text class="section">1. 我们收集的信息</text>
      <text class="text">当您使用「陪伴家园」小程序时，我们可能收集以下信息：</text>
      <text class="sub-text">• 您主动提供的信息：如昵称、头像（通过微信授权登录获取）</text>
      <text class="sub-text">• 您选择的服务身份：如“客户”或“护理员”</text>
      <text class="sub-text">• 您在使用服务过程中产生的交互信息（如服务需求、订单信息等，如有）</text>

      <text class="section">2. 信息使用目的</text>
      <text class="text">我们收集上述信息，旨在为您提供个性化的护理服务匹配、账户管理、以及改进平台服务与用户体验。</text>

      <text class="section">3. 信息的共享与披露</text>
      <text class="text">我们不会将您的个人信息出售、出租或共享给第三方，除非获得您的明确同意或法律法规要求。</text>

      <text class="section">4. 信息的存储与安全</text>
      <text class="text">我们采用合理的技术与管理措施保护您的个人信息，防止丢失、泄露或被篡改。</text>

      <text class="section">5. 您的权利</text>
      <text class="text">您有权查询、更正、删除您的个人信息，也有权撤回您对信息收集与处理的授权。如需行使相关权利，请通过平台客服联系我们。</text>

      <text class="section">6. 未成年人保护</text>
      <text class="text">若您为未成年人，请在监护人指导下使用本平台，我们建议未成年人在获得监护人同意后使用服务。</text>

      <text class="section">7. 政策更新</text>
      <text class="text">我们可能适时更新本隐私政策，更新后将通过小程序通知您，继续使用即视为同意。</text>

      <text class="section">8. 联系我们</text>
      <text class="text">如您对本隐私政策有任何疑问，请通过平台客服渠道与我们联系。</text>
    </view>
  </view>
</template>

<script setup>
// 隐私政策页面逻辑（可为空）
</script>

<style scoped>
.protocol {
  padding: 40rpx;
  line-height: 1.6;
  font-size: 28rpx;
  color: #333;
}
.header {
  text-align: center;
  margin-bottom: 60rpx;
}
.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #000;
}
.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #000;
  margin: 40rpx 0 20rpx 0;
}
.section {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin: 30rpx 0 15rpx 0;
}
.sub-text {
  display: block;
  margin-left: 20rpx;
  margin-bottom: 15rpx;
  color: #555;
  font-size: 28rpx;
}
.text {
  display: block;
  margin-bottom: 20rpx;
  color: #555;
}
</style>