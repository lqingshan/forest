type MiniappRecord = Record<string, any>

declare function Component(options: MiniappRecord & ThisType<MiniappRecord>): void
