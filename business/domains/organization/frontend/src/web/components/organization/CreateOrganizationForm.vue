<script setup lang="ts">
import { ref } from 'vue'

withDefaults(defineProps<{
  submitting?: boolean
}>(), {
  submitting: false
})

const emit = defineEmits<{
  submit: [organizationName: string]
}>()

const organizationName = ref('')

function handleSubmit() {
  emit('submit', organizationName.value)
}

function reset() {
  organizationName.value = ''
}

defineExpose({
  reset
})
</script>

<template>
  <form class="stack" @submit.prevent="handleSubmit">
    <label>
      企业名称
      <input v-model.trim="organizationName" placeholder="请输入企业名称">
    </label>
    <button type="submit" class="workspace-button" :disabled="submitting">
      {{ submitting ? '创建中' : '创建企业' }}
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
