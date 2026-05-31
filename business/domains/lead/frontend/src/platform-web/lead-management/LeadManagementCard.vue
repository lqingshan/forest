<template>
  <article class="lead-panel">
    <header class="lead-panel__header">
      <div>
        <p>{{ lead.sourceType || 'MANUAL' }}</p>
        <h3>{{ lead.name }}</h3>
      </div>
      <span>{{ lead.country || '未知地区' }}</span>
    </header>
    <p class="lead-panel__intro">{{ lead.intro || '暂无介绍' }}</p>
    <dl class="lead-panel__grid">
      <div>
        <dt>分类</dt>
        <dd>{{ lead.category || '未分类' }}</dd>
      </div>
      <div>
        <dt>关键词</dt>
        <dd>{{ lead.keywords || '未填写' }}</dd>
      </div>
      <div>
        <dt>邮箱</dt>
        <dd>{{ lead.email || '未填写' }}</dd>
      </div>
      <div>
        <dt>网站</dt>
        <dd>{{ lead.website || '未填写' }}</dd>
      </div>
    </dl>
  </article>
</template>

<script setup lang="ts">
import type { LeadPlatformItem } from './types'

defineProps<{ lead: LeadPlatformItem }>()
</script>

<style scoped>
.lead-panel {
  padding: 18px;
  border-radius: var(--workspace-radius-lg);
  background: linear-gradient(180deg, var(--workspace-card-paper-start), var(--workspace-card-paper-end));
  border: 1px solid var(--workspace-border-soft);
  box-shadow: var(--workspace-shadow-soft);
}

.lead-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.lead-panel__header p {
  margin: 0;
  font-size: 11px;
  color: var(--workspace-text-tertiary);
  font-family: var(--workspace-font-mono);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.lead-panel__header h3 {
  margin: 6px 0 0;
  font-size: 24px;
  color: var(--workspace-text-primary);
  font-family: var(--workspace-font-heading);
}

.lead-panel__header span {
  color: var(--workspace-text-secondary);
  font-weight: 600;
}

.lead-panel__intro {
  margin: 14px 0;
  color: var(--workspace-text-secondary);
  line-height: 1.6;
}

.lead-panel__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 0;
}

.lead-panel__grid dt {
  color: var(--workspace-text-tertiary);
  font-family: var(--workspace-font-mono);
  font-size: 11px;
}

.lead-panel__grid dd {
  margin: 4px 0 0;
  color: var(--workspace-text-primary);
  font-weight: 600;
}
</style>
