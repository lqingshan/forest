import { expect, test } from '@playwright/test'
import { createLeadDraft, login } from './helpers'

test('@write LEAD-CRUD 线索增删改查', async ({ page }) => {
  const lead = createLeadDraft()
  const updatedCategory = `${lead.category} Updated`
  const updatedIntro = `${lead.intro} Updated`

  await login(page)
  await page.getByRole('link', { name: '线索管理' }).click()

  await expect(page).toHaveURL(/\/lead$/)
  await expect(page.locator('.workspace-layout__header h2', { hasText: '线索管理' })).toBeVisible()

  const leadForm = page.locator('.lead-form')

  await leadForm.getByPlaceholder('名称').fill(lead.name)
  await leadForm.getByPlaceholder('来源类型').fill(lead.sourceType)
  await leadForm.getByPlaceholder('关键词').fill(lead.keywords)
  await leadForm.getByPlaceholder('分类').fill(lead.category)
  await leadForm.getByPlaceholder('国家').fill(lead.country)
  await leadForm.getByPlaceholder('电话').fill(lead.phone)
  await leadForm.getByPlaceholder('邮箱').fill(lead.email)
  await leadForm.getByPlaceholder('网站').fill(lead.website)
  await leadForm.getByPlaceholder('简介').fill(lead.intro)
  await page.getByRole('button', { name: '新增线索' }).click()

  const leadCard = page.locator('.lead-panel')
  await expect(leadCard).toContainText(lead.name)
  await expect(leadCard).toContainText(lead.category)
  await expect(leadCard).toContainText(lead.intro)

  await page.getByPlaceholder('按名称、类目或关键词搜索').fill(lead.keywords)
  await page.getByPlaceholder('按国家查询').fill(lead.country)
  await page.getByRole('button', { name: '查询线索' }).click()

  const leadRow = page.locator('.workspace-table__button').filter({ hasText: lead.name }).first()
  await expect(leadRow).toBeVisible()

  await leadRow.click()
  await expect(page.getByRole('button', { name: '保存修改' })).toBeVisible()

  await leadForm.getByPlaceholder('分类').fill(updatedCategory)
  await leadForm.getByPlaceholder('简介').fill(updatedIntro)
  await page.getByRole('button', { name: '保存修改' }).click()

  await expect(leadCard).toContainText(updatedCategory)
  await expect(leadCard).toContainText(updatedIntro)

  page.once('dialog', async (dialog) => {
    await dialog.accept()
  })
  await page.getByRole('button', { name: '删除线索' }).click()

  await expect(page.getByRole('button', { name: '新增线索' })).toBeVisible()

  await page.getByPlaceholder('按名称、类目或关键词搜索').fill(lead.name)
  await page.getByPlaceholder('按国家查询').fill(lead.country)
  await page.getByRole('button', { name: '查询线索' }).click()

  await expect(page.locator('.workspace-table__button').filter({ hasText: lead.name })).toHaveCount(0)
})
