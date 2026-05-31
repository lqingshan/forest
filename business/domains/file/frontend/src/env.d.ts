type MiniappRecord = Record<string, any>

declare const wx: MiniappRecord

declare function Component(options: MiniappRecord & ThisType<MiniappRecord>): void

declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent<Record<string, never>, Record<string, never>, unknown>
  export default component
}
