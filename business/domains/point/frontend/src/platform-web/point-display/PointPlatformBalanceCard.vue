<template>
  <article class="platform-point-balance">
    <div>
      <p>用户 #{{ balance.userId }}</p>
      <strong>{{ balance.balance }}</strong>
    </div>
    <dl>
      <div>
        <dt>累计收入</dt>
        <dd>{{ balance.totalIncome }}</dd>
      </div>
      <div>
        <dt>累计支出</dt>
        <dd>{{ balance.totalSpend }}</dd>
      </div>
    </dl>
  </article>
</template>

<script setup lang="ts">
import type { PointBalanceCardModel } from './types'

defineProps<{ balance: PointBalanceCardModel }>()
</script>

<style scoped>
.platform-point-balance {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  border-radius: var(--workspace-radius-lg);
  background: linear-gradient(180deg, var(--workspace-card-paper-start), var(--workspace-card-paper-end));
  border: 1px solid var(--workspace-border-soft);
}

.platform-point-balance p {
  margin: 0;
  font-size: 11px;
  color: var(--workspace-text-tertiary);
  font-family: var(--workspace-font-mono);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.platform-point-balance strong {
  display: block;
  margin-top: 10px;
  font-size: 38px;
  color: var(--workspace-text-primary);
  font-family: var(--workspace-font-heading);
}

.platform-point-balance dl {
  display: grid;
  gap: 10px;
  margin: 0;
}

.platform-point-balance dt {
  color: var(--workspace-text-tertiary);
  font-family: var(--workspace-font-mono);
  font-size: 11px;
}

.platform-point-balance dd {
  margin: 4px 0 0;
  color: var(--workspace-text-primary);
  font-weight: 700;
}
</style>
