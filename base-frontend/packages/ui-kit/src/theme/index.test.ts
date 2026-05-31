import { beforeEach, describe, expect, it, vi } from 'vitest'
import { applyTheme, getThemeTokens, resolveThemeId } from './index'
import { themeTokenKeys, workspaceThemeVarNames } from './contract'
import { themePresets } from './presets'

describe('theme presets', () => {
  beforeEach(() => {
    document.documentElement.removeAttribute('style')
    delete document.documentElement.dataset.workspaceTheme
  })

  it('applies paper theme variables to the document root', () => {
    applyTheme('paper')

    expect(document.documentElement.dataset.workspaceTheme).toBe('paper')
    expect(
      document.documentElement.style.getPropertyValue(workspaceThemeVarNames.page)
    ).toBe(themePresets.paper.page)
    expect(
      document.documentElement.style.getPropertyValue(workspaceThemeVarNames.tableHeaderBg)
    ).toBe(themePresets.paper.tableHeaderBg)
    expect(
      document.documentElement.style.getPropertyValue(workspaceThemeVarNames.fontMono)
    ).toBe(themePresets.paper.fontMono)
  })

  it('applies figma theme variables to a custom target', () => {
    const target = document.createElement('div')

    applyTheme('figma', target)

    expect(target.dataset.workspaceTheme).toBe('figma')
    expect(target.style.getPropertyValue(workspaceThemeVarNames.textPrimary)).toBe(
      themePresets.figma.textPrimary
    )
    expect(target.style.getPropertyValue(workspaceThemeVarNames.buttonPrimaryBackground)).toBe(
      themePresets.figma.buttonPrimaryBackground
    )
    expect(target.style.getPropertyValue(workspaceThemeVarNames.focusOutlineStyle)).toBe(
      themePresets.figma.focusOutlineStyle
    )
  })

  it('throws for an unknown theme in development', () => {
    expect(() => resolveThemeId('unknown-theme', { isDev: true })).toThrow(
      'Unknown workspace theme "unknown-theme". Expected "paper" or "figma".'
    )
  })

  it('falls back to paper for an unknown theme in production', () => {
    const warn = vi.fn()

    expect(resolveThemeId('unknown-theme', { isDev: false, logger: { warn } })).toBe('paper')
    expect(getThemeTokens('unknown-theme', { isDev: false, logger: { warn } })).toEqual(
      themePresets.paper
    )
    expect(warn).toHaveBeenCalledWith(
      'Unknown workspace theme "unknown-theme". Falling back to "paper".'
    )
  })

  it('keeps preset keys aligned with CSS variable bindings', () => {
    expect(Object.keys(themePresets.paper).sort()).toEqual(Object.keys(themePresets.figma).sort())
    expect(Object.keys(themePresets.paper).sort()).toEqual([...themeTokenKeys].sort())
    expect(themeTokenKeys.every((key) => workspaceThemeVarNames[key])).toBe(true)
  })
})
