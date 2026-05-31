import { expect, test } from '@playwright/test'
import { login, provisionClientUser } from './helpers'

test('@smoke @write USER-003 用户冻结与恢复', async ({ page, request }) => {
  const seededUser = await provisionClientUser(request)

  await login(page)

  await page.getByPlaceholder('按用户 ID 查询').fill(String(seededUser.userId))
  await page.getByRole('button', { name: '查询' }).click()

  const userRow = page
    .locator('.workspace-table__button')
    .filter({ hasText: new RegExp(`(^|\\D)${seededUser.userId}(\\D|$)`) })
    .first()

  await expect(userRow).toBeVisible()
  await expect(userRow).toContainText('ACTIVE')

  await userRow.click()

  await expect(page.locator('.user-card')).toContainText(`ID: ${seededUser.userId}`)
  await expect(page.locator('.user-card__status')).toHaveText('ACTIVE')

  await page.getByRole('button', { name: '冻结用户' }).click()

  await expect(page.locator('.user-card__status')).toHaveText('FROZEN')
  await expect(page.getByRole('button', { name: '恢复用户' })).toBeVisible()
  await expect(userRow).toContainText('FROZEN')

  await page.getByRole('button', { name: '恢复用户' }).click()

  await expect(page.locator('.user-card__status')).toHaveText('ACTIVE')
  await expect(page.getByRole('button', { name: '冻结用户' })).toBeVisible()
  await expect(userRow).toContainText('ACTIVE')
})

test('@smoke @regression AUTH-003 退出登录与路由守卫', async ({ page }) => {
  await login(page)

  await page.getByRole('button', { name: '退出登录' }).click()

  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: '手机号密码登录' })).toBeVisible()

  await page.goto('/users')

  await expect(page).toHaveURL(/\/login$/)
})
