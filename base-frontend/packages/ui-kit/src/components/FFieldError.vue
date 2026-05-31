<template>
  <p v-if="normalizedMessage" class="f-field-error" role="alert">
    {{ normalizedMessage }}
  </p>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  message?: string | null
}>()

const normalizedMessage = computed(() => props.message?.trim() ?? '')
</script>

<style scoped>
.f-field-error {
  display: block;
  margin: var(--forest-field-error-margin, 0);
  color: var(--forest-field-error-color, var(--workspace-danger, #c2410c));
  font-size: var(--forest-field-error-font-size, 13px);
  line-height: var(--forest-field-error-line-height, 1.5);
  word-break: break-word;
}
</style>
