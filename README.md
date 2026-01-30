## Houtu

Houtu（后土）是一个现代化、轻量级的开源`基础框架`、`脚手架`，其名源自中国传统文化“黄天后土”中的大地之神"后土"
娘娘，象征着稳固、承载与滋养万物的力量，坚实可靠的基础支撑。

* **稳固基础**：如大地般坚实可靠，为上层应用提供稳定支撑；
* **承载万物**：具备高度扩展性和兼容性，支持多样化开发需求；
* **滋养生态**：设计理念注重开发者体验，助力项目茁壮成长；
* **包容开放**：如大地般包容万物，支持融合多技术栈和开发范式；
* **生生不息**：持续演进的生命力，跟随技术发展不断更新。

## 💡 框架背景与初衷

<p>
在当前企业级Java项目开发中，大多在SpringBoot基础上融合其他组件框架，可各种组件框架存在兼容性差异且需要一定熟悉成本。这就带来新的问题，在多人团队、多组团队等场景时，每个人对技术框架的认识和掌握存在一定差异，给业务系统带来
新的挑战，如：稳定性、性能、安全、扩展等等。
</p>
<p>
Houtu旨在打造企业级业务项目基座框架，让大部人更加专注业务开发，依赖服务和组件间的大部分性能、安全、稳定、扩展、可用性和可维护性等等诉求由`houtu`提供保障，这使得`Houtu`更像是一个业务架构级项目。
</p>

## 😊 框架版本描述

项目分支版本号中<b>主版本号(Major version)</b>和<b>次版本号(Minor version)</b>与springboot的<b>主版本号(Major
version)</b>和<b>次版本号(Minor version)</b>对应，修订版本号之间两者无必然联系。

##### 以下仅包含主要控件版本

| houtu |  jdk   | springboot |    spring cloud     |   spring cloud alibaba    |
|:-----:|:------:|:----------:|:-------------------:|:-------------------------:|
| 3.5.0 | jdk17  |   3.5.10   | 2025.0.0~`2025.1.0` | 2023.0.1.2 - `2025.0.0.0` |
| 3.4.0 | jdk17  |   3.4.11   |      2024.0.2       | 2023.0.1.2 - `2023.0.3.4` |
| 3.3.0 | jdk17  |   3.3.13   |      2023.0.6       | 2023.0.1.2 - `2023.0.3.4` |
| 3.2.0 | jdk17  |   3.2.12   |      2023.0.6       | 2023.0.1.2 - `2023.0.3.4` |
| 3.1.0 | jdk17  |   3.1.12   |      2022.0.5       |        2022.0.0.2         |
| 2.7.0 | jdk1.8 |   2.7.18   |      2021.0.9       |        2021.0.6.2         |

> ### 注
> * `Spring Cloud Alibaba`配置模块`spring-cloud-starter-alibaba-nacos-config`在版本高于或等于`2023.0.1.3`
    后，已调整加载方式为`spring.config.import`加载。参考文档如下：
