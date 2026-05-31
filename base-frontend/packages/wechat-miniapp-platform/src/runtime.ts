/**
 * 微信小程序运行时抽象。
 *
 * 这个模块只负责描述和获取“小程序运行环境”：
 * - 真实运行时：从 globalThis 读取 wx/getApp/getCurrentPages
 * - 测试运行时：使用调用方注入的 fake runtime
 *
 * 其它模块不直接碰全局 wx，而是通过 getRuntime() 获取运行时能力。
 */

/**
 * MiniappRecord 表示“普通对象”。
 *
 * Record<string, any> 等价于：
 *
 * {
 *   [key: string]: any
 * }
 *
 * platform 层不知道具体 app 的 currentUser、wx 回调结果、globalData
 * 长什么样，所以这里用宽松对象类型。具体 app 会用泛型把类型收窄。
 */
export type MiniappRecord = Record<string, any>

/**
 * getCurrentPages() 返回的小程序页面对象很复杂。
 * 这个包只关心 route 和 options，因此只声明最小需要字段。
 */
export interface MiniappPage {
  route: string
  options?: Record<string, string | number | boolean | null | undefined>
}

/**
 * MiniappRuntime 是对微信小程序全局运行时的抽象。
 *
 * 正常运行时，这些能力来自全局对象：
 * - wx
 * - getApp
 * - getCurrentPages
 *
 * 单测里没有真实微信环境，所以允许外部注入 runtime。
 */
export interface MiniappRuntime {
  wx: MiniappRecord
  getApp?: () => MiniappRecord
  getCurrentPages?: () => MiniappPage[]
}

/**
 * 所有依赖微信运行时的函数，都可以通过 runtime 注入一个假的微信环境。
 * 不传 runtime 时，会从 globalThis 上读取真实 wx/getApp/getCurrentPages。
 */
export interface MiniappRuntimeOptions {
  runtime?: MiniappRuntime
}

/**
 * 获取微信小程序运行时。
 *
 * 优先使用测试/app 显式注入的 runtime；
 * 否则从 globalThis 上读取真实小程序全局对象。
 *
 * 如果当前代码跑在非小程序环境，而且又没有注入 runtime，会直接抛错。
 * 这比静默失败更好，因为 platform API 本来就必须依赖微信环境。
 *
 * 注意：该函数只给包内模块使用，不从 public index.ts 暴露。
 */
export function getRuntime(runtime?: MiniappRuntime): MiniappRuntime {
  if (runtime) {
    return runtime
  }

  const globalRuntime = globalThis as MiniappRecord
  if (!globalRuntime.wx) {
    throw new Error('微信小程序运行时不可用')
  }

  return {
    wx: globalRuntime.wx,
    getApp: globalRuntime.getApp,
    getCurrentPages: globalRuntime.getCurrentPages
  }
}
