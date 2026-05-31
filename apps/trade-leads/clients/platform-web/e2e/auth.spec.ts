import { expect, test } from '@playwright/test'

test('@smoke @regression @postdeploy @readonly AUTH-001 登录成功', async ({ page }) => {
  await page.goto('/users')

  await expect(page.getByRole('heading', { name: '手机号密码登录' })).toBeVisible()
  await page.getByPlaceholder('请输入手机号').fill('+8618257147892')
  await page.getByPlaceholder('请输入密码').fill('123456abc')
  await page.getByRole('button', { name: '密码登录' }).click()

  await page.waitForLoadState('networkidle')

  await expect(page).toHaveURL(/\/users$/)
  await expect(page.locator('.workspace-layout__header h2', { hasText: '用户管理' })).toBeVisible()
  await expect(page.locator('.workspace-layout__sidebar')).toBeVisible()
  await expect(page.getByRole('button', { name: '退出登录' })).toBeVisible()
})
