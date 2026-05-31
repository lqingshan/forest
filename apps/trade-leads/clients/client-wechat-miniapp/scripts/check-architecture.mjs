import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const repoRoot = path.resolve(projectRoot, '../../../..')
const baseFrontendRoot = path.join(repoRoot, 'base-frontend')
const businessRoot = path.join(repoRoot, 'business')

const sourceExtensions = new Set(['.ts', '.tsx', '.js', '.mjs', '.vue'])
const ignoredDirectories = new Set(['node_modules', 'dist', 'target', '.turbo', '.git'])
const businessPackageNames = [
  '@forest/file',
  '@forest/user',
  '@forest/lead',
  '@forest/point',
  '@forest/recharge',
  '@forest/payment',
  '@forest/user-lead'
]

const failures = []

assertNoForbiddenText({
  root: path.join(baseFrontendRoot, 'packages/wechat-miniapp-platform/src'),
  forbiddenTexts: businessPackageNames,
  message: 'wechat-miniapp-platform 是微信平台基础设施，不允许依赖 business 包'
})

assertNoForbiddenText({
  root: path.join(baseFrontendRoot, 'packages/wechat-miniapp-client-session/src'),
  forbiddenTexts: businessPackageNames,
  message: 'wechat-miniapp-client-session 只能通过注入函数编排登录，不允许依赖 business 包'
})

assertNoForbiddenText({
  root: path.join(baseFrontendRoot, 'packages/wechat-miniapp-client-app/src'),
  forbiddenTexts: businessPackageNames,
  message: 'wechat-miniapp-client-app 只能装配通用微信小程序基础设施，不允许直接依赖 business 包'
})

for (const sourceRoot of businessFrontendSourceRoots()) {
  assertNoForbiddenText({
    root: sourceRoot,
    forbiddenTexts: [
      'apps/trade-leads',
      '@forest/wechat-miniapp-platform',
      '@forest/wechat-miniapp-client-session',
      '@forest/wechat-miniapp-client-app'
    ],
    message: 'business frontend 不允许依赖 app 或微信小程序 app/platform 装配'
  })
}

assertNoTrackedSourceFiles({
  root: path.join(projectRoot, 'src/auth'),
  message: 'trade-leads 小程序不应继续保留 app 内登录装配目录，请使用 src/app-definition.ts 与 src/miniapp-app.ts'
})

assertNoTrackedSourceFiles({
  root: path.join(projectRoot, 'src/platform'),
  message: 'trade-leads 小程序不应继续保留 src/platform/* 通用实现，请统一上移到 base-frontend/packages/*'
})

assertPathMissing({
  target: path.join(projectRoot, 'src/app-platform.ts'),
  message: 'trade-leads 小程序不应继续保留 src/app-platform.ts，请统一从 src/miniapp-app.ts 取装配好的能力'
})

assertNoForbiddenText({
  root: path.join(projectRoot, 'src'),
  forbiddenTexts: ['@forest/wechat-miniapp-platform', '@forest/wechat-miniapp-client-session'],
  message: 'trade-leads 小程序 app 层应统一从 src/miniapp-app.ts 取装配好的能力'
})

if (failures.length) {
  console.error('[miniapp architecture] failed')
  for (const failure of failures) {
    console.error(`- ${failure}`)
  }
  process.exit(1)
}

console.log('[miniapp architecture] passed')

function businessFrontendSourceRoots() {
  const roots = []
  for (const scope of ['domains', 'aggregations']) {
    const scopeRoot = path.join(businessRoot, scope)
    if (!fs.existsSync(scopeRoot)) {
      continue
    }
    for (const item of fs.readdirSync(scopeRoot, { withFileTypes: true })) {
      if (!item.isDirectory()) {
        continue
      }
      const sourceRoot = path.join(scopeRoot, item.name, 'frontend/src')
      if (fs.existsSync(sourceRoot)) {
        roots.push(sourceRoot)
      }
    }
  }
  return roots
}

function assertNoForbiddenText({ root, forbiddenTexts, message }) {
  if (!fs.existsSync(root)) {
    return
  }
  for (const file of walk(root)) {
    const content = fs.readFileSync(file, 'utf8')
    for (const forbiddenText of forbiddenTexts) {
      if (content.includes(forbiddenText)) {
        failures.push(`${message}: ${path.relative(repoRoot, file)} 包含 ${forbiddenText}`)
      }
    }
  }
}

function assertNoTrackedSourceFiles({ root, message }) {
  if (!fs.existsSync(root)) {
    return
  }
  const files = walk(root)
  if (!files.length) {
    return
  }
  for (const file of files) {
    failures.push(`${message}: ${path.relative(repoRoot, file)}`)
  }
}

function assertPathMissing({ target, message }) {
  if (!fs.existsSync(target)) {
    return
  }
  failures.push(`${message}: ${path.relative(repoRoot, target)}`)
}

function walk(directory) {
  const files = []
  for (const item of fs.readdirSync(directory, { withFileTypes: true })) {
    if (ignoredDirectories.has(item.name)) {
      continue
    }
    const fullPath = path.join(directory, item.name)
    if (item.isDirectory()) {
      files.push(...walk(fullPath))
      continue
    }
    if (sourceExtensions.has(path.extname(item.name))) {
      files.push(fullPath)
    }
  }
  return files
}
