English | [中文](./README-CN.md)

## 1. spring-cloud-houtu-discovery
A service discovery extension that enhances service online status and health status. Maven dependency:
```pom
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-discovery</artifactId>
    <version>3.5.1</version>
</dependency>
```
### 1.1 Service Status Context
Provides the `ServiceContext` extension for runtime self-checking of whether a service is online (registered in the registry). This is suitable for internal self-running scenarios such as MQ consumption and scheduled task execution — when the service is explicitly offline, business processing is no longer executed.

### 1.2 Health Check Extension
Adds service online status detection to health checks. When a service is offline, the `/actuator/health` endpoint returns a 503 status code, which is useful in scenarios that require status detection.

## 2. spring-cloud-houtu-loadbalancer
Maven dependency:
```xml
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-loadbalancer</artifactId>
    <version>3.5.1</version>
</dependency>
```
### 2.1 Hint Overview
##### 2.1.1 Consumer-side LoadBalancerProperties Loading Priority
At runtime, `LoadBalancerProperties` are retrieved via `serviceId`. Priority (from high to low):
```mermaid
flowchart TD
F1["spring.cloud.loadbalancer.clients.[serviceId].[LoadBalancerProperties]"]
F2["spring.cloud.loadbalancer.[LoadBalancerProperties]"]
F1 -->|takes precedence over| F2
```

##### 2.1.2 Consumer-side Loadbalancer Hint Routing Mechanism
The service node list is obtained via service discovery, then `hint` is retrieved from `metadata` for matching. When the number of matched nodes is greater than 0, routing is done from the matched nodes; otherwise, routing uses the full node list by default.
Request-side `hint` retrieval priority (from high to low):
```mermaid
flowchart TD
H1["From request headers via LoadBalancerProperties.hintHeaderName"]
H2["HintContext.get()"]
H3["LoadBalancerProperties.hint.[serviceId]"]
H4["LoadBalancerProperties.hint.default"]
H5["HintContext.getXHint() (full-link hint)"]

H1 -->|takes precedence over| H2
H2 -->|takes precedence over| H3
H3 -->|takes precedence over| H4
H4 -->|takes precedence over| H5
```
##### 2.1.3 Server-side Hint Registration and Modification
When registering a service, `hint` can be set via `metadata` in the registration configuration, or modified through the registry center's service registration metadata.

##### 2.1.4 Node List Hint Matching Flow in Loadbalancer
```mermaid
flowchart TD
Start[Start] --> Step1
Step1[Get all Nodes via service discovery] --> Step2

Step2[Match all discovered Nodes by request hint when requesting LoadBalancer] --> Step3
Step3[Get match results] --> CheckMatchResult

CheckMatchResult{"Match count > 0?"} -->|Yes| Step4
CheckMatchResult -->|No| Step5
Step4[Route using matched Node list] --> Step6
Step5[Route using all Nodes by default] --> Step6
Step6[Return Node list] --> End
End[End]
```

##### 2.1.5 Full-link Hint
```mermaid
flowchart TD
CLIENT[Client]
REQUEST["Request, optionally carrying x-hint header"]
subgraph SpringCloudGateway [Gateway Service]
    H1
    H2["No x-hint"]
    H3["Has x-hint"]
    H4["Predicate Handler"]
    H5
    H6["Request downstream service"]
    subgraph GatewayNextRequest["RPC Service Call"]
        E1["LoadBalancer"]
        E2["Consumer-side hint routing mechanism -> load balance"]
        E3
        E4["Add x-hint to request header"]
        E5["Regular request"]

        E1 --> E2
        E2 --> E3
        E3{"Does x-hint exist?"} -->|Yes| E4
        E3 -->|No| E5
    end
    
    H1{"Filter client x-hint enabled?"} -->|Yes| H2
    H1 -->|No| H3
    H2 --> H4
    H3 --> H4
    H4 --> H5
    H5{"hint or x-hint added to request header, or set via HintContext.set..(String)?"} -->|Yes, overwrite if exists| GatewayNextRequest
    H5 -->|No| GatewayNextRequest
end

subgraph MicroService [Microservice]
    M1["Business processing"]
    M2
    M3["Business processing and response"]
    M4["Optionally set HintContext.set..(String)"]
    M5["Supports @Async and other async thread propagation"]
    subgraph NextRequest["RPC Service Call"]
        F1["LoadBalancer"]
        F2["Consumer-side hint routing mechanism -> load balance"]
        F3
        F4["Add x-hint to request header"]
        F5["Regular request"]
        F1 --> F2
        F2 --> F3
        F3{"Does x-hint exist?"} -->|Yes| F4
        F3 -->|No| F5
    end
    M1 --> M2
    M2{"Next-layer microservice call?"} -->|Yes| M4
    M2 -->|No| M3
    M4 --> M5
    M5 --> NextRequest
    NextRequest -->|Call next microservice| M1
end

CLIENT --> REQUEST
REQUEST --> MicroService
REQUEST --> SpringCloudGateway
SpringCloudGateway --> MicroService
```

