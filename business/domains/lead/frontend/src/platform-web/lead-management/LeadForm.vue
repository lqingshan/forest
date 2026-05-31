<template>
  <form ref="leadFormRef" class="lead-form" @submit.prevent="emit('submit')">
    <input :value="draft.name" :disabled="disabled" name="name" type="text" placeholder="名称" required @input="updateField('name', $event)">
    <input :value="draft.sourceType ?? ''" :disabled="disabled" name="sourceType" type="text" placeholder="来源类型" @input="updateField('sourceType', $event)">
    <input :value="draft.keywords ?? ''" :disabled="disabled" name="keywords" type="text" placeholder="关键词" @input="updateField('keywords', $event)">
    <input :value="draft.category ?? ''" :disabled="disabled" name="category" type="text" placeholder="分类" @input="updateField('category', $event)">
    <input :value="draft.country ?? ''" :disabled="disabled" name="country" type="text" placeholder="国家" @input="updateField('country', $event)">
    <input :value="draft.phone ?? ''" :disabled="disabled" name="phone" type="text" placeholder="电话" @input="updateField('phone', $event)">
    <input :value="draft.email ?? ''" :disabled="disabled" name="email" type="email" placeholder="邮箱" @input="updateField('email', $event)">
    <input :value="draft.website ?? ''" :disabled="disabled" name="website" type="url" placeholder="网站" @input="updateField('website', $event)">
    <textarea :value="draft.intro ?? ''" :disabled="disabled" name="intro" rows="5" placeholder="简介" @input="updateField('intro', $event)"></textarea>

    <div class="workspace-actions">
      <button type="submit" class="workspace-button" :disabled="disabled">
        {{ submitLabel }}
      </button>
      <button type="button" class="workspace-button-soft" :disabled="disabled" @click="emit('reset')">
        重置表单
      </button>
      <button
        v-if="showDelete"
        type="button"
        class="workspace-button-danger"
        :disabled="disabled"
        @click="emit('delete')"
      >
        删除线索
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { LeadPlatformDraft } from './types'

const props = defineProps<{
  draft: LeadPlatformDraft
  disabled?: boolean
  showDelete?: boolean
  submitLabel: string
}>()

const emit = defineEmits<{
  update: [draft: LeadPlatformDraft]
  submit: []
  reset: []
  delete: []
}>()

const leadFormRef = ref<HTMLFormElement | null>(null)

defineExpose({
  leadFormRef
})

function updateField(field: keyof LeadPlatformDraft, event: Event) {
  const target = event.target as HTMLInputElement | HTMLTextAreaElement
  const value = target.value
  emit('update', {
    ...props.draft,
    [field]: value
  } as LeadPlatformDraft)
}
</script>

<style scoped>
.lead-form {
  display: grid;
  gap: 12px;
}
</style>
