# 通用异步任务系统设计方案

## 1. 设计目标

通用异步任务系统用于承接长耗时、大数据量、外部模型调用和需要产物落盘的业务流程，例如：

- 按时间区间导出大量数据。
- 图片生成、图片处理、OCR、音频转写。
- 视频生成、视频转码、长内容导出。
- 后续的 PPT、Excel、批量报表、网页提取等任务。

任务系统的核心目标：

- 避免 HTTP 请求长时间阻塞。
- 支持排队、执行、进度、取消、失败、重试和超时处理。
- 支持任务上下文固化，worker 不依赖浏览器登录态。
- 支持任务产物统一管理，文件通过文件域和 OSS 私有访问。
- 支持后台审计和问题排查。

用户侧看到的不是一个同步下载接口，而是：

```text
创建任务
-> 后台执行
-> 生成产物
-> 用户在任务中心查看进度
-> 完成后下载或查看结果
```

首版采用数据库扫描触发任务，不引入 Redis Stream、RabbitMQ 或 Kafka。后续如果任务量上升，可以在不改变业务 API 的前提下，把触发层升级为消息队列。

## 2. 总体架构

整体链路：

```text
Client / Platform API
  -> Task Application Service
    -> 创建 async_task
    -> 固化身份、权限和请求参数
    -> status = QUEUED

Task Worker Scheduler
  -> 定时扫描 QUEUED 任务
  -> 数据库条件更新抢占任务
  -> status = RUNNING

Task Dispatcher
  -> 根据 task_type 查找白名单 handler
  -> handler.validate(...)
  -> handler.execute(...)

Task Handler
  -> 调用业务查询、导出逻辑、AI provider、文件服务
  -> 更新 progress/current_step
  -> 写 async_task_artifact
  -> status = SUCCEEDED / FAILED / CANCELED
```

模块边界建议：

- `business/domains/task/backend`：通用任务域，拥有任务表、状态机、worker、dispatcher、任务 API。
- `business/domains/file/backend`：继续负责文件元数据、OSS 对象、下载鉴权、签名 URL。
- 各业务域或聚合模块：提供具体 handler，负责解释业务参数和执行业务逻辑。
- `apps/*/backend`：装配任务域和业务 handler，不在 app 层重新实现任务系统。

任务域只负责“任务生命周期”，不直接理解下载、图片、视频的业务细节。具体任务由 handler 实现。

## 3. 任务触发机制

### 3.1 首版：数据库扫描

worker 每 3 秒扫描一批排队任务：

```sql
select id
from async_task
where status = 'QUEUED'
  and deleted = 0
  and (next_retry_time is null or next_retry_time <= now())
order by priority desc, created_time asc
limit :batchSize;
```

拿到候选任务后逐个抢占：

```sql
update async_task
set status = 'RUNNING',
    worker_id = :workerId,
    started_time = coalesce(started_time, now()),
    heartbeat_time = now(),
    modified_time = now()
where id = :taskId
  and status = 'QUEUED'
  and deleted = 0;
```

只有更新行数为 `1` 的 worker 才真正获得执行权。多个 worker 同时扫描时，同一任务最多只会被一个 worker 执行。

默认参数：

| 配置项 | 建议值 | 说明 |
|---|---:|---|
| `scanInterval` | 3 秒 | 扫描 `QUEUED` 任务 |
| `batchSize` | 20 | 单次最多抢占任务数 |
| `heartbeatInterval` | 10 秒 | 运行中刷新心跳 |
| `runningTimeout` | 30 分钟 | 超过后认为 worker 可能失联 |
| `maxRetries` | 2 | 默认失败重试次数 |

### 3.2 定时维护任务

除主扫描器外，还需要维护扫描器：

- 每 1 分钟扫描超时 `RUNNING` 任务。
- 每 5 分钟处理可重试失败任务。
- 每天清理过期任务产物和过期任务记录。

超时处理规则：

```text
RUNNING 且 heartbeat_time 早于 now - runningTimeout
  -> 如果 retry_count < max_retries，改回 QUEUED 并设置 next_retry_time
  -> 否则改为 FAILED，记录 error_code = WORKER_TIMEOUT
```

可重试失败任务：

```text
FAILED 且 retry_count < max_retries 且 next_retry_time <= now
  -> status = QUEUED
```

清理任务：

```text
SUCCEEDED / FAILED / CANCELED 且 expires_time <= now
  -> status = EXPIRED
  -> 触发 artifact 过期清理或交给文件域生命周期策略处理
```

### 3.3 后续升级路径

任务创建接口和任务表不绑定扫描模式。后续可以引入 `dispatch_mode` 配置：

```text
DATABASE_SCAN
REDIS_STREAM
EXTERNAL_MQ
```

