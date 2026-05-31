import { build } from 'esbuild'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const workspaceRoot = path.resolve(projectRoot, '../../../../base-frontend')
const srcDir = path.join(projectRoot, 'src')
const distDir = path.join(projectRoot, 'dist')
const sharedMiniappComponentsRoot = path.join(workspaceRoot, 'packages/wechat-miniapp-ui/src/components')
const sharedMiniappComponentsDistRoot = path.join(distDir, 'components')
const watchMode = process.argv.includes('--watch')
const defaultMiniappApiBaseUrl = 'http://localhost:8082'
const defaultMiniappAppCode = 'cxc-commerce-buyer-wechat-miniapp'
const miniappApiBaseUrl = process.env.MINIAPP_API_BASE_URL || defaultMiniappApiBaseUrl
const miniappAppCode = process.env.MINIAPP_APP_CODE || defaultMiniappAppCode

const businessMiniappModules = [
  {
    name: 'forest-user',
    sourceRoot: path.resolve(projectRoot, '../../../../business/domains/user/frontend/src/wechat-miniapp')
  },
  {
    name: 'forest-file',
    sourceRoot: path.resolve(projectRoot, '../../../../business/domains/file/frontend/src/wechat-miniapp')
  }
].map((item) => ({
  ...item,
  distRoot: path.join(distDir, 'modules', item.name, 'wechat-miniapp')
}))

const staticExtensions = new Set(['.json', '.wxml', '.wxss', '.png', '.jpg', '.jpeg', '.gif', '.svg', '.webp'])

function findRuntimeEntries() {
  const entries = []
  for (const relative of ['app.ts', 'app.js']) {
    const file = path.join(srcDir, relative)
    if (fs.existsSync(file)) {
      entries.push(file)
      break
    }
  }

  for (const scope of ['pages', 'components']) {
    const scopeDir = path.join(srcDir, scope)
    if (!fs.existsSync(scopeDir)) {
      continue
    }
    for (const file of walk(scopeDir)) {
      if (/\/index\.(t|j)s$/.test(file)) {
        entries.push(file)
      }
    }
  }

  return entries
}

function findReusableMiniappEntries(directory) {
  if (!fs.existsSync(directory)) {
    return []
  }

  return walk(directory).filter((file) => {
    if (!/\/index\.(t|j)s$/.test(file)) {
      return false
    }

    return isMiniappComponentDirectory(path.dirname(file))
  })
}

function isMiniappComponentDirectory(directory) {
  const manifestPath = path.join(directory, 'index.json')
  if (!fs.existsSync(manifestPath)) {
    return false
  }

  try {
    const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'))
    return manifest.component === true
  } catch {
    return false
  }
}

function walk(directory) {
  const files = []
  for (const item of fs.readdirSync(directory, { withFileTypes: true })) {
    const fullPath = path.join(directory, item.name)
    if (item.isDirectory()) {
      files.push(...walk(fullPath))
    } else {
      files.push(fullPath)
    }
  }
  return files
}

function copyStaticFiles(sourceRoot, targetRoot) {
  if (!fs.existsSync(sourceRoot)) {
    return
  }

  for (const file of walk(sourceRoot)) {
    if (!staticExtensions.has(path.extname(file))) {
      continue
    }
    const target = path.join(targetRoot, path.relative(sourceRoot, file))
    fs.mkdirSync(path.dirname(target), { recursive: true })
    fs.copyFileSync(file, target)
  }
}

async function buildRuntimeEntries(entryPoints, outbase, outdir) {
  if (!entryPoints.length) {
    return
  }

  await build({
    absWorkingDir: projectRoot,
    entryPoints,
    outbase,
    outdir,
    bundle: true,
    format: 'cjs',
    platform: 'browser',
    target: 'es2019',
    sourcemap: false,
    logLevel: 'info',
    loader: {
      '.vue': 'text'
    },
    mainFields: ['module', 'main'],
    conditions: ['import', 'default'],
    define: {
      'process.env.NODE_ENV': '"production"',
      'process.env.MINIAPP_API_BASE_URL': JSON.stringify(miniappApiBaseUrl),
      'process.env.MINIAPP_APP_CODE': JSON.stringify(miniappAppCode)
    }
  })
}

async function buildOnce() {
  fs.rmSync(distDir, { recursive: true, force: true })
  fs.mkdirSync(distDir, { recursive: true })
  copyStaticFiles(srcDir, distDir)
  copyStaticFiles(sharedMiniappComponentsRoot, sharedMiniappComponentsDistRoot)

  await buildRuntimeEntries(findRuntimeEntries(), srcDir, distDir)
  await buildRuntimeEntries(
    findReusableMiniappEntries(sharedMiniappComponentsRoot),
    sharedMiniappComponentsRoot,
    sharedMiniappComponentsDistRoot
  )

  for (const miniappModule of businessMiniappModules) {
    copyStaticFiles(miniappModule.sourceRoot, miniappModule.distRoot)
    await buildRuntimeEntries(
      findReusableMiniappEntries(miniappModule.sourceRoot),
      miniappModule.sourceRoot,
      miniappModule.distRoot
    )
  }

  console.log(`[miniapp] built ${path.relative(process.cwd(), distDir)}`)
}

function watchDirectories() {
  const candidates = [
    srcDir,
    path.join(workspaceRoot, 'packages/http-client/src'),
    path.join(workspaceRoot, 'packages/wechat-miniapp-platform/src'),
    path.join(workspaceRoot, 'packages/wechat-miniapp-client-session/src'),
    path.join(workspaceRoot, 'packages/wechat-miniapp-client-app/src'),
    sharedMiniappComponentsRoot,
    ...businessMiniappModules.map((item) => item.sourceRoot)
  ]

  let timer = null
  const rebuild = () => {
    clearTimeout(timer)
    timer = setTimeout(() => {
      buildOnce().catch((error) => {
        console.error(error)
      })
    }, 120)
  }

  for (const directory of candidates) {
    if (!fs.existsSync(directory)) {
      continue
    }
    fs.watch(directory, { recursive: true }, rebuild)
  }
}

await buildOnce()

if (watchMode) {
  watchDirectories()
  console.log('[miniapp] watching source and reused business packages...')
  process.stdin.resume()
}
