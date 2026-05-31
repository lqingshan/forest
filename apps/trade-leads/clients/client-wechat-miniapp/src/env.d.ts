declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent<Record<string, never>, Record<string, never>, unknown>
  export default component
}

type MiniappRecord = Record<string, any>

declare const wx: MiniappRecord

declare function App(options: MiniappRecord & ThisType<MiniappRecord>): void

declare function Page(options: MiniappRecord & ThisType<MiniappRecord>): void

declare function Component(options: MiniappRecord & ThisType<MiniappRecord>): void

declare function getApp(): MiniappRecord

declare function getCurrentPages(): Array<MiniappRecord>
