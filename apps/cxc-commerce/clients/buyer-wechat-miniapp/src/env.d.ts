type MiniappRecord = Record<string, any>

declare const wx: MiniappRecord

declare function App(options: MiniappRecord & ThisType<MiniappRecord>): void

declare function Page(options: MiniappRecord & ThisType<MiniappRecord>): void

declare function Component(options: MiniappRecord & ThisType<MiniappRecord>): void

declare function getApp(): MiniappRecord

declare function getCurrentPages(): Array<MiniappRecord>