升级为消息队列时，`async_task` 仍然是事实来源，队列只负责更快唤醒 worker。worker 消费到消息后仍需读取任务表并使用状态条件抢占，避免重复消费导致重复执行。

## 4. 数据模型

### 4.1 `async_task`

`async_task` 保存任务主状态、执行上下文、请求参数和执行结果摘要。

建议字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `bigserial` | 技术主键 |
| `task_no` | `varchar(64)` | 对外任务编号，唯一 |
| `task_type` | `varchar(64)` | 任务类型，例如 `download_export` |
| `status` | `varchar(32)` | 任务状态 |
| `priority` | `integer` | 优先级，越大越靠前 |
| `user_id` | `bigint` | 创建用户 |
| `account_id` | `bigint` | 创建账号 |
| `app_code` | `varchar(64)` | 业务应用或端标识 |
| `client_type` | `varchar(64)` | 客户端类型 |
| `access_scope` | `varchar(64)` | 访问范围 |
| `request_params` | `jsonb` | 请求参数 |
| `context_snapshot` | `jsonb` | 身份、权限、数据范围快照 |
| `result_summary` | `jsonb` | 结果摘要，不存大文件内容 |
| `progress_percent` | `integer` | 0-100 |
| `current_step` | `varchar(64)` | 当前阶段 |
| `worker_id` | `varchar(128)` | 执行 worker 标识 |
| `heartbeat_time` | `timestamp` | worker 心跳时间 |
| `retry_count` | `integer` | 已重试次数 |
| `max_retries` | `integer` | 最大重试次数 |
| `next_retry_time` | `timestamp` | 下一次可重试时间 |
| `error_code` | `varchar(64)` | 失败编码 |
| `error_message` | `varchar(1000)` | 失败摘要 |
| `started_time` | `timestamp` | 首次开始时间 |
| `finished_time` | `timestamp` | 完成时间 |
| `expires_time` | `timestamp` | 任务过期时间 |
| `created_id` | `bigint` | 创建人 |
| `modified_id` | `bigint` | 修改人 |
| `deleted` | `integer` | 软删除标记 |
| `created_time` | `timestamp` | 创建时间 |
| `modified_time` | `timestamp` | 修改时间 |

推荐索引：

```sql
uk_async_task_task_no(task_no)
idx_async_task_scan(status, deleted, next_retry_time, priority, created_time)
idx_async_task_user(user_id, status, created_time)
idx_async_task_worker(status, worker_id, heartbeat_time)
idx_async_task_expire(status, expires_time)
```

### 4.2 `async_task_artifact`

`async_task_artifact` 保存任务产物索引。文件内容不进入任务表，统一通过 `file_no` 关联文件域。

建议字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `bigserial` | 技术主键 |
| `task_id` | `bigint` | 关联 `async_task.id` |
| `artifact_type` | `varchar(32)` | `FILE` / `IMAGE` / `VIDEO` / `THUMBNAIL` / `LOG` |
| `file_no` | `varchar(64)` | 文件域编号 |
| `title` | `varchar(255)` | 展示标题 |
| `mime_type` | `varchar(128)` | MIME 类型 |
| `size_bytes` | `bigint` | 文件大小 |
| `metadata` | `jsonb` | 宽高、时长、页数、模型信息等 |
| `expires_time` | `timestamp` | 产物过期时间 |
| `created_id` | `bigint` | 创建人 |
| `modified_id` | `bigint` | 修改人 |
| `deleted` | `integer` | 软删除标记 |
| `created_time` | `timestamp` | 创建时间 |
| `modified_time` | `timestamp` | 修改时间 |

推荐索引：

```sql
idx_async_task_artifact_task(task_id, deleted, created_time)
idx_async_task_artifact_file(file_no)
idx_async_task_artifact_expire(expires_time)
```

### 4.3 `async_task_log`

`async_task_log` 保存任务生命周期事件，便于后台排查和审计。

建议字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `bigserial` | 技术主键 |
| `task_id` | `bigint` | 关联任务 |
| `event_type` | `varchar(64)` | `STATUS_CHANGED` / `PROGRESS_UPDATED` / `FAILED` / `RETRIED` |
| `from_status` | `varchar(32)` | 原状态 |
| `to_status` | `varchar(32)` | 新状态 |
| `message` | `varchar(1000)` | 事件描述 |
| `event_payload` | `jsonb` | 事件上下文 |
| `created_id` | `bigint` | 创建人 |
| `modified_id` | `bigint` | 修改人 |
| `deleted` | `integer` | 软删除标记 |
| `created_time` | `timestamp` | 创建时间 |
| `modified_time` | `timestamp` | 修改时间 |

## 5. 状态机

主状态：

```text
QUEUED
RUNNING
SUCCEEDED
FAILED
CANCEL_REQUESTED
CANCELED
EXPIRED
```

状态流转：

