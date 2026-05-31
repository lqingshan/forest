# Forest 数据库规范

本文档是 Forest 项目的数据库设计、迁移和 SQL 开发规范入口。它基于当前仓库真实实现整理，适用于 `business/domains/*/backend`、`business/aggregations/*/backend` 和 `apps/*/backend` 中涉及数据库的改动。

相关入口：

- 总开发规范：[development-guidelines.md](./development-guidelines.md)
- 架构边界：[architecture.md](../architecture/architecture.md)

## 1. 当前数据库技术栈

当前项目默认数据库栈：

- 数据库：PostgreSQL 18.3
- 后端：Spring Boot 4.0.5 + Spring Data JPA
- 迁移：Flyway
- 连接池：HikariCP

每个 app 的 `backend/src/main/resources/application.yml` 都应明确以下关键约定：

- `spring.jpa.hibernate.ddl-auto: validate`
- `spring.jpa.open-in-view: false`
- `spring.sql.init.mode: never`
- `spring.flyway.enabled: true`
- Flyway locations 按当前 app 实际装配的模块列出。

含义：

- 数据库结构必须由 Flyway migration 维护。
- JPA 只做实体与表结构校验，不负责自动建表或改表。
- 业务初始化数据不得依赖 Spring SQL init。
- 每个 app 拥有独立数据库，dev / prod 连接必须根据当前 app 的 `apps/<app>/env/*.env` 和 `SPRING_DATASOURCE_URL` 确认，不存在统一的 `forest_dev` / `forest_prod` 默认库。

### 1.1 app 数据库连接口径

Forest 当前按 app 隔离数据库。不同 app 不共享用户、账号、认证会话和业务数据表。

数据库连接判断顺序：

1. 先看当前启动的是哪个 app。
2. 再看 `apps/<app>/env/*.env` 中的 `SPRING_DATASOURCE_URL`。
3. 最后看该 app `application.yml` 中的本地兜底值。

示例：

| app | local 示例 | prod 示例 |
|---|---|---|
| `trade-leads` | `trade_leads_local` | `trade_leads_prod` |
| `cxc-commerce` | `cxc_commerce_local` | `cxc_commerce_prod` |
| `attendance` | `attendance_local` | 后续按 app 环境文件确定 |
| `ai-content-generation` | `ai_content_generation_local` | 后续按 app 环境文件确定 |

约束：

- 不要凭记忆切库。
- 不要把一个 app 的 dev/prod 数据库连接复制到另一个 app。
- 本地是否连接远程 PostgreSQL，也由当前 app 的 env 文件决定；不能写成全项目统一默认。
- 迁移、排障、压测、数据修复前，必须先确认当前 app、环境和 database name。

## 2. 模块归属与迁移目录

Forest 是 modular monolith，数据库归属要跟模块边界一致：

- `business/domains/*` 拥有本业务域的实体、表和 migration。
- `business/aggregations/*` 负责跨域查询和编排，默认不拥有下游 domain 的实体和表。
- `apps/*/backend` 是运行容器，负责装配多个 business 模块；除 app 专属历史表或专属编排表外，不应重新定义 domain 表。

migration 目录不要靠文档手写列表判断，先用命令查看当前真实目录：

```bash
find business apps -path '*/src/main/resources/db/migration*' -type d | sort
```

当前常见目录形态：

```text
business/domains/<domain>/backend/src/main/resources/db/migration/<domain>
apps/<app>/backend/src/main/resources/db/migration/<app>
```

当前已有 domain migration group 包括：

```text
access
file
lead
notification
organization
payment
point
recharge
user
```

当前已有 app legacy migration group：

```text
trade-leads
```

各 app 的 `backend/pom.xml` 会在 `process-resources` 阶段把本 app 实际依赖的 business 模块 migration 复制到应用 classpath。检查某个 app 实际会加载哪些 migration 时，以该 app 的 `backend/pom.xml` 和 `application.yml` 为准，不以本文档列表为准。

新增业务域表时，优先放在对应 `business/domains/<domain>` 下。只有 app 专属历史表、app 专属编排表或无法沉淀为通用 domain 的表，才放在 `apps/<app>/backend/src/main/resources/db/migration/<app>`。

