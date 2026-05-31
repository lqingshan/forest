<script setup lang="ts">
withDefaults(defineProps<{
  modelValue: string
  submitting?: boolean
}>(), {
  submitting: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  save: []
}>()

function handleInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value.trim())
}
</script>

<template>
  <form class="stack" @submit.prevent="emit('save')">
    <label>
      企业名称
      <input :value="modelValue" placeholder="请输入企业名称" @input="handleInput">
    </label>
    <button type="submit" class="workspace-button" :disabled="submitting">
      {{ submitting ? '保存中' : '保存企业资料' }}
    </button>
  </form>
</template>

<style scoped>
.stack {
  display: grid;
  gap: 14px;
}

.stack label {
  display: grid;
  gap: 8px;
  color: var(--workspace-text-secondary);
}
</style>
