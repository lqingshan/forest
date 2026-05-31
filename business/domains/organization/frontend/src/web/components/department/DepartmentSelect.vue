<script setup lang="ts">
import { computed } from 'vue'
import type { Department } from '../../../shared/types'
import { buildDepartmentTree, flattenDepartmentTreeOptions } from './department-options'

const props = withDefaults(defineProps<{
  departments: Department[]
  modelValue: string
  disabled?: boolean
}>(), {
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const departmentTree = computed(() => buildDepartmentTree(props.departments))
const departmentOptions = computed(() => flattenDepartmentTreeOptions(departmentTree.value))

function optionLabel(depth: number, label: string) {
  return `${'　'.repeat(depth)}${label}`
}

function handleChange(event: Event) {
  emit('update:modelValue', (event.target as HTMLSelectElement).value)
}
</script>

<template>
  <select
    :value="modelValue"
    :disabled="disabled || departmentOptions.length === 0"
    @change="handleChange"
  >
    <option v-if="departmentOptions.length === 0" value="" disabled>暂无部门</option>
    <option v-for="option in departmentOptions" :key="option.value" :value="option.value">
      {{ optionLabel(option.depth, option.label) }}
    </option>
  </select>
</template>
