<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import type { Organization } from '../../../shared/types'
import { fetchOrganization, updateOrganization } from '../../api/organization-api'
import OrganizationProfileCard from '../../components/organization/OrganizationProfileCard.vue'
import OrganizationProfileEditForm from '../../components/organization/OrganizationProfileEditForm.vue'

const props = withDefaults(defineProps<{
  organizationNo?: string
  canUpdate?: boolean
}>(), {
  organizationNo: '',
  canUpdate: false
})

const emit = defineEmits<{
  updated: [organization: Organization]
}>()

const organization = ref<Organization | null>(null)
const organizationName = ref('')
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

onMounted(loadOrganization)
watch(() => props.organizationNo, loadOrganization)

async function loadOrganization() {
  organization.value = null
  organizationName.value = ''
  if (!props.organizationNo) {
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchOrganization(props.organizationNo)
    organization.value = result
    organizationName.value = result.organizationName
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '企业资料加载失败'
  } finally {
    loading.value = false
  }
}

async function handleUpdate() {
  if (!props.organizationNo || !organizationName.value) {
    errorMessage.value = '请填写企业名称'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    const result = await updateOrganization(props.organizationNo, {
      organizationName: organizationName.value
    })
    organization.value = result
    organizationName.value = result.organizationName
    emit('updated', result)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '企业资料保存失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="workspace-page">
    <header class="workspace-page__header">
      <p class="workspace-page__eyebrow">Organization</p>
      <h1 class="workspace-page__title">企业资料</h1>
      <p class="workspace-page__description">当前企业工作台的基础资料。创建企业属于入口页能力，不在这里处理。</p>
    </header>

    <div v-if="!organizationNo" class="state">请先在企业入口页选择企业。</div>
    <div v-else-if="loading" class="state">加载中</div>
    <div v-else class="workspace-grid workspace-grid--split">
      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Profile</p>
            <h3>基础信息</h3>
          </div>

          <div v-if="errorMessage" class="state state--danger">{{ errorMessage }}</div>
          <OrganizationProfileCard v-if="organization" :organization="organization" />
        </div>
      </section>

      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Edit</p>
            <h3>编辑资料</h3>
          </div>

          <OrganizationProfileEditForm
            v-if="canUpdate"
            v-model="organizationName"
            :submitting="submitting"
            @save="handleUpdate"
          />
          <div v-else class="state">当前账号只能查看企业资料。</div>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
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
</style>
