# 文件模块设计方案

## 1. 定位

文件模块是通用基础模块，服务多个业务 app，不绑定商城、线索、订单、商家、店铺、组织或 RBAC。

一期目标：

- 支持 `cxc-commerce` 和 `trade-leads`。
- 使用阿里云 OSS 私有 bucket。
- 前端直传 OSS，后端负责上传会话、元数据、complete 校验和下载鉴权。
- 支持图片、常用文档、50MB 内小视频和音频。
- 支持多文件同时上传交互，允许局部失败和局部重试。

一期不做：

- 不实现本地文件存储。
- 不做分片上传。
- 不做视频转码。
- 不做内容安全扫描。
- 不把 Redis 放进核心上传流程。

## 2. Bucket 与 Object Key

每个业务 app 分配一个 bucket：

```text
cxc-commerce -> cxc-commerce-file
trade-leads  -> trade-leads
```

`local` 和 `prod` 共用同一个 bucket，通过 `object_key` 第一层区分环境：

```text
{env}/{fileCategory}/{yyyyMMdd}/{fileNo}.{ext}
```

示例：

```text
local/image/20260510/FILE202605100001.jpg
prod/video/20260510/FILE202605100002.mp4
prod/document/20260510/FILE202605100003.pdf
prod/audio/20260510/FILE202605100004.mp3
```

## 3. 后端模块

后端分两层：

- `base-backend/starter-object-storage`：对象存储技术能力，定义 `ObjectStorageClient`，提供阿里云 OSS 实现和测试 mock。
- `business/domains/file/backend`：文件业务域，负责元数据、上传会话、complete、下载鉴权和删除。

依赖方向：

```text
apps/*
  -> business/domains/file/backend
    -> base-backend/starter-object-storage
```

`starter-object-storage` 不依赖 `business/*` 或 `apps/*`。

## 4. 上传流程

`file_no` 在创建上传会话时生成，并立即写入数据库，不等前端 complete。

```text
创建上传会话
-> 生成 file_no
-> 生成 upload_session_no
-> 写 file_object = UPLOADING
-> 写 file_upload_session = CREATED
-> 返回 OSS 直传凭证
-> 前端直传 OSS
-> 前端调用 complete
-> 后端 HEAD OSS 校验大小和类型
-> file_object = AVAILABLE
-> file_upload_session = COMPLETED
```

complete 前的文件不能作为业务可用文件。

## 5. 数据模型

`file_object` 保存文件主元数据：

```text
file_no
business_app_code
uploaded_client_app_code
uploader_user_id
uploader_account_id
bucket
object_key
etag
original_name
content_type
file_category
size_bytes
sha256
image_width
image_height
status
created_id
created_time
modified_id
modified_time
deleted
deleted_time
```

`file_upload_session` 保存一次上传会话：

```text
upload_session_no
file_no
business_app_code
uploader_user_id
expected_content_type
expected_size_bytes
expected_file_category
expires_at
status
created_id
created_time
modified_id
modified_time
deleted
```

## 6. API

```text
POST /api/client/file/upload-session
POST /api/admin/file/upload-session
POST /api/platform/file/upload-session

POST /api/{client|admin|platform}/file/upload-session/{uploadSessionNo}/complete
POST /api/{client|admin|platform}/file/upload-session/{uploadSessionNo}/abort
GET  /api/{client|admin|platform}/file/{fileNo}
POST /api/{client|admin|platform}/file/download-url
POST /api/{client|admin|platform}/file/preview-url
GET  /api/{client|admin|platform}/file/{fileNo}/download
DELETE /api/{client|admin|platform}/file/{fileNo}
```

下载必须后端鉴权。文件模块一期默认只允许上传者本人访问，后续可通过 `FileAccessPolicy` 扩展平台、商家、组织或 RBAC 权限。

## 7. 多文件上传交互

后端仍然保持“一文件一上传会话”，前端负责批量编排。

每个文件独立状态：

```text
PENDING
UPLOADING
SUCCESS
FAILED
```

批量上传规则：

- 支持多文件选择。
- 支持并发上传。
- 每个文件独立进度。
- 单个文件失败不阻断其他文件。
- 批量结果同时返回成功文件和失败项。
- 失败项保留错误原因，并支持单文件重试。
- 重试时重新创建上传会话，重新直传 OSS，重新 complete。

小程序上传组件只提供状态机、事件和样式 hook；app 负责视觉样式。

PC/H5 侧提供 Vue 上传面板和底层上传 API：

```text
@forest/file/web/upload
  uploadWebFile
  uploadWebFiles
  FileUploadPanel
```

`FileUploadPanel` 不包含 app 品牌样式，只输出稳定 class 和上传事件，供各 app 自己控制视觉。

## 8. 前端样式边界

`business/domains/file/frontend` 是通用 domain frontend，不写死 app 品牌样式。

允许：

- 提供上传 API、类型、view-model 和组件状态。
- 提供必要结构 class。
- 提供 `externalClasses` 和 CSS 变量作为 app 接管样式的 hook。
- 保留最低限度布局，保证组件结构可用。

不允许：

- 写死 app 品牌色。
- 写死卡片阴影、页面背景、营销风格。
- 把某个 app 的按钮、间距、圆角、主题复制进通用模块。

app 使用组件时，应在各自客户端工程中决定：

- 按钮颜色和形态。
- 卡片或列表视觉。
- 错误、成功、重试状态样式。
- 上传区域在页面里的布局。

## 9. Redis

一期不依赖 Redis。

后续可能使用 Redis 的场景：

- 上传频率限制。
- 下载签名短期缓存。
- 大文件分片上传会话缓存。
- 防刷和审计辅助。

Redis KEY 必须遵守 [redis-key-guidelines.md](../../../../docs/guidelines/redis-key-guidelines.md)。

## 10. 配置

```yaml
forest:
  file:
    app-code: cxc-commerce
    env: ${FOREST_FILE_ENV:local}
    storage:
      provider: aliyun-oss
      aliyun-oss:
        endpoint: ${ALIYUN_OSS_ENDPOINT}
        access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}
        buckets:
          cxc-commerce: ${CXC_COMMERCE_OSS_BUCKET:cxc-commerce-file}
          trade-leads: ${TRADE_LEADS_OSS_BUCKET:trade-leads}
```

## 11. 验收

- `cxc-commerce` 写入 bucket `cxc-commerce-file`。
- `trade-leads` 写入 bucket `trade-leads`。
- `local` 写入 `local/` 前缀。
- `prod` 写入 `prod/` 前缀。
- 图片最大 10MB。
- 文档最大 50MB。
- 视频最大 50MB。
- 音频最大 50MB。
- 未登录不能上传和下载。
- complete 前文件不可用。
- 非上传者默认不能下载。
- 多文件上传时局部失败不影响其他文件成功。
- 失败文件可以单独重试。
