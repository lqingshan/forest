<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FileCategory, FileInfo, FileUploadItemStatus, FileUploadProgress } from '../../shared/types'
import { uploadWebFile, uploadWebFiles } from './api'

interface UploadPanelItem {
  id: string
  file: File
  name: string
  progress: number
  status: FileUploadItemStatus
  result?: FileInfo
  errorMessage?: string
}

const props = withDefaults(defineProps<{
  fileCategory?: FileCategory
  multiple?: boolean
  maxCount?: number
  concurrency?: number
  disabled?: boolean
  accept?: string
  chooseText?: string
  uploadingText?: string
  retryText?: string
}>(), {
  fileCategory: 'IMAGE',
  multiple: true,
  maxCount: 9,
  concurrency: 3,
  disabled: false,
  accept: '',
  chooseText: '选择文件',
  uploadingText: '上传中',
  retryText: '重试'
})

const emit = defineEmits<{
  uploaded: [payload: { files: FileInfo[], failedItems: UploadPanelItem[], items: UploadPanelItem[] }]
  progress: [payload: { index: number, progress: FileUploadProgress }]
  uploaderror: [payload: { index: number, message: string }]
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const progress = ref(0)
const errorMessage = ref('')
const items = ref<UploadPanelItem[]>([])

const inputAccept = computed(() => {
  if (props.accept) {
    return props.accept
  }
  if (props.fileCategory === 'IMAGE') {
    return 'image/jpeg,image/png,image/webp,image/gif'
  }
  if (props.fileCategory === 'VIDEO') {
    return 'video/mp4,video/quicktime,video/x-msvideo'
  }
  if (props.fileCategory === 'AUDIO') {
    return 'audio/mpeg,audio/mp4,audio/aac,audio/wav,audio/x-wav,audio/ogg,audio/flac'
  }
  return '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt'
})

async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const maxSelectableCount = props.multiple ? props.maxCount : 1
  const selectedFiles = Array.from(target.files || []).slice(0, maxSelectableCount)
  target.value = ''
  if (!selectedFiles.length || props.disabled || uploading.value) {
    return
  }

  items.value = selectedFiles.map((file, index) => ({
    id: `${Date.now()}-${index}`,
    file,
    name: file.name,
    progress: 0,
    status: 'PENDING'
  }))
  errorMessage.value = ''
  uploading.value = true

  const result = await uploadWebFiles({
    files: selectedFiles,
    fileCategory: props.fileCategory,
    concurrency: props.concurrency,
    onFileStatusChange: (item) => {
      updateItem(item.index, {
        progress: item.progress,
        status: item.status,
        result: item.file,
        errorMessage: item.errorMessage
      })
    },
    onFileProgress: (index, itemProgress) => {
      emit('progress', { index, progress: itemProgress })
    },
    onFileFailed: (index, error) => {
      emit('uploaderror', { index, message: error.message })
    }
  })

  uploading.value = false
  if (result.hasFailed) {
    errorMessage.value = `${result.failedItems.length} 个文件上传失败，可单独重试`
  }
  emit('uploaded', {
    files: result.files,
    failedItems: failedItems(),
    items: [...items.value]
  })
}

async function retryItem(index: number) {
  const item = items.value[index]
  if (!item || uploading.value) {
    return
  }

  uploading.value = true
  errorMessage.value = ''
  updateItem(index, {
    progress: 0,
    status: 'UPLOADING',
    errorMessage: ''
  })

  try {
    const file = await uploadWebFile({
      file: item.file,
      fileCategory: props.fileCategory,
      onProgress: (itemProgress) => {
        updateItem(index, {
          progress: itemProgress.progress
        })
        emit('progress', { index, progress: itemProgress })
      }
    })
    updateItem(index, {
      progress: 100,
      status: 'SUCCESS',
      result: file,
      errorMessage: ''
    })
    emit('uploaded', {
      files: successfulFiles(),
      failedItems: failedItems(),
      items: [...items.value]
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '文件上传失败'
    updateItem(index, {
      status: 'FAILED',
      errorMessage: message
    })
    errorMessage.value = message
    emit('uploaderror', { index, message })
  } finally {
    uploading.value = false
  }
}

function updateItem(index: number, patch: Partial<UploadPanelItem>) {
  const nextItems = [...items.value]
  nextItems[index] = {
    ...nextItems[index],
    ...patch
  }
  items.value = nextItems
  progress.value = calculateTotalProgress(nextItems)
}

function successfulFiles() {
  return items.value
    .filter((item) => item.status === 'SUCCESS' && item.result)
    .map((item) => item.result as FileInfo)
}

function failedItems() {
  return items.value.filter((item) => item.status === 'FAILED')
}

function calculateTotalProgress(nextItems: UploadPanelItem[]) {
  if (!nextItems.length) {
    return 0
  }
  return Math.round(nextItems.reduce((sum, item) => sum + Number(item.progress || 0), 0) / nextItems.length)
}
</script>

<template>
  <div class="forest-file-upload-panel">
    <input
      ref="fileInput"
      class="forest-file-upload-panel__input"
      type="file"
      hidden
      :accept="inputAccept"
      :multiple="multiple"
      :disabled="disabled || uploading"
      @change="handleFileChange"
    >

    <button
      class="forest-file-upload-panel__button"
      type="button"
      :disabled="disabled || uploading"
      @click="fileInput?.click()"
    >
      {{ uploading ? uploadingText : chooseText }}
    </button>

    <div v-if="uploading" class="forest-file-upload-panel__progress">
      <div class="forest-file-upload-panel__progress-bar" :style="{ width: `${progress}%` }" />
    </div>

    <div v-if="items.length" class="forest-file-upload-panel__items">
      <div
        v-for="(item, index) in items"
        :key="item.id"
        class="forest-file-upload-panel__item"
      >
        <div class="forest-file-upload-panel__item-main">
          <span class="forest-file-upload-panel__item-name">{{ item.name }}</span>
          <span v-if="item.errorMessage" class="forest-file-upload-panel__item-error">
            {{ item.errorMessage }}
          </span>
        </div>
        <div class="forest-file-upload-panel__item-action">
          <span class="forest-file-upload-panel__item-status">
            {{ item.status === 'SUCCESS' ? '成功' : item.status === 'FAILED' ? '失败' : `${item.progress}%` }}
          </span>
          <button
            v-if="item.status === 'FAILED'"
            class="forest-file-upload-panel__retry"
            type="button"
            :disabled="uploading"
            @click="retryItem(index)"
          >
            {{ retryText }}
          </button>
        </div>
      </div>
    </div>

    <p v-if="errorMessage" class="forest-file-upload-panel__error">
      {{ errorMessage }}
    </p>
  </div>
</template>