```text
QUEUED -> RUNNING -> SUCCEEDED
                  -> FAILED
                  -> CANCEL_REQUESTED -> CANCELED

QUEUED -> CANCEL_REQUESTED -> CANCELED
FAILED -> QUEUED       # 显式重试或自动重试
SUCCEEDED -> EXPIRED
FAILED -> EXPIRED
CANCELED -> EXPIRED
```

状态规则：

- 只有 `QUEUED` 可被 worker 抢占。
- `RUNNING` 必须周期性刷新 `heartbeat_time`。
- 用户取消任务时先改为 `CANCEL_REQUESTED`，worker 在安全点停止后改为 `CANCELED`。
- `FAILED` 任务根据 `retry_count < max_retries` 和 `next_retry_time` 回到 `QUEUED`。
- `SUCCEEDED`、`FAILED`、`CANCELED`、`EXPIRED` 是终态；除显式重试外，不继续执行。
- worker 写进度前必须确认任务仍属于当前 `worker_id`，避免失联 worker 复活后覆盖新执行结果。

## 6. Handler 分发设计

任务表只保存 `task_type` 和参数，不能保存函数名、类名、脚本路径或第三方 URL。执行代码由后端白名单 registry 决定。

接口形态：

```java
public interface TaskHandler {
    String supportTaskType();

    void validate(TaskRequestParams requestParams);

    void execute(TaskExecutionContext context);

    void cancel(AsyncTask task);
}
```

白名单 registry：

| `task_type` | Handler |
|---|---|
| `download_export` | `DownloadExportTaskHandler` |
| `image_generation` | `ImageGenerationTaskHandler` |
| `video_generation` | `VideoGenerationTaskHandler` |

执行流程：

```text
worker 抢占任务
-> dispatcher 根据 task_type 找 handler
-> handler.validate(request_params)
-> 创建 TaskExecutionContext
-> handler.execute(context)
-> 任务成功则写 result_summary 和 artifact
-> 任务失败则写 error_code / error_message / retry
```

找不到 handler 的任务直接失败：

```text
status = FAILED
error_code = UNSUPPORTED_TASK_TYPE
```

这种失败不自动重试，需要开发或配置修复后由后台人工重试。

## 7. 上下文与权限

worker 执行时不能依赖当前 HTTP 请求，因为用户请求已经结束。创建任务时必须固化必要上下文。

通用上下文：

```json
{
  "user_id": 1001,
  "account_id": 2001,
  "app_code": "trade-leads",
  "client_type": "platform-web",
  "access_scope": "ADMIN"
}
```

企业或组织工作台上下文：

```json
{
  "organization_id": 88,
  "organization_no": "ORG202605260001",
  "member_id": 3001,
  "access_boundary": {
    "boundary_type": "ORGANIZATION",
    "boundary_id": 88
  }
}
```

权限规则：

- 创建任务时校验一次权限。
- worker 执行前再校验一次权限。
- 下载导出类任务必须保存数据范围快照，例如项目、组织、部门、查询条件。
- 查询业务数据时必须带上 `user_id`、`organization_id` 或 `data_scope`。
- 任务产物下载时仍要通过任务和文件域做访问鉴权。

禁止写入任务上下文的内容：

- 用户密码。
- Cookie。
- JWT access token。
- session token。
- 完整请求 header。
- 第三方供应商密钥。

worker 的身份模型是“系统代用户执行”：worker 可以使用系统服务能力调用内部模块，但每一步都必须携带任务固化的用户、组织和数据范围。

## 8. 三类任务执行策略

### 8.1 下载导出任务

适用场景：

- 按时间区间下载业务数据。
- 导出大批量线索、订单、报表、日志。
- 生成 CSV、XLSX、JSONL 等文件。

参数示例：

```json
{
  "start_time": "2026-01-01T00:00:00+08:00",
  "end_time": "2026-02-01T00:00:00+08:00",
  "format": "xlsx",
  "filters": {
    "status": "ACTIVE"
  }
}
```

执行规则：

- 时间区间统一使用左闭右开 `[start_time, end_time)`。
- 查询条件必须带上任务上下文里的权限范围。
- 禁用大 offset 分页，使用游标分页或时间分片。
- 边查边写文件，不把全量数据放入内存。
- 每完成一批更新 `processed_count` 和 `progress_percent`。
- 文件生成后写入文件域，artifact 保存 `file_no`。

游标分页示例：

```sql
select *
from records
where created_time >= :startTime
  and created_time < :endTime
  and id > :lastId
order by id asc
limit :pageSize;
```

### 8.2 图片生成任务

适用场景：

- 文生图。
- 图生图。
- 图片增强、局部重绘、抠图。

参数示例：

```json
{
  "prompt": "a forest at sunrise",
  "size": "1024x1024",
  "count": 4,
  "model": "default-image-model"
}
```

执行规则：

