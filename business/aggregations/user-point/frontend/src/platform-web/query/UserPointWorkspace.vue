<template>
  <section class="workspace-card workspace-card--paper">
    <div class="workspace-card__body">
      <div class="workspace-card__heading">
        <p>Points Detail</p>
        <h3>用户积分详情</h3>
      </div>

      <p v-if="errorMessage" class="workspace-error">{{ errorMessage }}</p>

      <div v-if="detail && balanceCardModel">
        <p class="workspace-note">当前查看：{{ detail.user.name || `用户 #${detail.user.id}` }}</p>
        <PointPlatformBalanceCard :balance="balanceCardModel" />
        <div class="log-wrap">
          <PointLogTable :logs="logPage.content" />
        </div>
        <div class="workspace-pager">
          <span>第 {{ logPage.number + 1 }} / {{ Math.max(logPage.totalPages, 1) }} 页</span>
          <div class="workspace-pager__actions">
            <button type="button" class="workspace-button-soft" :disabled="logPage.number <= 0 || loading" @click="loadLogPage(logPage.number - 1)">
              上一页
            </button>
            <button
              type="button"
              class="workspace-button-soft"
              :disabled="logPage.number + 1 >= logPage.totalPages || loading"
              @click="loadLogPage(logPage.number + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>

      <p v-else class="workspace-note">选择用户后可以查看当前余额与积分流水。</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { PointPlatformBalanceCard, PointLogTable } from '@forest/point/platform-web/point-display'
import { fetchUserPointDetail, fetchUserPointLogs } from './api'
import type { UserPointDetail, UserPointLogPage } from './types'

const LOG_PAGE_SIZE = 20

const props = defineProps<{
  userId?: number
}>()

const detail = ref<UserPointDetail | null>(null)
const logPage = ref<UserPointLogPage>({
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: LOG_PAGE_SIZE,
  number: 0
})
const loading = ref(false)
const errorMessage = ref('')

const balanceCardModel = computed(() => {
  if (!detail.value) {
    return null
  }
  return {
    userId: detail.value.user.id,
    balance: detail.value.points.balance,
    totalIncome: detail.value.points.totalIncome,
    totalSpend: detail.value.points.totalSpend
  }
})

watch(
  () => props.userId,
  async (value) => {
    if (!value) {
      detail.value = null
      logPage.value = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: LOG_PAGE_SIZE,
        number: 0
      }
      errorMessage.value = ''
      return
    }
    await loadUserPoint()
  },
  { immediate: true }
)

async function loadUserPoint() {
  if (!props.userId) {
    errorMessage.value = '请选择用户'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    detail.value = null
    logPage.value = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: LOG_PAGE_SIZE,
      number: 0
    }
    detail.value = await fetchUserPointDetail(props.userId)
    logPage.value = await fetchUserPointLogs(props.userId, 0, LOG_PAGE_SIZE)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '积分信息加载失败'
  } finally {
    loading.value = false
  }
}

async function loadLogPage(page: number) {
  if (!props.userId) {
    errorMessage.value = '请选择用户'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    logPage.value = await fetchUserPointLogs(props.userId, page, logPage.value.size)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '积分流水加载失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.log-wrap {
  margin-top: 18px;
}

@media (max-width: 720px) {
  .log-wrap {
    overflow-x: auto;
  }
}
</style>
