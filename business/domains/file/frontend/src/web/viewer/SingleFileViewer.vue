<script setup lang="ts">
import { computed } from 'vue'
import { buildSingleFileViewerModel } from '../../shared/viewer'
import type { FileInfo } from '../../shared/types'

const props = defineProps<{
  file: FileInfo
  previewUrl?: string
  downloadUrl?: string
}>()

const model = computed(() => buildSingleFileViewerModel({
  file: props.file,
  previewUrl: props.previewUrl,
  downloadUrl: props.downloadUrl
}))

function openPreview() {
  if (!model.value.canPreview) {
    return
  }
  window.open(model.value.previewUrl, '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <div class="forest-single-file-viewer">
    <div class="forest-single-file-viewer__media">
      <img
        v-if="model.kind === 'IMAGE' && model.previewUrl"
        class="forest-single-file-viewer__image"
        :src="model.previewUrl"
        :alt="model.title"
        @click="openPreview"
      >
      <video
        v-else-if="model.kind === 'VIDEO' && model.previewUrl"
        class="forest-single-file-viewer__video"
        :src="model.previewUrl"
        controls
      />
      <audio
        v-else-if="model.kind === 'AUDIO' && model.previewUrl"
        class="forest-single-file-viewer__audio"
        :src="model.previewUrl"
        controls
      />
      <div v-else class="forest-single-file-viewer__placeholder">
        <span>{{ model.kind }}</span>
      </div>
    </div>

    <div class="forest-single-file-viewer__body">
      <strong class="forest-single-file-viewer__title">{{ model.title }}</strong>
      <span class="forest-single-file-viewer__meta">{{ model.contentType || 'unknown' }}</span>
      <div class="forest-single-file-viewer__actions">
        <button
          v-if="model.kind === 'DOCUMENT' && model.canPreview"
          class="forest-single-file-viewer__button"
          type="button"
          @click="openPreview"
        >
          预览
        </button>
        <a
          v-if="model.canDownload"
          class="forest-single-file-viewer__button"
          :href="model.downloadUrl"
          target="_blank"
          rel="noreferrer"
        >
          下载
        </a>
        <span v-if="!model.canPreview && !model.canDownload" class="forest-single-file-viewer__unavailable">
          文件地址不可用
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forest-single-file-viewer {
  display: grid;
  grid-template-columns: minmax(96px, 160px) 1fr;
  gap: var(--forest-file-viewer-gap, 16px);
  align-items: start;
}

.forest-single-file-viewer__media {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: var(--forest-file-viewer-radius, 8px);
  background: var(--forest-file-viewer-media-bg, #f4f7fb);
}

.forest-single-file-viewer__image,
.forest-single-file-viewer__video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.forest-single-file-viewer__audio {
  width: 100%;
  margin-top: 24px;
}

.forest-single-file-viewer__placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: var(--forest-file-viewer-muted, #6b7280);
  font-size: 13px;
}

.forest-single-file-viewer__body {
  min-width: 0;
  display: grid;
  gap: 8px;
}

.forest-single-file-viewer__title {
  overflow-wrap: anywhere;
  font-size: 14px;
}

.forest-single-file-viewer__meta,
.forest-single-file-viewer__unavailable {
  color: var(--forest-file-viewer-muted, #6b7280);
  font-size: 12px;
}

.forest-single-file-viewer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.forest-single-file-viewer__button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid var(--forest-file-viewer-border, #d6dde8);
  border-radius: var(--forest-file-viewer-button-radius, 6px);
  background: var(--forest-file-viewer-button-bg, #ffffff);
  color: var(--forest-file-viewer-button-color, #1f2937);
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
}
</style>
