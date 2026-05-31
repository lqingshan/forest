import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 120_000,
  fullyParallel: false,
  workers: 1,
  retries: 0,
  expect: {
    timeout: 30_000
  },
  use: {
    baseURL: process.env.ADMIN_WEB_BASE_URL ?? 'https://127.0.0.1',
    ignoreHTTPSErrors: true,
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure'
  }
})