历史说明：`recharge_order` 的初始建表 migration 当前仍在 `apps/trade-leads/backend/.../trade-leads/V20260409_2304__init_trade_leads_tables.sql`，后续充值域变更已经放到 `business/domains/recharge/.../V20260415_2302__alter_recharge_order_for_payment.sql`。新充值表结构变更继续归属 `business/domains/recharge`。

## 3. 命名规范

表名和字段名统一使用 `snake_case`。

表名规则：

- 表名默认使用单数名词，例如 `lead`、`point_log`、`user_account`、`recharge_order`。
- 如果单数名命中数据库关键字，使用可执行的单数替代名，例如 `app_user`，不要直接使用 `user`。
- 关联表使用单数桥接名，例如 `user_account`，不要使用复数桥接名。

字段规则：

- 主键统一命名为 `id`。
- 关联 ID 统一命名为 `<entity>_id`，例如 `user_id`、`lead_id`、`account_id`。
- 枚举字段使用明确业务名，例如 `status`、`direction`、`source_type`。
- Java 字段与数据库字段一一对应，例如 `modifiedTime -> modified_time`。

约束和索引命名：

- 唯一约束：`uk_<table>_<business_key>`
- 普通索引：`idx_<table>_<columns_or_purpose>`
- 名称要能表达业务语义，不要只写 `idx_1`、`idx_temp`。

命名重构必须同步修改实体、Flyway migration、约束名、SQL、测试和文档，不能只改其中一层。

## 4. 固定字段和业务时间字段

本节是建表时必须优先对齐的固定字段规范。后续新增表时，不允许只按业务字段建表后再临时补审计字段，也不允许用业务时间字段替代固定审计时间字段。

### 4.1 所有业务表固定字段

所有业务表必须固定包含以下字段：

| 字段 | DDL | 是否必需 | 说明 |
|---|---|---|---|
| `id` | `bigserial primary key` | 必需 | 表内技术主键，JPA 使用 `GenerationType.IDENTITY` |
| `created_id` | `bigint` | 必需 | 创建人 ID，当前可为空，为后台审计和后续用户上下文预留 |
| `created_time` | `timestamp not null` | 必需 | 创建时间，由实体持久化前写入 |
| `modified_id` | `bigint` | 必需 | 最后修改人 ID，当前可为空，为后台审计和后续用户上下文预留 |
| `modified_time` | `timestamp not null` | 必需 | 最后修改时间，由实体创建和更新时维护 |
| `deleted` | `integer not null default 0` | 必需 | 软删除标记，`0` 有效，`1` 已删除 |

JPA 主键对应：

```sql
id bigserial primary key
```

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

固定审计字段对应：

```sql
created_id bigint
created_time timestamp not null
modified_id bigint
modified_time timestamp not null
deleted integer not null default 0
```

```java
public class XxxPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

说明：

- `created_id`、`modified_id` 是固定字段，即使当前链路暂时写不出操作人，也要保留字段。
- `created_time`、`modified_time` 是固定字段，所有业务表都必须包含。
- `deleted` 也是固定字段，不因表当前“看起来不会删除”而省略。
- 业务表不得使用业务编号、手机号、订单号、微信 openid 等业务字段替代 `id` 作为主键。
- 业务 PO 必须继承 `com.forest.starter.jpa.ForestAuditablePO`，由全局基类统一声明和维护固定审计字段。
- `id` 不放入全局基类，必须在每个 PO 内显式声明。
- 业务模块不得再自建 `AuditPO`、`BasePO` 等审计字段基类；确需例外时，只允许非业务实体表，并必须在文档或代码注释中说明原因。

### 4.2 实体时间维护规则

所有业务实体必须通过 `ForestAuditablePO` 维护创建和修改时间：

```java
import com.forest.starter.jpa.ForestAuditablePO;

@Entity
@Table(name = "xxx")
public class XxxPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

`ForestAuditablePO` 内部使用 `ForestTime.now()`，业务实体不得重复声明固定审计字段，也不得重复实现同名 `@PrePersist`、`@PreUpdate` 生命周期方法。

数据库 `timestamp without time zone` 字段的项目语义固定为 `Asia/Shanghai` 本地时间。业务代码不得直接使用 `LocalDateTime.now()` 生成入库时间，应统一使用 `ForestTime.now()`；外部带 offset 的时间先转换到 `Asia/Shanghai` 后再落库。

### 4.3 新建表基础模板

业务表默认模板：

