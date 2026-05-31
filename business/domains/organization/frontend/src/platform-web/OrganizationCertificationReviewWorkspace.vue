<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { Certification } from '../shared/types'
import {
  approveOrganizationCertification,
  listPendingOrganizationCertifications,
  rejectOrganizationCertification
} from './api'

const emit = defineEmits<{
  changed: []
}>()

const certifications = ref<Certification[]>([])
const remarks = reactive<Record<number, string>>({})
const loading = ref(false)
const errorMessage = ref('')

onMounted(load)

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    certifications.value = await listPendingOrganizationCertifications()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '认证列表加载失败'
  } finally {
    loading.value = false
  }
}

async function handleApprove(certificationId: number) {
  errorMessage.value = ''
  try {
    await approveOrganizationCertification(certificationId, { reviewRemark: remarks[certificationId] || null })
    await load()
    emit('changed')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '认证通过失败'
  }
}

async function handleReject(certificationId: number) {
  errorMessage.value = ''
  try {
    await rejectOrganizationCertification(certificationId, { reviewRemark: remarks[certificationId] || null })
    await load()
    emit('changed')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '认证驳回失败'
  }
}
</script>

<template>
  <section class="workspace-page">
    <header class="workspace-page__header">
      <p class="workspace-page__eyebrow">Review</p>
      <h1 class="workspace-page__title">认证审核</h1>
      <p class="workspace-page__description">平台审核企业提交的基础认证材料，通过或驳回后会更新企业当前认证状态。</p>
    </header>

    <section class="workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <div class="workspace-card__heading">
          <p>Pending</p>
          <h3>待审核列表</h3>
        </div>

        <div v-if="errorMessage" class="state state--danger">{{ errorMessage }}</div>
        <div v-if="loading" class="state">加载中</div>
        <div v-else-if="!certifications.length" class="state">暂无待审核认证。</div>
        <div v-else class="review-list">
          <article v-for="certification in certifications" :key="certification.id" class="review-list__item">
            <div class="review-list__main">
              <h3>{{ certification.companyName }}</h3>
              <p>信用代码：{{ certification.unifiedSocialCreditCode || '-' }}</p>
              <p>法人：{{ certification.legalRepresentativeName }}</p>
              <p>联系人：{{ certification.contactName }} · {{ certification.contactPhone }}</p>
              <p>营业执照 fileNo：{{ certification.businessLicenseFileNo }}</p>
            </div>
            <div class="review-list__actions">
              <input v-model.trim="remarks[certification.id]" placeholder="审核备注">
              <button type="button" class="workspace-button-success" @click="handleApprove(certification.id)">
                通过
              </button>
              <button type="button" class="workspace-button-danger" @click="handleReject(certification.id)">
                驳回
              </button>
            </div>
          </article>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.review-list {
  display: grid;
  gap: 14px;
}

.review-list__item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
  padding: 16px;
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-lg);
  background: var(--workspace-surface-strong);
}

.review-list__main {
  display: grid;
  gap: 8px;
}

.review-list__main h3,
.review-list__main p {
  margin: 0;
}

.review-list__main p {
  color: var(--workspace-text-secondary);
}

.review-list__actions {
  display: grid;
  gap: 10px;
  align-content: start;
}

.state {
  padding: 16px;
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-soft);
  color: var(--workspace-text-secondary);
}

.state--danger {
  color: var(--workspace-danger);
  background: var(--workspace-danger-soft);
}

@media (max-width: 920px) {
  .review-list__item {
    grid-template-columns: 1fr;
  }
}
</style>
