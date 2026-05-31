<script setup lang="ts">
import type { Certification } from '../../../shared/types'
import { certificationReviewStatusText } from '../../../shared/status-text'

defineProps<{
  certification: Certification | null
}>()
</script>

<template>
  <div v-if="certification" class="detail-list">
    <p><span>企业名称</span><strong>{{ certification.companyName }}</strong></p>
    <p><span>信用代码</span><strong>{{ certification.unifiedSocialCreditCode || '-' }}</strong></p>
    <p><span>营业执照</span><strong>{{ certification.businessLicenseFileNo }}</strong></p>
    <p><span>状态</span><strong>{{ certificationReviewStatusText(certification.status) }}</strong></p>
    <p><span>审核备注</span><strong>{{ certification.reviewRemark || '-' }}</strong></p>
  </div>
  <div v-else class="state">暂无认证记录。</div>
</template>

<style scoped>
.detail-list {
  display: grid;
  gap: 14px;
}

.detail-list p {
  margin: 0;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--workspace-border-soft);
}

.detail-list span {
  color: var(--workspace-text-secondary);
}

.state {
  padding: 16px;
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-soft);
  color: var(--workspace-text-secondary);
}
</style>