```sql
create table if not exists <table_name> (
    id bigserial primary key,
    -- business columns...
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null
);
```

余额、库存、计数器等并发敏感汇总表默认模板：

```sql
create table if not exists <table_name> (
    id bigserial primary key,
    -- owner and value columns...
    version integer not null default 0,
    created_id bigint,
    created_time timestamp not null,
    modified_id bigint,
    modified_time timestamp not null,
    deleted integer not null default 0,
);
```

### 4.4 业务时间字段命名原则

约束如下：

- `unlock_time`、`paid_time`、`notify_time` 等字段是业务时间字段，只能额外存在，不能替代 `created_time`、`modified_time`。
- `updated_at` 不作为统一固定字段使用；如确需保留，必须逐表说明业务含义。
- 不要在同一张表里混用多个含义重复的时间字段。
- 实体里通过 `@PrePersist`、`@PreUpdate` 维护的时间字段，DDL 必须有对应非空约束或可空语义。
- 新增字段时要同步检查前端展示、接口返回和测试数据。

## 5. 软删除规范

当前项目统一采用软删除字段：

```sql
deleted integer not null default 0
```

语义：

- `deleted = 0` 表示有效记录。
- `deleted = 1` 表示已删除。

规则：

- 新建业务表必须包含 `deleted` 字段。
- 是否过滤软删除数据，必须在 service 查询语义中显式决定。
- 流水表、关系表、业务主表都遵循同一软删除规范。
- 业务删除不使用物理删除；物理删除只允许用于测试清理、一次性初始化或明确的运维脚本。

当前例外不是“没有软删除”，而是部分表当前业务上只产生有效记录，例如 `payment_order`、`recharge_order`，它们仍保留 `deleted` 字段。

## 6. 金额、积分和数值单位

货币金额使用整数分，字段后缀统一为 `_cents`：

- `amount_cents`
- `price_cents`
- `paid_amount_cents`
- `refund_amount_cents`

硬规则：

- 数据库不使用 `decimal`、`float`、`double` 存储货币金额。
- 金额字段使用 `integer` 或 `bigint`。
- API 与数据库默认保持同一金额语义：整数分。
- 页面展示层负责把分转换为展示格式。
- 如果后续支持多币种，金额字段旁边必须增加币种字段，例如 `currency_code`。

非货币数值不要套用金额命名：

- 积分使用 `balance`、`amount`、`total_income`、`total_spend`、`credited_points`。
- 解锁积分成本使用 `point_cost`。

## 7. 枚举和字符串

