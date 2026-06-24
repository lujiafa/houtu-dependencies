<div align="center">

# Houtu (后土)

**现代化、轻量级的企业级 Java 基础框架**

[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JDK](https://img.shields.io/badge/JDK-1.8+-green.svg)](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lujiafa/houtu-dependencies.svg)](https://central.sonatype.com/artifact/io.github.lujiafa/houtu-dependencies)

*取名自中国传统文化"黄天后土"中的大地之神 —— 象征稳固承载、滋养万物的力量*

[快速开始](#-快速开始) | [模块总览](#-模块总览) | [配置参考](#-配置参考) | [版本兼容](#-版本兼容) | [参与贡献](#-参与贡献)

[English](./README.md) | 中文

</div>

---

## 简介

在企业级 Java 项目中，团队往往需要在 Spring Boot 基础上集成大量组件框架，面临兼容性差异、学习成本高、多团队协作标准不一等挑战，直接影响系统的稳定性、性能与安全性。

**Houtu** 正是为此而生 —— 它封装了企业级开发中最常见的基础设施关注点，让开发者专注于业务逻辑，而将安全、会话、缓存、日志、加密、限流、灰度路由等横切能力交由框架统一管理。

### 核心特性

- **开箱即用** — Spring Boot Starter 风格，引入依赖即自动装配，零配置启动
- **按需取用** — 模块间低耦合，所有扩展依赖均为 `optional`，只用你需要的
- **注解驱动** — `@Lock`、`@CheckSession`、`@SecurityWatch`、`@AccessLog` 等声明式 API，一行注解解决横切关注点
- **安全内建** — 会话管理（JWT/Redis）、请求签名、防重放、数据加解密、RBAC 权限控制
- **微服务就绪** — 深度集成 Spring Cloud 生态：全链路灰度、权重路由、服务发现增强、熔断限流

---

## 架构总览

```
┌─────────────────────────── 业务应用层 ───────────────────────────┐
│                         你的业务代码                              │
└───────────────────────────────┬──────────────────────────────────┘
                                │
┌───────────────────────────────┼── Houtu 框架层 ─────────────────────────────┐
│                               │                                             │
│  ┌─── Web & 安全 ────┐  ┌─── 数据 & 缓存 ────┐  ┌─── 可观测性 ─────────┐  │
│  │ houtu-web         │  │ houtu-cache        │  │ houtu-access-log     │  │
│  │ houtu-web-security│  │ houtu-data-security│  │ houtu-actuator       │  │
│  │ houtu-web-swagger │  │ houtu-id           │  └──────────────────────┘  │
│  └───────────────────┘  └────────────────────┘                             │
│                                                                             │
│  ┌─── Spring Cloud 增强 ─────────────────────────────────────────────────┐  │
│  │ spring-cloud-houtu-loadbalancer  (灰度/权重/自动降级)                  │  │
│  │ spring-cloud-houtu-feign         (@AutoFeign 自动发布)                 │  │
│  │ spring-cloud-houtu-discovery     (服务状态自检/健康增强)               │  │
│  │ spring-cloud-houtu-alibaba-sentinel  (熔断限流/Nacos 规则持久化)      │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌─── 基础层 ────────────────────────────────────────────────────────────┐  │
│  │ houtu-core (上下文/异常/i18n/线程传播)    houtu-utils (加密/HTTP/工具) │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 快速开始

### 1. 引入 BOM

在项目 `pom.xml` 中通过 `dependencyManagement` 统一管理版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.lujiafa</groupId>
            <artifactId>houtu-dependencies</artifactId>
            <version>2.7.5</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 按需引入模块

```xml
<!-- Web 增强：统一参数解析、响应封装、异常处理 -->
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>houtu-web</artifactId>
</dependency>

<!-- 分布式锁 & 缓存增强 -->
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>houtu-cache</artifactId>
</dependency>

<!-- 敏感数据自动加解密 -->
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>houtu-data-security</artifactId>
</dependency>
```

### 3. 使用示例

**分布式锁** — 一个注解搞定并发控制：
```java
// key 支持：空值（自动 class.method）、参数名（"orderId"）、SpEL（"#orderId"、"#order.id"）
@Lock(prefix = "order", key = "#orderId", waitTime = 3, leaseTime = 10)
public void processOrder(String orderId) {
    // 业务逻辑，框架自动加锁/释放
}
```

**敏感数据加解密** — DAO 层透明加密：
```java
@SecurityWatch
public interface UserMapper {
    int insert(@SecurityParam User user); // 插入自动加密
    User selectById(Long id);             // 查询自动解密
}
```

**会话鉴权 & 权限控制**：
```java
@CheckSession
@RequiresPermission("user:manage")
@PostMapping("/user/update")
public ResponseData<Void> updateUser(UserForm form) {
    // 框架自动校验会话和权限
}
```

**访问日志** — 注解式或全量拦截：
```java
@AccessLog
@PostMapping("/api/order")
public ResponseData<Order> createOrder(OrderForm form) {
    // 自动记录: POST|/api/order|192.168.1.1|Mozilla/5.0...|null|-|ResponseData XxController.createOrder(OrderForm)|{...}|{...}||56
}
```

---

## 模块总览

### 基础模块

| 模块 | 说明 |
|------|------|
| **houtu-core** | 核心基础 — 应用上下文、配置解密、异常体系、i18n、跨线程上下文传播 |
| **houtu-utils** | 工具集 — 加解密（AES/RSA/SM2/SM3/SM4/ECDSA 等）、HTTP 客户端、编解码、通用工具 |

### Web & 安全

| 模块 | 说明 |
|------|------|
| **houtu-web** | Web 增强 — 自动参数映射（`BaseForm`/`BaseDTO`/`HashMap`）、统一响应（`ResponseData`）、统一异常处理 |
| **houtu-web-security** | 业务安全 — 会话管理（JWT/Redis+L2缓存）、请求鉴权、参数签名、防重放、RBAC 角色权限 |
| **houtu-web-swagger** | 文档增强 — 基于 SpringDoc OpenAPI，在 dev/test 环境自动配置默认 OpenAPI 文档及 Swagger UI |

### 数据 & 缓存

| 模块 | 说明 |
|------|------|
| **houtu-cache** | 缓存增强 — 多 RedisTemplate 实例化、Redisson/Jedis/Lettuce 扩展、`@Lock` 分布式锁、限流 |
| **houtu-data-security** | 数据安全 — `@SecurityWatch` + `@SecurityParam` 持久化层自动加解密（默认 SM4），适用于身份证、手机号等敏感数据 |
| **houtu-id** | 分布式 ID — Snowflake 与 SnowflakeX（秒级时间戳 + 可选 custom 段）生成器；基于 Redis/DB 的 `WorkerIdProvider`，租约+心跳（300s 失效/30s 心跳）；通过 `houtu.id.work-id.type` Spring Boot 自动装配 |

### 可观测性

| 模块 | 说明 |
|------|------|
| **houtu-access-log** | 访问日志 — 注解式/全量拦截，格式：`method\|path\|ip\|ua\|query\|body\|handler\|args\|response\|exception\|耗时` |
| **houtu-actuator** | 监控指标 — Web/DataSource/Redis 指标采集，集成 Micrometer Prometheus + SkyWalking APM |

### Spring Cloud 增强

> 详细文档请参阅 [spring-cloud-houtu/README-CN.md](./spring-cloud-houtu/README-CN.md)

| 模块 | 说明 |
|------|------|
| **spring-cloud-houtu-loadbalancer** | 智能路由 — 全链路灰度（hint）、权重路由、实例异常自动降级飘移 |
| **spring-cloud-houtu-feign** | Feign 增强 — `@AutoFeign` 接口自动发布到 HandlerMapping |
| **spring-cloud-houtu-discovery** | 发现增强 — 服务在线状态自检 `ServiceContext`（适用于任务/MQ 场景）、健康检测增强 |
| **spring-cloud-houtu-alibaba-sentinel** | 熔断限流 — Alibaba Sentinel 集成，支持 Nacos 规则持久化 |

---

## 注解速查

| 注解 | 所属模块 | 用途 |
|------|---------|------|
| `@Lock` | houtu-cache | 分布式锁；key 支持：空值（自动 `class.method`）、参数名（`"orderId"`）、SpEL（`"#orderId"`）；可配置 waitTime/leaseTime |
| `@CheckSession` | houtu-web-security | 校验用户会话有效性 |
| `@CheckSign` | houtu-web-security | 请求参数签名验证 |
| `@CheckRepeatRequest` | houtu-web-security | 防重放攻击 |
| `@RequiresRole` | houtu-web-security | 角色访问控制 |
| `@RequiresPermission` | houtu-web-security | 权限访问控制 |
| `@AccessLog` | houtu-access-log | 启用访问日志记录 |
| `@SecurityWatch` | houtu-data-security | 启用数据自动加解密 |
| `@SecurityParam` | houtu-data-security | 标记需加解密的参数/字段 |
| `@AutoFeign` | spring-cloud-houtu-feign | Feign 接口自动发布为 HTTP 端点 |

---

## 配置参考

<details>
<summary><b>houtu-core</b> — 核心配置</summary>

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `houtu.core.decrypt.encrypt-keys` | 已加密的配置项列表，启动时自动解密 | - |
| `houtu.core.decrypt.decrypt-processor-class` | 自定义解密处理器类 | - |

</details>

<details>
<summary><b>houtu-utils</b> — HTTP 客户端配置</summary>

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `houtu.util.httpclient.pool.max-total` | 连接池最大连接数 | 200 |
| `houtu.util.httpclient.pool.max-per-route` | 每路由最大连接数 | 50 |
| `houtu.util.httpclient.request.connect-timeout` | 连接超时（秒） | 5 |
| `houtu.util.httpclient.request.response-timeout` | 响应超时（秒） | 15 |
| `houtu.util.httpclient.proxy.hostname` | 代理主机名 | - |
| `houtu.util.httpclient.proxy.port` | 代理端口 | - |

</details>

<details>
<summary><b>houtu-web</b> — Web 增强配置</summary>

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `houtu.web.exception-resolver` | 是否启用统一异常解析器 | true |
| `houtu.web.exception-fallback` | 是否对未知异常启用统一兜底（上游服务可关闭以便链路追踪发现） | true |
| `houtu.web.combine-form-resolver-type` | 复合参数解析方式（JSON / NATIVE） | JSON |

</details>

<details>
<summary><b>houtu-web-security</b> — 安全配置</summary>

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `houtu.web.session.session-id-name` | 请求头中 Session ID 的键名 | `sid` |
| `houtu.web.session.expire` | Session 有效期 | 1800s |
| `houtu.web.session.delay` | 请求触发自动延期 | true |
| `houtu.web.session.type` | 会话类型：`CACHE` 或 `JWT` | CACHE |
| `houtu.web.session.login-url` | 会话失效重定向 URL | - |
| `houtu.web.session.redis-base-key` | Redis Key 前缀（CACHE 模式） | `security:session:` |
| `houtu.web.session.efficient-cache-name` | L2 缓存名称 | `session` |
| `houtu.web.session.efficient-cache-sync-channel` | L2 缓存同步频道 | `session-sync` |
| `houtu.web.session.redis.*` | 会话专用 Redis 配置（可缺省用默认） | - |
| `houtu.web.session.jwt-signature-key` | JWT 签名密钥（JWT 模式必填） | - |
| `houtu.web.session.jwt-signature-verify-key` | JWT 验签密钥 | - |
| `houtu.web.session.jwt-signature-algorithm` | JWT 签名算法 | HS256 |
| `houtu.web.security.enabled` | 是否启用安全模块 | true |
| `houtu.web.sign.sign-key` | HMacMD5 验签密钥（**必填**） | - |
| `houtu.web.sign.sign-name` | 签名字段名（请求头/请求参数中的键名） | `sign` |
| `houtu.web.sign.source` | 签名取值来源：`HEADER`/`BODY`/`BOTH`（BOTH 先取请求头再取请求参数） | BOTH |
| `houtu.web.sign.additional-params` | 附加必填参数，按 `source` 取值并参与签名计算；空列表表示无附加必填参数 | `nonce,timestamp` |
| `houtu.web.repeat.expire` | 防重放时间窗口 | 900s |
| `houtu.web.repeat.fields` | 参与防重放 key 的字段（取值拼接为缓存 key）；空列表表示关闭防重放 | `nonce,timestamp,sign` |
| `houtu.web.repeat.source` | 防重放字段取值来源（语义同 `houtu.web.sign.source`） | BOTH |

</details>

<details>
<summary><b>houtu-data-security</b> — 数据安全配置</summary>

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `houtu.data.security.secret-key` | SM4 对称加密 Base64 密钥（**必填**） | - |

</details>

<details>
<summary><b>houtu-id</b> — 分布式 ID 配置</summary>

| 配置项 | 说明 | 默认值   |
|--------|------|-------|
| `houtu.id.work-id.type` | WorkerIdProvider 后端：`redis` 或 `db`；不配置则禁用自动装配 | redis |
| `houtu.id.work-id.worker-bits` | workerId 位宽，槽位数 = `2^workerBits` | 5     |

> Identity（心跳所有权标识）自动解析：IP 取首个非 loopback 的 IPv4 网卡地址，端口取 `server.port`；任一缺失则 identity 退化为 UUID。
>
> **ID 生成器**（纯 Java，均复用上述 `WorkerIdProvider` 分配 workerId）：
> - **Snowflake**（`io.github.lujiafa.houtu.id.snowflake`）：经典布局 `1 符号 + 41 时间戳(ms) + 10 机器位 + 12 序列`，低 22 位可调。
> - **SnowflakeX**（`io.github.lujiafa.houtu.id.snowflakex`）：扩展版，布局 `1 符号 + 31 时间戳(秒) + machine + custom + sequence`；默认 `machine=10、custom=0、seq=22`（占满低 32 位，单节点约 2²² ≈ 419 万/秒，寿命约 68 年）。支持每次生成时传入 **`custom` 段**（`snowflakeX.next(custom)`，默认关闭，`customBits(N)` 开启，三段之和 ≤ 32），可把业务标识（如商户号用于分库分表）直接埋入 ID；毫秒精度处理时钟回拨。
>
> 两者均由代码构造（`SnowflakeXOptions` / `SnowflakeOptions`，非 Spring 自动装配），反解分别用 `SnowflakeXIds` / `SnowflakeIds`。

</details>

<details>
<summary><b>spring-cloud-houtu-loadbalancer</b> — 负载均衡配置</summary>

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.cloud.loadbalancer.weight` | 启用权重路由 | true |
| `spring.cloud.loadbalancer.hint` | 启用 hint 灰度策略 | true |
| `spring.cloud.loadbalancer.disable-gateway-request-hint` | 禁用 Gateway 请求头中的 hint | false |

</details>

---

## 版本兼容

项目主版本号和次版本号与 Spring Boot 对应，便于快速定位兼容版本。

|   Houtu   | JDK | Spring Boot | Spring Cloud | Spring Cloud Alibaba |
|:---------:|:---:|:-----------:|:------------:|:--------------------:|
| **2.7.5** | 1.8 |   2.7.18    | 2021.0.9 | 2021.0.6.2 |
|   2.7.4   | 1.8 |   2.7.18    | 2021.0.9 | 2021.0.6.2 |
|   2.7.3   | 1.8 |   2.7.18    | 2021.0.9 | 2021.0.6.2 |
|   2.7.2   | 1.8 |   2.7.18    | 2021.0.9 | 2021.0.6.2 |
|   2.7.1   | 1.8 |   2.7.18    | 2021.0.9 | 2021.0.6.2 |
|   2.7.0   | 1.8 |   2.7.18    | 2021.0.9 | 2021.0.6.2 |

---

## 主要依赖版本

| 依赖 | 版本 |
|------|------|
| Redisson | 3.51.0 |
| Cache2k | 2.6.1.Final |
| JJWT | 0.12.7 |
| Bouncy Castle | 1.82 |
| SpringDoc OpenAPI | 1.8.0 |
| Apache SkyWalking | 9.5.0 |

---

## 参与贡献

欢迎各种形式的贡献：

- **报告问题** — 使用 [Issues](https://github.com/lujiafa/houtu-dependencies/issues) 提交 Bug 或功能建议
- **提交代码** — Fork 仓库 → 创建功能分支 → 提交 Pull Request
- **完善文档** — 修正错误、补充示例、改进说明
- **测试反馈** — 在不同环境下测试并反馈兼容性

---

## 许可证

MIT License
