<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Organization } from '../../../shared/types'
import CreateOrganizationForm from '../../components/organization/CreateOrganizationForm.vue'
import OrganizationEntryList from '../../components/organization/OrganizationEntryList.vue'
import { createOrganization } from '../../api/organization-api'

const props = withDefaults(defineProps<{
  organizations: Organization[]
  selectedOrganizationNo?: string
  loading?: boolean
  errorMessage?: string
}>(), {
  selectedOrganizationNo: '',
  loading: false,
  errorMessage: ''
})

const emit = defineEmits<{
  select: [organizationNo: string]
  created: [organization: Organization]
}>()

const createFormRef = ref<InstanceType<typeof CreateOrganizationForm> | null>(null)
const submitting = ref(false)
const createErrorMessage = ref('')
const displayErrorMessage = computed(() => props.errorMessage || createErrorMessage.value)

async function handleCreate(organizationName: string) {
  if (!organizationName) {
    createErrorMessage.value = '请填写企业名称'
    return
  }
  submitting.value = true
  createErrorMessage.value = ''
  try {
    const organization = await createOrganization({ organizationName })
    createFormRef.value?.reset()
    emit('created', organization)
  } catch (error) {
    createErrorMessage.value = error instanceof Error ? error.message : '企业创建失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="workspace-page">
    <header class="workspace-page__header">
      <p class="workspace-page__eyebrow">Organization</p>
      <h1 class="workspace-page__title">企业与租户</h1>
      <p class="workspace-page__description">一个自然人可以加入多家企业。当前企业会作为后续部门、员工、商品、订单等商家端业务的租户边界。</p>
    </header>

    <div class="workspace-grid workspace-grid--split">
      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>My Organizations</p>
            <h3>我的企业</h3>
          </div>

          <OrganizationEntryList
            :organizations="organizations"
            :selected-organization-no="selectedOrganizationNo"
            :loading="loading"
            :error-message="displayErrorMessage"
            @select="emit('select', $event)"
          />
        </div>
      </section>

      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Create</p>
            <h3>创建企业</h3>
          </div>
          <CreateOrganizationForm ref="createFormRef" :submitting="submitting" @submit="handleCreate" />
        </div>
      </section>
    </div>
  </section>
</template>
