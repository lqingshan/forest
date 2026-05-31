<template>
  <div class="workspace-grid workspace-grid--split">
    <section class="workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <div class="workspace-card__heading">
          <p>Lead Archive</p>
          <h3>线索列表</h3>
        </div>

        <LeadFilterBar :filters="filters" @update="filters = $event" @submit="loadLeads(0)" />

        <p v-if="errorMessage" class="workspace-error">{{ errorMessage }}</p>

        <LeadTable
          :page="leadPage"
          :selected-lead-id="selectedLead?.id ?? null"
          @select="selectLead"
        />

        <div class="workspace-pager">
          <span>第 {{ leadPage.number + 1 }} / {{ Math.max(leadPage.totalPages, 1) }} 页</span>
          <div class="workspace-pager__actions">
            <button type="button" class="workspace-button-soft" :disabled="leadPage.number <= 0 || loading" @click="loadLeads(leadPage.number - 1)">
              上一页
            </button>
            <button
              type="button"
              class="workspace-button-soft"
              :disabled="leadPage.number + 1 >= leadPage.totalPages || loading"
              @click="loadLeads(leadPage.number + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <div class="workspace-card__heading">
          <p>Lead Editor</p>
          <h3>{{ editingId ? '编辑线索' : '新增线索' }}</h3>
        </div>

        <p v-if="detailLoading" class="workspace-note">正在加载线索详情，请稍候。</p>

        <LeadForm
          ref="leadFormComponent"
          :draft="draft"
          :disabled="loading || detailLoading"
          :show-delete="Boolean(editingId)"
          :submit-label="editingId ? '保存修改' : '新增线索'"
          @update="mergeDraft"
          @submit="saveLead"
          @reset="resetForm"
          @delete="removeLead"
        />

        <LeadManagementCard v-if="selectedLead" :lead="selectedLead" />
        <p v-else class="workspace-note">选中左侧线索后，这里会展示当前记录的详细预览。</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import {
  createLeadPlatformItem,
  deleteLeadPlatformItem,
  fetchLeadPlatformItem,
  fetchLeadPlatformItems,
  updateLeadPlatformItem
} from './api'
import type { LeadPlatformItem, LeadPlatformDraft, LeadPlatformPage, LeadPlatformPageQuery } from './types'
import LeadFilterBar from './LeadFilterBar.vue'
import LeadForm from './LeadForm.vue'
import LeadManagementCard from './LeadManagementCard.vue'
import LeadTable from './LeadTable.vue'

const filters = ref<LeadPlatformPageQuery>({
  keyword: undefined,
  country: undefined
})

const leadPage = ref<LeadPlatformPage>({
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 20,
  number: 0
})

const selectedLead = ref<LeadPlatformItem | null>(null)
const editingId = ref<number | null>(null)
const loading = ref(false)
const detailLoading = ref(false)
const errorMessage = ref('')
const leadFormComponent = ref<InstanceType<typeof LeadForm> | null>(null)
const draft = reactive<LeadPlatformDraft>({
  name: '',
  sourceType: '',
  keywords: '',
  category: '',
  country: '',
  phone: '',
  email: '',
  website: '',
  intro: ''
})

onMounted(async () => {
  await loadLeads(0)
})

async function loadLeads(page: number) {
  loading.value = true
  errorMessage.value = ''
  try {
    leadPage.value = await fetchLeadPlatformItems({
      page,
      size: leadPage.value.size,
      keyword: filters.value.keyword,
      country: filters.value.country
    })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '线索列表加载失败'
  } finally {
    loading.value = false
  }
}

async function selectLead(leadId: number) {
  detailLoading.value = true
  try {
    const lead = await fetchLeadPlatformItem(leadId)
    selectedLead.value = lead
    editingId.value = lead.id
    syncDraftFromLead(lead)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '线索详情加载失败'
  } finally {
    detailLoading.value = false
  }
}

function mergeDraft(partialDraft: LeadPlatformDraft) {
  Object.assign(draft, partialDraft)
}

async function saveLead() {
  await nextTick()

  if (!draft.name?.trim()) {
    errorMessage.value = '线索名称不能为空'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const payload = buildDraftPayload()
    const lead = editingId.value
      ? await updateLeadPlatformItem(editingId.value, payload)
      : await createLeadPlatformItem(payload)
    selectedLead.value = lead
    editingId.value = lead.id
    syncDraftFromLead(lead)
    await loadLeads(editingId.value ? leadPage.value.number : 0)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '线索保存失败'
  } finally {
    loading.value = false
  }
}

async function removeLead() {
  if (!editingId.value) {
    return
  }
  const confirmed = window.confirm('确认删除当前线索吗？')
  if (!confirmed) {
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    await deleteLeadPlatformItem(editingId.value)
    resetForm()
    await loadLeads(Math.max(leadPage.value.number - Number(leadPage.value.content.length === 1 && leadPage.value.number > 0), 0))
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '线索删除失败'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  selectedLead.value = null
  Object.assign(draft, {
    name: '',
    sourceType: '',
    keywords: '',
    category: '',
    country: '',
    phone: '',
    email: '',
    website: '',
    intro: ''
  })
}

function syncDraftFromLead(lead: LeadPlatformItem) {
  Object.assign(draft, {
    name: lead.name,
    sourceType: lead.sourceType ?? '',
    keywords: lead.keywords ?? '',
    category: lead.category ?? '',
    country: lead.country ?? '',
    phone: lead.phone ?? '',
    email: lead.email ?? '',
    website: lead.website ?? '',
    intro: lead.intro ?? ''
  })
}

function buildDraftPayload(): LeadPlatformDraft {
  const normalize = (field: keyof LeadPlatformDraft) => {
    const element = leadFormComponent.value?.leadFormRef?.elements.namedItem(String(field))
    if (element instanceof HTMLInputElement || element instanceof HTMLTextAreaElement) {
      const value = element.value.trim()
      return value || null
    }

    const rawValue = draft[field]
    if (typeof rawValue === 'string') {
      const value = rawValue.trim()
      return value || null
    }
    return rawValue ?? null
  }

  const name = normalize('name')
  return {
    name: typeof name === 'string' ? name : '',
    sourceType: normalize('sourceType'),
    keywords: normalize('keywords'),
    category: normalize('category'),
    country: normalize('country'),
    phone: normalize('phone'),
    email: normalize('email'),
    website: normalize('website'),
    intro: normalize('intro')
  }
}
</script>