枚举字段默认以字符串保存，不使用 ordinal：

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private Status status;
```

如果需要兼容历史存储值或第三方值，使用显式 converter，例如 `PaymentOrderChannelConverter` 兼容历史 `WECHAT_JSAPI` 并统一到 `WECHAT_MINIAPP_PAYMENT`。

字符串长度要在 DDL 与实体注解中保持一致。当前常见长度：

- `status`、`type`、`direction`、`source_type`：`varchar(20)` 或 `varchar(32)`
- 业务单号、支付单号：`varchar(64)`
- 外部交易或预支付标识：`varchar(64)`、`varchar(128)`
- 手机号：`varchar(30)` 或按业务表约定长度
- 邮箱：`varchar(100)` 或 `varchar(120)`
- 长文本介绍：`text`

## 8. 唯一约束与幂等键

唯一约束只服务真实业务语义，不为了“完整”预铺。

当前已有典型唯一约束：

- `uk_account_type_identifier`：账号类型 + 标识唯一。
- `uk_user_account_user_account`：用户与账号绑定关系唯一。
- `uk_user_account_account_id`：一个账号只能绑定一个用户。
- `uk_point_balance_user_id`：一个用户只有一条积分余额。
- `uk_point_log_biz_key`：积分流水幂等键唯一。
- `uk_lead_unlock_user_lead`：同一用户同一线索只能解锁一次。
- `uk_recharge_order_recharge_no`：充值业务单号唯一。
- `uk_payment_order_payment_no`：支付单号唯一。
- `uk_payment_order_out_trade_no`：传给微信支付的商户订单号唯一。

规则：

- 业务幂等必须落库为唯一约束，不能只靠代码判断。
- 一对一关系必须用唯一约束表达，例如 `point_balance.user_id`。
- 组合唯一关系直接落联合唯一约束。
- 新增唯一约束时，同步更新实体 `@Table(uniqueConstraints = ...)`。

## 9. 索引与查询路径

当前项目默认不预铺大量索引，只为明确查询路径补必要索引。

规则：

- 主键和唯一约束天然带索引，先利用已有唯一键。
- 高频筛选、排序、关联字段在查询路径稳定后再补普通索引。
- 新增索引必须对应真实查询条件和排序字段。
- 不要因为“可能会用到”提前铺一批索引。
- `lower(column)`、`%keyword%` 等表达式不会自然吃到普通 B-tree 索引，需要单独评估表达式索引或搜索方案。

当前已有普通索引和搜索索引：

- `idx_payment_order_biz_ref`：`payment_order (biz_type, biz_order_id)`
- `idx_payment_order_status`：`payment_order (status)`
- `idx_lead_search_vector_active`：`lead.search_vector` 的 GIN 部分索引
- `idx_lead_trgm_document_active`：线索名称、类目、关键词拼接文本的 trigram GIN 部分索引
- `idx_lead_country_active`：有效线索国家筛选索引

线索搜索是 PostgreSQL 原生能力：

- 使用 `pg_trgm` 扩展。
- 使用 generated `tsvector` 字段 `search_vector`。
- 对 `deleted = 0` 建部分索引。
- 原生 SQL 查询要封装在 repository 实现里，并显式识别数据库类型。

## 10. 外键与跨域关系

当前项目默认不建立物理外键约束，尤其不跨 domain 建数据库外键。

原因：

- 项目强调 domain 解耦。
- 未来可能按模块拆分服务或数据库。
- 跨域一致性通过 service 编排、唯一约束、幂等键和事务语义保证。

规则：

- 表关系通过 `<entity>_id` 字段表达。
- 不跨 domain 建数据库外键。
- 同 domain 内如果确实需要物理外键，必须在设计说明中写清楚原因。
- 不能把数据库外键当成跨模块边界的替代方案。

## 11. Flyway 迁移规范

数据库结构变更统一通过 Flyway migration 管理。

文件命名沿用当前风格：

```text
VYYYYMMDD_HHMM__description.sql
```

规则：

- 一次 migration 优先只做一类可理解变更。
- 已进入共享环境、已发布、已被任何 local/prod 数据库执行过的 migration 视为历史事实，禁止再修改内容。
- 历史 migration 即使只是改初始化数据、账号类型、注释、空格或换行，也会改变 Flyway checksum，可能导致应用启动阶段校验失败。
- 命名重构或历史数据修正必须新增更高版本 migration 处理，不能回改旧 migration 让历史 SQL “看起来更一致”。
- Flyway 启动顺序是先 validate 历史 migration checksum，再 migrate 新版本；历史 checksum 不一致时，新 migration 不会执行，应用会直接启动失败。
- 不允许把 `flyway repair` 当作常规修复手段。只有确认数据库真实结构和当前 migration 文件完全一致，且只是 `flyway_schema_history` 记录错误时，才可以单独评审后使用。
- DDL 要尽量具备清晰的重复执行边界，例如 `create table if not exists`、`add column if not exists`、`create index if not exists`。
- 数据修正 SQL 要写明范围，避免无条件大范围误改。
- 初始化数据要写成“已存在则跳过”的形式，避免重复插入。
- 生产数据库禁止手工改表，必须通过 migration 随应用发布。

当前 migration 示例：

- `V20260409_2301__init_user_tables.sql`
- `V20260409_2302__init_point_tables.sql`
- `V20260409_2303__init_lead_tables.sql`
- `V20260409_2304__init_trade_leads_tables.sql`
- `V20260415_1200__add_lead_search_indexes.sql`
- `V20260415_2301__init_payment_tables.sql`
- `V20260415_2302__alter_recharge_order_for_payment.sql`
- `V20260422_0001__rename_wechat_jsapi_channel_to_miniapp_payment.sql`

## 12. 实体与 DDL 对齐

JPA 实体必须与 migration 保持一致：

- `@Table(name = ...)` 与真实表名一致。
- `@Column(nullable = ..., length = ...)` 与 DDL 一致。
- `@Column(name = ...)` 与 `snake_case` 字段名一致。
- 唯一约束名称与 migration 中的约束名称一致。
- 枚举默认使用 `EnumType.STRING` 或显式 converter。

变更表结构时，至少同步检查：

- Flyway migration SQL
- JPA entity
- repository 查询
- service 校验与事务语义
- 测试数据和集成测试断言
- 前端类型和展示格式
- 文档和 ER 图

因为 `ddl-auto` 是 `validate`，实体和数据库不一致会在启动阶段暴露，不要依赖 Hibernate 自动修表。

## 13. SQL 与 Repository 规范

新增查询默认遵循总开发规范里的 JPA 查询选择顺序：

1. 能用派生查询 `findBy...` 清楚表达时，优先使用派生查询。
2. 派生查询表达不了，或者需要更新、聚合、固定复杂语义时，使用 `@Query`。
3. 只有多可选条件动态组合时，使用 `Specification`。
4. 只有 JPQL 不足以表达数据库能力时，才使用原生 SQL。

Repository 只暴露当前业务真正需要的方法，不预铺所有字段组合。

原生 SQL 使用要求：

- 必须说明为什么不能用 JPQL 或派生查询。
- PostgreSQL 专属能力要封装在基础能力或 repository 实现中。
- 如果测试环境可能使用 H2，需要有真实 PostgreSQL Testcontainers 测试覆盖数据库专属行为。

## 14. 并发敏感写操作

涉及余额、库存、计数器、支付状态、解锁状态等并发敏感数据时，优先让数据库兜底。

当前项目标准做法：

- `point_balance` 保留 `version` 字段。
- 积分加减使用条件更新，更新语句显式校验 `version`。
- 扣减积分时同时校验余额条件。
- `point_log.biz_key` 保证流水幂等。
- `lead_unlock_record (user_id, lead_id)` 保证重复解锁幂等。
- `payment_order.out_trade_no` 和 `payment_order.payment_no` 保证支付单幂等定位。

不要把并发控制简化成：

- 先查再改再保存。
- 只靠应用内存锁。
- 只靠接口调用顺序假设。
- 只靠前端防重复点击。

## 15. 种子数据和演示数据

种子数据分两类：

- 必需初始化数据：可以放在 migration 中，但必须可重复执行安全，例如默认平台手机号账号。
- 演示数据：放在 `deploy/sql/seed-trade-leads-demo-data.sql` 这类独立脚本中，不作为生产结构迁移的一部分。

要求：

- 敏感账号、密钥、证书不写入仓库。
- 演示数据脚本要避免覆盖生产真实数据。
- 运行演示数据前必须确认连接的是目标开发库或演示库。

## 16. 现有表归属速查

| 表 | 归属模块 | 说明 |
|---|---|---|
| `app_user` | `business/domains/user` | 用户主体，使用 `app_user` 避开 `user` 关键字风险 |
| `account` | `business/domains/user` | 登录账号，`type + identifier` 唯一 |
| `user_account` | `business/domains/user` | 用户账号绑定关系 |
| `point_balance` | `business/domains/point` | 用户积分余额，一用户一条 |
| `point_log` | `business/domains/point` | 积分流水，`biz_key` 幂等 |
| `lead` | `business/domains/lead` | 线索主数据，包含 PostgreSQL 搜索索引 |
| `lead_unlock_record` | `business/domains/lead` | 用户线索解锁记录 |
| `recharge_order` | `business/domains/recharge` | 充值业务单，初始 migration 位于 app 历史目录 |
| `payment_order` | `business/domains/payment` | 支付执行单 |

## 17. 数据库变更检查清单

改数据库前先过这张清单：

1. 表或字段归属哪个 domain？是否误放到了 app 或 aggregation？
2. 是否需要新增 Flyway migration？命名是否符合 `VYYYYMMDD_HHMM__description.sql`？
3. 是否命中了表名、字段名、固定字段、金额、时间、软删除、枚举规范？
4. 是否需要唯一约束保证业务幂等？
5. 是否需要索引？索引是否对应真实查询路径？
6. 是否涉及跨 domain 关系？是否避免了跨 domain 物理外键？
7. 实体、DDL、repository、service、测试是否同步更新？
8. 并发敏感写操作是否有数据库条件更新或唯一约束兜底？
9. 本地或 CI 是否覆盖了 H2 兼容测试，以及 PostgreSQL 专属行为测试？
10. 文档或 ER 图是否需要同步更新？