### 2.2 Weighted Routing and Failover Drift
Supports configuring `weight` (1-100) to control routing node weights — the higher the weight, the higher the priority.
When a consumer-side service call encounters an exception on a specific node, the node's weight is reduced for demotion, achieving exception-driven failover drift.
```mermaid
flowchart TD
START[Business Processing]
subgraph NextRequest["RPC Service Call"]
    F1["LoadBalancer"]
    F2["Route by weight"]
    F3["RPC Request"]
    F4
    F5["Reduce Node weight, each time to 80% of current, minimum 1"]
    F6
    F7["Increase weight until original weight is restored"]
    END[Response]
    F1 --> F2
    F2 --> F3
    F3 --> F4
    F4{"Call exception?"} -->|Yes| F5
    F4 -->|No| F6
    F5 --> END
    F6{"Has current Node been demoted (weight below original)?"} --> F7
    F6 --> END
    F7 --> END
end
START --> NextRequest
```

### 2.3 Enhanced with Retry
In environments with high business stability requirements, you can combine circuit breaking, retry, and service instance failover drift to improve service availability.
Example retry configuration:
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


## 3. spring-cloud-houtu-feign
When serving as a microservice server, by adding the `@AutoFeign` annotation to a `@FeignClient`-annotated interface, the interface implementation bean will be automatically published based on method annotations such as @RequestMapping, @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, etc. — no need to write separate Controller classes.

Maven dependency:
```pom
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-feign</artifactId>
    <version>3.5.1</version>
</dependency>
```

Other enhancements or configurations can be adjusted according to business needs, such as configuring connection timeout:
```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connect-timeout: 1000
```

## 4. spring-cloud-houtu-alibaba-sentinel
Provides WritableDataSource delegation and persistence support for Alibaba Sentinel rules including circuit breaking (DegradeRule), rate limiting (FlowRule), authority control (AuthorityRule), and system protection (SystemRule), with built-in Web (SpringMVC/WebFlux) BlockException handlers. Combined with the Spring Cloud Alibaba Sentinel Nacos DataSource, rules can be persisted to the Nacos config center.
Maven dependency:
```pom
<dependency>
    <groupId>io.github.lujiafa</groupId>
    <artifactId>spring-cloud-houtu-alibaba-sentinel</artifactId>
    <version>3.5.1</version>
</dependency>
```

### 4.1 Example Rate Limiting Configuration in Config Center
Configure a dataId as `xx-flow-rule.json` with the following content:
```json
[
  {
    "resource": "/user/findByUserName",    // Resource name
    "limitApp": "default",                // Source app (default means no distinction)
    "grade": 1,                           // Threshold type (1=QPS)
    "count": 10,                          // Single-machine threshold (10 QPS)
    "strategy": 0,                        // Flow control mode (0=direct)
    "controlBehavior": 0,                 // Flow control effect (0=fast fail)
    "clusterMode": false                  // Cluster mode
  }
]
```
### 4.2 Example Service Configuration
```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8858
      datasource:
        nacos:
          server-addr: ${spring.cloud.nacos.server-addr}
        # Key for the config Map, used to assist Bean generation. See SentinelDataSourceHandler
        flow:
          nacos:
            data-id: ${spring.application.name}-flow-rule.json
            group-id: SENTINEL_GROUP
            type: json
            rule-type: flow
```
