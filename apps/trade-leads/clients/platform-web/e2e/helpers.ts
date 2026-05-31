import { expect, type APIRequestContext, type Page } from '@playwright/test'

type ResultEnvelope<T> = {
  code: number
  message: string
  data: T
}

type ClientLoginData = {
  accessToken: string
  refreshToken: string
  firstLogin: boolean
}

type ClientUserData = {
  id: number
}

type RechargeOrderData = {
  id: number
  rechargeNo: string
  packageCode: string
  amountCents: number
  creditedPoints: number
  status: string
}

type PaymentOrderData = {
  id: number
  paymentNo: string
  bizType: string
  bizOrderId: number
  channel: string
  amountCents: number
  status: string
  outTradeNo: string
}

export async function login(page: Page) {
  await page.goto('/users')
  await expect(page.getByRole('heading', { name: '手机号密码登录' })).toBeVisible()
  await page.getByPlaceholder('请输入手机号').fill('+8618257147892')
  await page.getByPlaceholder('请输入密码').fill('123456abc')
  await page.getByRole('button', { name: '密码登录' }).click()
  await expect(page).toHaveURL(/\/users$/)
  await expect(page.locator('.workspace-layout__header h2', { hasText: '用户管理' })).toBeVisible()
}

export async function provisionClientUser(request: APIRequestContext) {
  const code = uniqueId('playwright-user')
  const phoneCode = `138${uniqueNumericSuffix(8)}`
  const response = await request.post('/api/auth/wechat-miniapp/phone-login', {
    data: {
      code,
      phoneCode,
      clientType: 'WECHAT_MINIAPP',
      appCode: 'trade-leads-miniapp',
      accessScope: 'CLIENT'
    }
  })

  expect(response.ok()).toBeTruthy()

  const result = await response.json() as ResultEnvelope<ClientLoginData>
  expect(result.code).toBe(200)
  const currentUserResponse = await request.get('/api/client/user/me', {
    headers: {
      Authorization: `Bearer ${result.data.accessToken}`
    }
  })
  expect(currentUserResponse.ok()).toBeTruthy()
  const currentUser = await currentUserResponse.json() as ResultEnvelope<ClientUserData>
  expect(currentUser.code).toBe(200)

  return {
    code,
    phone: phoneCode,
    userId: currentUser.data.id,
    accessToken: result.data.accessToken,
    refreshToken: result.data.refreshToken,
    firstLogin: result.data.firstLogin
  }
}

export async function provisionRechargedClientUser(request: APIRequestContext, packageCode = 'starter') {
  const user = await provisionClientUser(request)

  const createOrderResponse = await request.post('/api/client/recharge/orders', {
    headers: {
      Authorization: `Bearer ${user.accessToken}`
    },
    data: { packageCode }
  })

  expect(createOrderResponse.ok()).toBeTruthy()

  const createdOrder = await createOrderResponse.json() as ResultEnvelope<RechargeOrderData>
  expect(createdOrder.code).toBe(200)

  const createPaymentResponse = await request.post('/api/client/payment/orders', {
    headers: {
      Authorization: `Bearer ${user.accessToken}`
    },
    data: {
      bizType: 'RECHARGE',
      bizOrderId: createdOrder.data.id
    }
  })

  expect(createPaymentResponse.ok()).toBeTruthy()

  const paymentOrder = await createPaymentResponse.json() as ResultEnvelope<PaymentOrderData>
  expect(paymentOrder.code).toBe(200)
  expect(paymentOrder.data.status).toBe('PREPAY_CREATED')

  const notifyResponse = await request.post('/api/open/wechat/pay/notify', {
    data: {
      outTradeNo: paymentOrder.data.outTradeNo,
      transactionId: `mock-tx-${paymentOrder.data.id}`,
      amountCents: paymentOrder.data.amountCents
    }
  })

  expect(notifyResponse.ok()).toBeTruthy()

  return {
    ...user,
    orderId: createdOrder.data.id,
    paymentOrderId: paymentOrder.data.id,
    packageCode: createdOrder.data.packageCode,
    points: createdOrder.data.creditedPoints
  }
}

export function createLeadDraft() {
  const suffix = uniqueId('lead')

  return {
    name: `Playwright ${suffix}`,
    sourceType: 'PLAYWRIGHT',
    keywords: `keyword-${suffix}`,
    category: 'Automation',
    country: 'CN',
    phone: '13800138000',
    email: `${suffix}@forest.example`,
    website: `https://${suffix}.forest.example`,
    intro: `Created by Playwright ${suffix}`
  }
}

function uniqueId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function uniqueNumericSuffix(length: number) {
  const seed = `${Date.now()}${Math.floor(Math.random() * 1000000)}`
  return seed.slice(-length).padStart(length, '0')
}