> * [https://github.com/alibaba/spring-cloud-alibaba/issues/3998](https://github.com/alibaba/spring-cloud-alibaba/issues/3998)
> * [https://sca.aliyun.com/docs/2023/user-guide/nacos/quick-start](https://sca.aliyun.com/docs/2023/user-guide/nacos/quick-start)

## 🚀 模块描述

- **houtu-core**: 核心基础模块，定义基础支持类和桥接SpringBoot容器；
- **houtu-utils**: 工具模块，提供大量工具类（包括但不限于市面上主流的加解密、签名、16/10/2进制转换等等工具）；
- **houtu-cache**:
  提供Cache能力增强，包括但不限于增加Jedis/Lettuce/Redisson等Bean初始化扩展工具类（解决SpringBoot仅支持单RedisTemplate实例化问题）、注解锁
  `@com.houtu.lock.annotation.Lock`
  （解决模块锁、事务锁、...）等等；
- **houtu-access-log**: 提供访问日志增强，可以让日志分析更加轻松，同时支持`注解日志`和`全量请求拦截日志`
  ，日志格式：
  `httpMethod|path|requestIp|user-agent|queryString|[body]|methodName|arg1, arg2, ...|responseArg|exception|耗时`；
- **houtu-data-security**: 提供持久化层数据注解`@SecurityWatch`结合`@SecurityParam`与`SecurityObject`
  实现简单参数与嵌套参数自动加解密能力，适用于敏感数据（如：用户身份证、手机号码、银行卡号码等等）存储；
- **houtu-monitor**:
  可选使用，主要提供部分自定义监控支持。为什么SpringBoot提供了Actuator能力还要自主实现呢？因为Actuator在请求等多指标监控时，对实时和内存要求较高，如将监控数据日志化+MQ+Prometheus可更好更完整的监控业务数据；
- **houtu-web**:
  提供Web能力增强，包括但不限于自定义参数解析、响应解析、统一异常处理等等，如：Controller中会自动将参数封装到
  `BaseForm/BaseDTO子类`
  或`HashMap类型`参数对象中（不限请求方式），响应类型为`ResponseData/EmbedResponseData`
  时无需@ResponseBody注解但提供相同响应能力，统一异常处理会自动拦截业务异常`BusinessException`
  和其他各类异常响应`{"code":xx,"message":"xx"}`。
- **houtu-web-security**: 在`houtu-web`能力之上，提供业务安全支撑，包括但不限于会话管理、请求鉴权、参数签名、防重放请求等等能力；
- **houtu-web-swagger**: 提供Swagger文档增强，如：自动将`@ApiModel`
  注解的属性映射为Swagger文档中的参数，自动将`@ApiOperation`注解的描述映射为Swagger文档中的接口描述等等。

- **SpringCloud场景模块**
    - **spring-cloud-houtu-loadbalancer**: 增强SpringCloud环境中Gateway和普通服务负载均衡，包括但不限于全链路灰度、独立集群、权重路由和实例异常自动降级飘移等能力。
    - **spring-cloud-houtu-discovery**: 对服务发现能力进行增强，包括但不限于当前服务状态自检测`ServiceContext`
      （常用于任务处理、MQ消费等等场景，离线不处理）、和服务检测增强`/service/health`或`/actuator/health`。
    - **spring-cloud-houtu-sentinel**: 引入并持续建设增强熔断限流功能；
    - **spring-cloud-houtu-feign**: 引入并持续建设增强Feign能力，包括但不限于`@AutoFeign`服务接口自动发布、上游异常自动穿透等等能力；

## ⚙️ 配置

| 模块                              | 配置项                                                    | 配置描述                                                                                       | 默认值                 | 是否必须 |
|---------------------------------|--------------------------------------------------------|--------------------------------------------------------------------------------------------|---------------------|------|
| houtu-core                      | houtu.core.decrypt.encrypt-keys                        | 已加密的配置项，会通过`decrypt-processor-class`对应类进行解密操作                                              | -                   | 否    |
| houtu-core                      | houtu.core.decrypt.decrypt-processor-class             | 自定义配置解密处理器类（Class<? extends com.houtu.core.env.DecryptProcessor>）                          | -                   | 否    |
| houtu-utils                     | houtu.util.httpclient.pool.max-total                   | 连接池最大连接数                                                                                   | 80                  | 否    |
| houtu-utils                     | houtu.util.httpclient.pool.max-per-route               | 每个路由的默认最大连接                                                                                | 10                  | 否    |
| houtu-utils                     | houtu.util.httpclient.request.connect-timeout          | 连接超时时间（秒）                                                                                  | 5                   | 否    |
| houtu-utils                     | houtu.util.httpclient.request.response-timeout         | 响应超时时间（秒）                                                                                  | 15                  | 否    |
| houtu-utils                     | houtu.util.httpclient.proxy.hostname                   | 请求代理Hostname                                                                               | -                   | 否    |
| houtu-utils                     | houtu.util.httpclient.proxy.port                       | 请求代理端口                                                                                     | -                   | 否    |
| houtu-data-security             | houtu.data.security.secret-key                         | 默认SM4(SM4/ECB/PKCS5Padding)对称Base64密钥。                                                     | -                   | 是    |
| houtu-web                       | houtu.web.exception-resolver                           | 是否启用统一异常解析器                                                                                | true                | 否    |
| houtu-web                       | houtu.web.combine-form-resolver-type                   | 复合参数解析器对Form参数处理方式                                                                         | JSON                | 否    |
| houtu-web-security              | houtu.web.session.session-id-name                      | 定义请求头数据中session id键名                                                                       | "sid"               | 否    |
| houtu-web-security              | houtu.web.session.expire                               | session有效期                                                                                 | 1800s               | 否    |
| houtu-web-security              | houtu.web.session.delay                                | 是否启用通过请求触发session自动延期                                                                      | true                | 否    |
| houtu-web-security              | houtu.web.session.type                                 | 会话保持和持久化类型，支持JWT和CACHE                                                                     | CACHE               | 否    |
| houtu-web-security              | houtu.web.session.login-url                            | 会话失效登录URL地址(仅web场景中有用)                                                                     | -                   | 否    |
| houtu-web-security              | houtu.web.session.redis-base-key                       | 默认服务Session持久化Redis Key前缀(type=CACHE时，此值有效)                                                | "security:session:" | 否    |
| houtu-web-security              | houtu.web.session.efficient-cache-name                 | 高效二级缓存名称，支持Caffeine、Cache2k(type=CACHE且引入相关依赖时，此值有效)                                       | "session"           | 否    |
| houtu-web-security              | houtu.web.session.efficient-cache-sync-channel         | 高效二级缓存集群同步发布订阅频道名称(type=CACHE且引入相关依赖时，此值有效)                                                | "session-sync"      | 否    |
| houtu-web-security              | houtu.web.session.redis.*                              | 会话Redis配置，同SpringBoot引入的Redis配置，可缺省使用默认Redis                                               | -                   | 否    |
| houtu-web-security              | houtu.web.session.jwt-signature-key                    | JWT签名Base64密钥(type=JWT时，此值有效)                                                              | -                   | 否    |
| houtu-web-security              | houtu.web.session.jwt-signature-verify-key             | JWT验证签名类型HmacSHA的Base64密钥(type=JWT时，此值有效)，HmacSHA时可缺省等于houtu.web.session.jwt-signature-key | -                   | 否    |
| houtu-web-security              | houtu.web.session.jwt-signature-algorithm              | JWT签名类型                                                                                    | -                   | 否    |
| houtu-web-security              | houtu.web.security.enabled                             | 安全模块是否启用，启用则启用安全处理模块                                                                       | true                | 否    |
| houtu-web-security              | houtu.web.sign.sign-key                                | 默认HMacMD5验签密钥                                                                              | -                   | 是    |
| spring-cloud-houtu-loadbalancer | spring.cloud.loadbalancer.weight                       | 是否启用权重路由                                                                                   | true                | 否    |
| spring-cloud-houtu-loadbalancer | spring.cloud.loadbalancer.hint                         | 是否启用hint灰度策略                                                                               | true                | 否    |
| spring-cloud-houtu-loadbalancer | spring.cloud.loadbalancer.disable-gateway-request-hint | 是否禁用请求头中的hint参数，仅Spring-Cloud-Gateway有效                                                    | false               | 否    |

## 🤝 贡献

欢迎各种形式的贡献。

- 报告问题: 使用 issue 模板报告 bug 或提出功能请求
- 提交代码: fork 存储库，创建功能分支，提交 pull request
- 改进文档: 修正错误、添加示例、完善说明
- 测试反馈: 在不同环境下测试并提供反馈
