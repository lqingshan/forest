import type { GlobalThemeTokens, ThemeId } from './contract'
import { themeTokenKeys, workspaceThemeVarNames } from './contract'
import { themePresets } from './presets'

export type { GlobalThemeTokens, ThemeId } from './contract'
export { workspaceThemeVarNames } from './contract'
export { themePresets } from './presets'

function isDevelopmentEnvironment() {
  return Boolean(import.meta.env?.DEV)
}

export function isThemeId(themeId: string): themeId is ThemeId {
  return themeId in themePresets
}

export function resolveThemeId(
  themeId: string,
  options?: {
    isDev?: boolean
    logger?: Pick<Console, 'warn'>
  }
): ThemeId {
  if (isThemeId(themeId)) {
    return themeId
  }

  const message = `Unknown workspace theme "${themeId}".`
  if (options?.isDev ?? isDevelopmentEnvironment()) {
    throw new Error(`${message} Expected "paper" or "figma".`)
  }

  options?.logger?.warn?.(`${message} Falling back to "paper".`)
  return 'paper'
}

export function getThemeTokens(
  themeId: string,
  options?: {
    isDev?: boolean
    logger?: Pick<Console, 'warn'>
  }
): GlobalThemeTokens {
  return themePresets[resolveThemeId(themeId, options)]
}

export function applyTheme(themeId: string, target: HTMLElement = document.documentElement) {
  const resolvedThemeId = resolveThemeId(themeId)
  const tokens = themePresets[resolvedThemeId]

  for (const tokenKey of themeTokenKeys) {
    target.style.setProperty(workspaceThemeVarNames[tokenKey], tokens[tokenKey])
  }

  target.dataset.workspaceTheme = resolvedThemeId
  return resolvedThemeId
}