- 创建时校验 prompt、尺寸、数量。
- 执行前校验模型权限、用户额度、内容安全策略。
- handler 调用图片 provider，不让任务表直接保存 provider URL。
- 多张图片生成多个 artifact。
- 进度以阶段为主：

```text
VALIDATING
GENERATING
UPLOADING
DONE
```

如果第三方图片生成服务本身是异步的，handler 可以在内部轮询供应商任务状态，但平台任务状态仍以 `async_task` 为准。

### 8.3 视频生成任务

适用场景：

- 文生视频。
- 图生视频。
- 视频处理、封面生成、字幕生成。

参数示例：

```json
{
  "prompt": "product intro video",
  "duration_seconds": 10,
  "resolution": "1080p",
  "fps": 30
}
```

执行规则：

- 首版按普通任务处理，内部使用阶段化进度。
- 长视频、多段视频、混剪项目后续升级为父子任务。
- 严格限制单用户并发，避免一个用户占满 worker。
- 产物可以包括视频、封面、字幕、渲染日志。
- 失败时保留供应商错误摘要和内部 `error_code`，不直接暴露敏感供应商响应。

建议阶段：

```text
VALIDATING
GENERATING_SCRIPT
GENERATING_VIDEO
ENCODING
UPLOADING
DONE
```

## 9. API 设计

### 9.1 客户端接口

```text
POST /api/client/tasks
GET  /api/client/tasks
GET  /api/client/tasks/{taskNo}
POST /api/client/tasks/{taskNo}/cancel
POST /api/client/tasks/{taskNo}/retry
GET  /api/client/tasks/{taskNo}/artifacts
```

创建任务请求：

```json
{
  "task_type": "image_generation",
  "request_params": {
    "prompt": "a forest at sunrise",
    "size": "1024x1024",
    "count": 4
  }
}
```

任务详情响应：

```json
{
  "task_no": "TASK202605260001",
  "task_type": "image_generation",
  "status": "RUNNING",
  "progress_percent": 60,
  "current_step": "GENERATING",
  "result_summary": null,
  "error_code": null,
  "error_message": null,
  "created_time": "2026-05-26T20:00:00",
  "finished_time": null
}
```

前端默认每 2-5 秒轮询任务详情。首版不做 WebSocket 或 SSE。

### 9.2 平台管理接口

```text
GET  /api/platform/tasks
GET  /api/platform/tasks/{taskNo}
POST /api/platform/tasks/{taskNo}/cancel
POST /api/platform/tasks/{taskNo}/retry
```

平台接口用于运营和客服排查，必须接入后台权限控制。平台端可以看到更多错误信息、worker 信息、重试次数和任务日志。

## 10. 测试与验收

### 10.1 单元测试

- 状态机只允许合法流转。
- handler registry 只允许白名单 `task_type`。
- 未知 `task_type` 失败且不自动重试。
- 任务上下文快照不保存 token、cookie、密码。
- `CANCEL_REQUESTED` 任务不会被标记为成功。

### 10.2 集成测试

- 创建任务后，worker 扫描并抢占成功。
- 多 worker 同时扫描时，同一任务只执行一次。
- handler 执行成功后任务变为 `SUCCEEDED` 并生成 artifact。
- handler 抛异常后任务变为 `FAILED` 或进入重试。
- `RUNNING` 超时任务能按规则重新入队或失败。
- artifact 只能由任务拥有者或有权限的后台用户访问。

### 10.3 验收场景

- 创建下载任务，完成后能拿到可下载文件。
- 创建图片任务，完成后能拿到多张图片 artifact。
- 创建视频任务，能看到阶段化进度和最终视频 artifact。
- 取消排队中的任务后，worker 不再执行它。
- 取消运行中的任务后，worker 在安全点停止并标记 `CANCELED`。
- 用户权限变更后，worker 执行前能阻止越权任务。

## 11. 实施顺序建议

首版实施顺序：

1. 新增 `business/domains/task/backend` 模块和 Flyway migration。
2. 实现 `async_task`、`async_task_artifact`、`async_task_log` 实体和 repository。
3. 实现任务创建、查询、取消、重试 API。
4. 实现状态机服务和任务日志服务。
5. 实现数据库扫描 worker 和超时维护 worker。
6. 实现 `TaskHandler`、`TaskDispatcher`、handler registry。
7. 接入一个最小 handler，例如 `download_export` 或 mock `image_generation`。
8. 接入文件域 artifact 访问。
9. 补充客户端任务中心列表和详情轮询。
10. 再逐步接入图片生成、视频生成等真实 handler。

首版不做：

- Redis Stream / RabbitMQ / Kafka。
- WebSocket / SSE 实时推送。
- 复杂父子任务 UI。
- 跨语言 LangGraph worker 编排。
- 任务 DSL 或数据库动态脚本执行。

这些能力应在普通任务闭环稳定后再扩展。
