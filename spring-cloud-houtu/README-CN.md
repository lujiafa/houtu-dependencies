[English](./README.md) | 中文

## 一、spring-cloud-houtu-discovery
作为服务发现扩展，提供对服务在线状态与健康状态增强，其Maven引入配置如下：
```pom
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-discovery</artifactId>
    <version>3.5.1</version>
</dependency>
```
### 1.1 服务状态上下文
提供状态上下文`ServiceContext`扩展，支持运行时自检测服务状态是否在线（在注册中心），适用于MQ消费、定时任务执行等等内部自运行场景，当明确服务下线时，不再执行业务处理。

### 1.2 健康检查拓展
在健康检查中增加服务状态在线检测，当服务离线时通过`/actuator/health`检测时状态码为503，在某些需要检测的场景较为有用。

## 二、spring-cloud-houtu-loadbalancer
其Maven引入配置如下：
```xml
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-loadbalancer</artifactId>
    <version>3.5.1</version>
</dependency>
```
### 2.1 Hint概要
##### 2.1.1 消费端LoadBalancerProperties加载优先流程
运行时通过`serviceId`获取`LoadBalancerProperties`, 优先级（从高到低）如下：
```mermaid
flowchart TD
F1["spring.cloud.loadbalancer.clients.[serviceId].[LoadBalancerProperties]"]
F2["spring.cloud.loadbalancer.[LoadBalancerProperties]"]
F1 -->|优先于| F2
```

##### 2.1.2 消费端Loadbalancer路由hint匹配机制
通过服务发现获取服务Node列表，然后`metadata`中获取`hint`进行，当匹配到节点数大于0时则从匹配Node中路由，否则使用默认全量Node进行路由。
请求方`hint`获取优先级（从高到低）如下：
```mermaid
flowchart TD
H1["请求头中通过LoadBalancerProperties.hintHeaderName获取"]
H2["HintContext.get()"]
H3["LoadBalancerProperties.hint.[serviceId]"]
H4["LoadBalancerProperties.hint.default"]
H5["HintContext.getXHint()（全链路hint）"]

H1 -->|优先于| H2
H2 -->|优先于| H3
H3 -->|优先于| H4
H4 -->|优先于| H5
```
##### 2.1.3 服务端注册hint配置与修改
服务注册时可以通过注册配置中`metadata`设置`hint`，也可以通过注册中心修改服务注册信息中的`metadata`。

##### 2.1.4 Loadbalancer中Node列表通过hint匹配流程
```mermaid
flowchart TD
Start[开始] --> Step1
Step1[通过服务发现获取所有 Node] --> Step2

Step2[请求 LoadBalancer 时通过请求 hint 匹配所有发现的 Node] --> Step3
Step3[获得匹配结果] --> CheckMatchResult

CheckMatchResult{"匹配结果数 > 0?"} -->|是| Step4
CheckMatchResult -->|否| Step5
Step4[通过匹配 Node 列表进行路由] --> Step6
Step5[默认使用全部 Node 进行路由] --> Step6
Step6[返回 Node 列表] --> End
End[结束]
```

##### 2.1.5 全链路Hint
```mermaid
flowchart TD
CLIENT[客户端]
REQUEST["请求，可选请求头携带参数 x-hint"]
subgraph SpringCloudGateway [网关服务]
    H1
    H2["无 x-hint"]
    H3["有 x-hint"]
    H4["谓词处理器"]
    H5
    H6["请求下游服务"]
    subgraph GatewayNextRequest["RPC服务调用"]
        E1["LoadBalancer"]
        E2["消费端Loadbalancer路由hint匹配机制 -> load balance"]
        E3
        E4["在请求头添加 x-hint"]
        E5["常规请求"]

        E1 --> E2
        E2 --> E3
        E3{"是否存在x-hint"} -->|是| E4
        E3 -->|否| E5
    end
    
    H1{"是否启用过滤客户端 x-hint"} -->|是| H2
    H1 -->|否| H3
    H2 --> H4
    H3 --> H4
    H4 --> H5
    H5{"是否在请求头添加 hint 或 x-hint，或通过HintContext.set..(String)设置？"} -->|是，存在则覆盖| GatewayNextRequest
    H5 -->|否| GatewayNextRequest
end

subgraph MicroService [微服务]
    M1["业务处理"]
    M2
    M3["业务处理与响应"]
    M4["可选设置HintContext.set..(String)"]
    M5["支持@Async等异步线程传递"]
    subgraph NextRequest["RPC服务调用"]
        F1["LoadBalancer"]
        F2["消费端Loadbalancer路由hint匹配机制 -> load balance"]
        F3
        F4["在请求头添加 x-hint"]
        F5["常规请求"]
        F1 --> F2
        F2 --> F3
        F3{"是否存在x-hint"} -->|是| F4
        F3 -->|否| F5
    end
    M1 --> M2
    M2{"是否存在下一层微服务调用？"} -->|是| M4
    M2 -->|否| M3
    M4 --> M5
    M5 --> NextRequest
    NextRequest -->|调用下一个微服务| M1
end

CLIENT --> REQUEST
REQUEST --> MicroService
REQUEST --> SpringCloudGateway
SpringCloudGateway --> MicroService
```

