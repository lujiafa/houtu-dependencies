<div align="center">

# Houtu (后土)

**A Modern, Lightweight Enterprise Java Foundation Framework**

[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JDK](https://img.shields.io/badge/JDK-17+-green.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lujiafa/houtu-dependencies.svg)](https://central.sonatype.com/artifact/io.github.lujiafa/houtu-dependencies)

*Named after "Houtu" (后土), the Earth Goddess in Chinese mythology — symbolizing stability, sustenance, and the power to nurture all things*

[Quick Start](#-quick-start) | [Module Overview](#-module-overview) | [Configuration Reference](#-configuration-reference) | [Version Compatibility](#-version-compatibility) | [Contributing](#-contributing)

English | [中文](./README-CN.md)

</div>

---

## Introduction

In enterprise Java projects, teams often need to integrate numerous component frameworks on top of Spring Boot, facing challenges such as compatibility differences, high learning curves, and inconsistent standards across teams — directly impacting system stability, performance, and security.

**Houtu** was built to solve exactly these problems. It encapsulates the most common infrastructure concerns in enterprise development, allowing developers to focus on business logic while delegating cross-cutting concerns — security, session management, caching, logging, encryption, rate limiting, and canary routing — to the framework.

### Core Features

- **Zero-config startup** — Spring Boot Starter style; auto-configured on dependency import, no manual setup required
- **On-demand modules** — Loosely coupled modules with all extension dependencies marked as `optional`; only include what you need
- **Annotation-driven** — Declarative APIs like `@Lock`, `@CheckSession`, `@SecurityWatch`, `@AccessLog` handle cross-cutting concerns with a single annotation
- **Security built-in** — Session management (JWT/Redis), request signing, replay protection, data encryption/decryption, RBAC access control
- **Microservice-ready** — Deep integration with the Spring Cloud ecosystem: full-link canary routing, weighted load balancing, enhanced service discovery, circuit breaking and rate limiting

---

## Architecture Overview

```
┌─────────────────────────── Business Application Layer ──────────────────────────┐
│                              Your Business Code                                  │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼── Houtu Framework Layer ────────────────────────────────┐
│                                   │                                                         │
│  ┌─── Web & Security ──────┐  ┌─── Data & Cache ────────┐  ┌─── Observability ──────────┐  │
│  │ houtu-web               │  │ houtu-cache             │  │ houtu-access-log           │  │
│  │ houtu-web-security      │  │ houtu-data-security     │  │ houtu-actuator             │  │
│  │ houtu-web-swagger       │  │ houtu-id                │  └────────────────────────────┘  │
│  └─────────────────────────┘  └─────────────────────────┘                                  │
│                                                                                             │
│  ┌─── Spring Cloud Enhancements ───────────────────────────────────────────────────────┐   │
│  │ spring-cloud-houtu-loadbalancer  (canary/weighted/auto-failover)                    │   │
│  │ spring-cloud-houtu-feign         (@AutoFeign auto-publish / exception propagation)  │   │
│  │ spring-cloud-houtu-discovery     (service health self-check / discovery enhancement)│   │
│  │ spring-cloud-houtu-sentinel      (circuit breaking / Nacos rule persistence)        │   │
│  └─────────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                             │
│  ┌─── Foundation Layer ────────────────────────────────────────────────────────────────┐   │
│  │ houtu-core (context/exceptions/i18n/thread propagation)  houtu-utils (crypto/HTTP)  │   │
│  └─────────────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### 1. Import the BOM

Manage all versions uniformly via `dependencyManagement` in your project's `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.lujiafa</groupId>
            <artifactId>houtu-dependencies</artifactId>
            <version>3.5.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. Add Modules As Needed

```xml
<!-- Web enhancements: unified parameter resolution, response wrapping, exception handling -->
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>houtu-web</artifactId>
</dependency>

<!-- Distributed lock & cache enhancements -->
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>houtu-cache</artifactId>
</dependency>

<!-- Transparent encryption/decryption for sensitive data -->
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>houtu-data-security</artifactId>
</dependency>
```

### 3. Usage Examples

**Distributed Lock** — concurrency control with a single annotation:
```java
// key supports: empty (auto class.method), param name ("orderId"), SpEL ("#orderId", "#order.id")
@Lock(prefix = "order", key = "#orderId", waitTime = 3, leaseTime = 10)
public void processOrder(String orderId) {
    // Business logic; the framework handles locking and releasing automatically
}
```

**Sensitive Data Encryption** — transparent encryption at the DAO layer:
```java
@SecurityWatch
public interface UserMapper {
    int insert(@SecurityParam User user); // Auto-encrypted on insert
    User selectById(Long id);             // Auto-decrypted on query
}
```

**Session Authentication & Access Control**:
```java
@CheckSession
@RequiresPermission("user:manage")
@PostMapping("/user/update")
public ResponseData<Void> updateUser(UserForm form) {
    // The framework automatically validates the session and permissions
}
```

**Access Logging** — annotation-based or full interception:
```java
@AccessLog
@PostMapping("/api/order")
public ResponseData<Order> createOrder(OrderForm form) {
    // Auto-recorded: POST|/api/order|192.168.1.1|Mozilla/5.0...|null|-|ResponseData XxController.createOrder(OrderForm)|{...}|{...}||56
}
```

---

## Module Overview

### Foundation Modules

| Module | Description |
|--------|-------------|
| **houtu-core** | Core foundation — application context, config decryption, exception hierarchy, i18n, cross-thread context propagation |
| **houtu-utils** | Utilities — cryptography (AES/RSA/SM2/SM3/SM4/ECDSA, etc.), HTTP client, encoding/decoding, general utilities |

### Web & Security

| Module | Description |
|--------|-------------|
| **houtu-web** | Web enhancements — auto parameter mapping (`BaseForm`/`BaseDTO`/`HashMap`), unified response (`ResponseData`), unified exception handling |
| **houtu-web-security** | Business security — session management (JWT/Redis + L2 cache), request authentication, parameter signing, replay protection, RBAC role permissions |
| **houtu-web-swagger** | API docs enhancement — based on SpringDoc OpenAPI, provides a default OpenAPI configuration and Swagger UI for dev/test profiles |

### Data & Cache

| Module | Description |
|--------|-------------|
| **houtu-cache** | Cache enhancements — multiple RedisTemplate instances, Redisson/Jedis/Lettuce extensions, `@Lock` distributed lock, rate limiting |
| **houtu-data-security** | Data security — `@SecurityWatch` + `@SecurityParam` auto encryption/decryption at the persistence layer (default SM4), for sensitive data like ID numbers and phone numbers |
| **houtu-id** | Distributed ID — Snowflake & SnowflakeX (seconds-based timestamp + optional custom segment) generators; Redis/DB-backed `WorkerIdProvider` with lease + heartbeat (300s TTL / 30s heartbeat); Spring Boot AutoConfiguration via `houtu.id.work-id.type` |

### Observability

| Module | Description |
|--------|-------------|
| **houtu-access-log** | Access logging — annotation-based or full interception; format: `method\|path\|ip\|ua\|query\|body\|handler\|args\|response\|exception\|duration` |
| **houtu-actuator** | Monitoring metrics — Web/DataSource/Redis metric collection, integrates Micrometer Prometheus + SkyWalking APM |

### Spring Cloud Enhancements

> For detailed documentation, see [spring-cloud-houtu/README.md](./spring-cloud-houtu/README.md)

| Module | Description |
|--------|-------------|
| **spring-cloud-houtu-loadbalancer** | Smart routing — full-link canary (hint), weighted routing, automatic failover on instance errors |
| **spring-cloud-houtu-feign** | Feign enhancements — `@AutoFeign` auto-publishes interfaces to HandlerMapping, upstream exception propagation |
| **spring-cloud-houtu-discovery** | Discovery enhancements — service online status self-check `ServiceContext` (for batch jobs/MQ scenarios), enhanced health detection |
| **spring-cloud-houtu-sentinel** | Circuit breaking & rate limiting — Alibaba Sentinel integration with Nacos rule persistence |

---

## Annotation Quick Reference

| Annotation | Module | Purpose |
|------------|--------|---------|
| `@Lock` | houtu-cache | Distributed lock; key supports: empty (auto `class.method`), param name (`"orderId"`), SpEL (`"#orderId"`, `"#order.id"`); configurable waitTime/leaseTime |
| `@CheckSession` | houtu-web-security | Validates user session validity |
| `@CheckSign` | houtu-web-security | Request parameter signature verification |
| `@CheckRepeatRequest` | houtu-web-security | Replay attack protection |
| `@RequiresRole` | houtu-web-security | Role-based access control |
| `@RequiresPermission` | houtu-web-security | Permission-based access control |
| `@AccessLog` | houtu-access-log | Enables access log recording |
| `@SecurityWatch` | houtu-data-security | Enables automatic data encryption/decryption |
| `@SecurityParam` | houtu-data-security | Marks parameters/fields for encryption/decryption |
| `@AutoFeign` | spring-cloud-houtu-feign | Auto-publishes a Feign interface as an HTTP endpoint |

---

## Configuration Reference

<details>
<summary><b>houtu-core</b> — Core Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `houtu.core.decrypt.encrypt-keys` | List of encrypted config keys to auto-decrypt at startup | - |
| `houtu.core.decrypt.decrypt-processor-class` | Custom decryption processor class | - |

</details>

<details>
<summary><b>houtu-utils</b> — HTTP Client Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `houtu.client.httpclient.pool.max-total` | Maximum total connections in pool | 2000 |
| `houtu.client.httpclient.pool.max-per-route` | Maximum connections per route | 500 |
| `houtu.client.httpclient.pool.disable-ssl-validation` | Trust all certificates & skip hostname verification (test/intranet only) | false |
| `houtu.client.httpclient.pool.pool-reuse-policy` | Connection reuse policy (LIFO / FIFO) | - |
| `houtu.client.httpclient.pool.pool-concurrency-policy` | Pool concurrency policy (STRICT / LAX) | - |
| `houtu.client.httpclient.pool.time-to-live` | Max time-to-live of a pooled connection (Duration) | - |
| `houtu.client.httpclient.pool.validate-after-inactivity` | Re-validate connection after inactivity (Duration) | - |
| `houtu.client.httpclient.pool.evict-expired-connections` | Evict expired connections | true |
| `houtu.client.httpclient.pool.evict-idle-time` | Evict connections idle longer than this (Duration) | - |
| `houtu.client.httpclient.request.connect-timeout` | Connection timeout (Duration); falls back to `spring.http.client.connect-timeout` | - |
| `houtu.client.httpclient.request.read-timeout` | Socket read timeout (Duration); falls back to `spring.http.client.read-timeout` | - |
| `houtu.client.httpclient.request.connection-keep-alive` | Connection keep-alive (Duration) | - |
| `houtu.client.httpclient.request.user-agent` | Default User-Agent header | - |
| `houtu.client.httpclient.request.redirects-enabled` | Follow redirects; falls back to `spring.http.client.redirects` | - |
| `houtu.client.httpclient.proxy.hostname` | Proxy hostname | - |
| `houtu.client.httpclient.proxy.port` | Proxy port | - |
| `houtu.client.httpclient.disable-cookie-management` | Disable cookie management | true |

> **Note:** `request.connect-timeout` / `request.read-timeout` / `request.redirects-enabled` have no built-in default. When unset they fall back to `spring.http.client.*`, then to the underlying HttpClient defaults (connect ≈ 3 min, read = unlimited). Configure them (here or under `spring.http.client.*`) in production to avoid hanging requests.

</details>

<details>
<summary><b>houtu-web</b> — Web Enhancement Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `houtu.web.exception-resolver` | Whether to enable the unified exception resolver | true |
| `houtu.web.exception-fallback` | Whether to wrap unknown exceptions into a unified fallback response (disable on upstream services so they propagate and are surfaced by tracing) | true |
| `houtu.web.combine-form-resolver-type` | Combined parameter resolution mode (JSON / NATIVE) | JSON |

</details>

<details>
<summary><b>houtu-web-security</b> — Security Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `houtu.web.session.session-id-name` | Request header key name for Session ID | `sid` |
| `houtu.web.session.expire` | Session expiration duration | 1800s |
| `houtu.web.session.delay` | Auto-extend session on each request | true |
| `houtu.web.session.type` | Session type: `CACHE` or `JWT` | CACHE |
| `houtu.web.session.login-url` | Redirect URL on session expiration | - |
| `houtu.web.session.redis-base-key` | Redis key prefix (CACHE mode) | `security:session:` |
| `houtu.web.session.efficient-cache-name` | L2 cache name | `session` |
| `houtu.web.session.efficient-cache-sync-channel` | L2 cache sync channel | `session-sync` |
| `houtu.web.session.redis.*` | Dedicated Redis config for sessions (falls back to default if omitted) | - |
| `houtu.web.session.jwt-signature-key` | JWT signing key (required in JWT mode) | - |
| `houtu.web.session.jwt-signature-verify-key` | JWT verification key | - |
| `houtu.web.session.jwt-signature-algorithm` | JWT signing algorithm | HS256 |
| `houtu.web.security.enabled` | Whether to enable the security module | true |
| `houtu.web.sign.sign-key` | HMacMD5 signature key (**required**) | - |

</details>

<details>
<summary><b>houtu-data-security</b> — Data Security Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `houtu.data.security.secret-key` | SM4 symmetric encryption Base64 key (**required**) | - |

</details>

<details>
<summary><b>houtu-id</b> — Distributed ID Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `houtu.id.work-id.type` | WorkerIdProvider backend: `redis` or `db`; leave unset to disable AutoConfiguration | redis   |
| `houtu.id.work-id.worker-bits` | Worker id bit width; pool size = `2^workerBits` | 5       |

> Identity (used for heartbeat ownership) is auto-resolved: IP from the first non-loopback IPv4 NIC, port from `server.port`. When either is unavailable the identity falls back to a UUID.
>
> **ID generators** (pure Java, both reuse the `WorkerIdProvider` above for workerId allocation):
> - **Snowflake** (`io.github.lujiafa.houtu.id.snowflake`): classic layout `1 sign + 41 timestamp(ms) + 10 machine + 12 sequence`, lower 22 bits configurable.
> - **SnowflakeX** (`io.github.lujiafa.houtu.id.snowflakex`): extended variant, layout `1 sign + 31 timestamp(seconds) + machine + custom + sequence`; defaults `machine=10, custom=0, seq=22` (fills the low 32 bits, ~2²² ≈ 4.19M IDs/s per node, ~68-year lifespan). Adds a per-call **`custom` segment** (`snowflakeX.next(custom)`, off by default, enable via `customBits(N)`, with `machine+custom+seq ≤ 32`) to embed a business key (e.g. merchant id for sharding) directly into the ID; clock rollback handled at millisecond precision.
>
> Both are constructed in code (`SnowflakeXOptions` / `SnowflakeOptions`, not Spring-autoconfigured); decode via `SnowflakeXIds` / `SnowflakeIds` respectively.

</details>

<details>
<summary><b>spring-cloud-houtu-loadbalancer</b> — Load Balancer Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `spring.cloud.loadbalancer.weight` | Enable weighted routing | true |
| `spring.cloud.loadbalancer.hint` | Enable hint-based canary strategy | true |
| `spring.cloud.loadbalancer.disable-gateway-request-hint` | Disable hint from Gateway request headers | false |

</details>

<details>
<summary><b>spring-cloud-houtu-feign</b> — Feign Configuration</summary>

| Property | Description | Default |
|----------|-------------|---------|
| `houtu.feign.exception-source-trace` | Whether to write the source service name into the exception response header; a placeholder is used when disabled | false |

</details>

---

## Version Compatibility

The major and minor version numbers of the project align with Spring Boot, making it easy to locate compatible versions.

|   Houtu   | JDK | Spring Boot | Spring Cloud | Spring Cloud Alibaba |
|:---------:|:---:|:-----------:|:------------:|:--------------------:|
| **3.5.3** | 17 |   3.5.14    | 2025.0.2 | 2025.0.0.0 |
|   3.5.2   | 17 |   3.5.13    | 2025.0.2 | 2025.0.0.0 |
|   3.5.1   | 17 |   3.5.11    | 2025.0.1 | 2025.0.0.0 |
|   3.5.0   | 17 |   3.5.11    | 2025.0.0 ~ 2025.0.1 | 2023.0.1.2 ~ 2025.0.0.0 |
|   3.4.0   | 17 |   3.4.13    | 2024.0.2 | 2023.0.1.2 ~ 2023.0.3.4 |
|   3.3.0   | 17 |   3.3.13    | 2023.0.6 | 2023.0.1.2 ~ 2023.0.3.4 |
|   3.2.0   | 17 |   3.2.12    | 2023.0.6 | 2023.0.1.2 ~ 2023.0.3.4 |
|   3.1.0   | 17 |   3.1.12    | 2022.0.5 | 2022.0.0.2 |
|   2.7.x   | 8 |   2.7.18    | 2021.0.9 | 2021.0.6.2 |

> **Note:** Spring Cloud Alibaba `spring-cloud-starter-alibaba-nacos-config` has switched to the `spring.config.import` loading mechanism since version >= `2023.0.1.3`. See the [SCA official documentation](https://sca.aliyun.com/docs/2023/user-guide/nacos/quick-start) for details.

---

## Key Dependency Versions

| Dependency | Version |
|------------|---------|
| Redisson | 3.51.0 |
| Cache2k | 2.6.1.Final |
| JJWT | 0.13.0 |
| Bouncy Castle | 1.84 |
| SpringDoc OpenAPI | 2.8.9 |
| Apache SkyWalking | 9.5.0 |

---

## Contributing

All forms of contribution are welcome:

- **Report Issues** — Submit bugs or feature requests via [Issues](https://github.com/lujiafa/houtu-dependencies/issues)
- **Submit Code** — Fork the repo → create a feature branch → submit a Pull Request
- **Improve Docs** — Fix errors, add examples, or improve explanations
- **Testing Feedback** — Test in different environments and report compatibility findings

---

## License

MIT License
