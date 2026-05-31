import { expect, test } from '@playwright/test'
import { login, provisionRechargedClientUser } from './helpers'

test('@smoke @regression @postdeploy @readonly POINT-001 旧积分路由跳转到用户积分页', async ({ page }) => {
  await login(page)

  await page.goto('/point')

  await expect(page).toHaveURL(/\/user-point$/)
  await expect(page.locator('.workspace-layout__header h2', { hasText: '积分查询' })).toBeVisible()
})

test('@smoke @regression @postdeploy @readonly POINT-002 从用户页进入积分详情', async ({ page }) => {
  await login(page)

  await page.getByPlaceholder('按名称查询').fill('admin')
  await page.getByRole('button', { name: '查询' }).click()
  await page.locator('.workspace-table__button', { hasText: 'admin' }).first().click()
  await expect(page.getByRole('button', { name: '查看积分' })).toBeVisible()
  await page.getByRole('button', { name: '查看积分' }).click()

  await expect(page).toHaveURL(/\/user-point\?userId=\d+/)
  await expect(page.locator('.workspace-layout__header h2', { hasText: '积分查询' })).toBeVisible()
  await expect(page.locator('.workspace-card__heading h3', { hasText: '用户积分详情' })).toBeVisible()
  await expect(page.getByText('用户 #')).toBeVisible()
  await page.getByRole('button', { name: '查询用户积分' }).click()
  await expect(page.getByText('请输入用户名、手机号或邮箱')).toHaveCount(0)
})

test('@smoke @regression POINT-004 积分页短字符校验', async ({ page }) => {
  await login(page)

  await page.getByRole('link', { name: '积分查询' }).click()

  await expect(page).toHaveURL(/\/user-point$/)

  await page.getByPlaceholder('手机号，至少 2 个字符').fill('1')
  await page.getByRole('button', { name: '查询用户积分' }).click()

  await expect(page.getByText('手机号或邮箱至少输入 2 个字符')).toBeVisible()
})

test('@write POINT-005 积分页展示余额与充值流水', async ({ page, request }) => {
  const seededUser = await provisionRechargedClientUser(request)

  await login(page)
  await page.getByRole('link', { name: '积分查询' }).click()

  await expect(page).toHaveURL(/\/user-point$/)

  await page.getByRole('button', { name: '查询用户积分' }).click()

  const userRow = page
    .locator('.workspace-table__button')
    .filter({ hasText: new RegExp(`(^|\\D)${seededUser.userId}(\\D|$)`) })
    .first()

  await expect(userRow).toBeVisible()
  await expect(userRow).toContainText(String(seededUser.points))

  await userRow.click()

  await expect(page).toHaveURL(new RegExp(`/user-point\\?userId=${seededUser.userId}$`))
  await expect(page.locator('.admin-point-balance')).toContainText(`用户 #${seededUser.userId}`)
  await expect(page.locator('.admin-point-balance')).toContainText(String(seededUser.points))
  await expect(page.locator('.point-log-table')).toContainText('RECHARGE')
  await expect(page.locator('.point-log-table')).toContainText(String(seededUser.points))
  await expect(page.locator('.point-log-table')).toContainText(`recharge:${seededUser.orderId}`)
})