### 2.2 权重路由与降级飘移
支持配置权重`weight`(1-100)控制路由节点的权重，权重值越大，优先级越高。
当消费端服务调用服务端某节点发生异常后，会对该节点进行降权降级，从而实现异常飘移。
```mermaid
flowchart TD
START[业务处理]
subgraph NextRequest["RPC服务调用"]
    F1["LoadBalancer"]
    F2["根据weight路由"]
    F3["RPC请求"]
    F4
    F5["对Node weight进行降权，每次降低为原来的80%，最低为1"]
    F6
    F7["加权，直至恢复原有权重"]
    END[响应]
    F1 --> F2
    F2 --> F3
    F3 --> F4
    F4{"是否调用异常"} -->|是| F5
    F4 -->|否| F6
    F5 --> END
    F6{"当前使用Node是否被降权过（即weight低于原始值）？"} --> F7
    F6 --> END
    F7 --> END
end
START --> NextRequest
```

### 2.3 配合Retry增强
在对业务稳定要求较高环境，可以结合熔断、重试、服务实例降级飘移等方式，提升服务可用性。
实例重试配置如下：
```yaml
spring:
  cloud:
    loadbalancer:
      cache:
        ttl: 5s
      retry:
        retry-on-all-operations: true
        retryable-exceptions:
          - java.net.ConnectException
          - java.net.UnknownHostException
          - java.net.http.HttpConnectTimeoutException
```


## 三、spring-cloud-houtu-feign
作为微服务服务端时，通过`@FeignClient`注解接口，增加`@AutoFeign`注解，该接口实现Bean会根据方法@RequestMapping、@GetMapping、@PostMapping、@PutMapping、@DeleteMapping等等自动发布，无需在单独写Controller类。

其Maven引入配置如下：
```pom
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-feign</artifactId>
    <version>3.5.1</version>
</dependency>
```

其他增强或配置可以根据业务自行调整，如配置连接超时时间：
```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connect-timeout: 1000
```

## 四、spring-cloud-houtu-alibaba-sentinel
基于 Alibaba Sentinel 提供熔断降级（DegradeRule）、流量控制（FlowRule）、权限控制（AuthorityRule）、系统保护（SystemRule）等规则的 WritableDataSource 委托持久化支持，并内置 Web（SpringMVC/WebFlux）BlockException 处理器。配合 Spring Cloud Alibaba Sentinel Nacos DataSource 可实现规则持久化到 Nacos 配置中心。
其Maven引入配置如下：
```pom
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-alibaba-sentinel</artifactId>
    <version>3.5.1</version>
</dependency>
```

### 4.1 示例配置中心限流配置
配置dataId为`xx-flow-rule.json`，内容如下：
```json
[
  {
    "resource": "/user/findByUserName",    // 资源名
    "limitApp": "default",                // 来源应用（default表示不区分）
    "grade": 1,                           // 阈值类型（1=QPS）
    "count": 10,                          // 单机阈值（10 QPS）
    "strategy": 0,                        // 流控模式（0=直接）
    "controlBehavior": 0,                 // 流控效果（0=快速失败）
    "clusterMode": false                  // 是否集群
  }
]
```
### 4.2 示例服务配置引入
```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8858
      datasource:
        nacos:
          server-addr: ${spring.cloud.nacos.server-addr}
        # 作为配置Map的Key，用于协助生成Bean。参考SentinelDataSourceHandler
        flow:
          nacos:
            data-id: ${spring.application.name}-flow-rule.json
            group-id: SENTINEL_GROUP
            type: json
            rule-type: flow
```
