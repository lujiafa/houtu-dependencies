### 一、功能描述
* 提供`NacosLoadBalancer`增强组件，通过`spring.cloud.loadbalancer.nacos.enabled=true`显示的开启；
* 提供服务级和全链路`hint`支持，通过请求下游服务或注册中心metadata中`hint`值控制路由优先。

### 二、主要Maven依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

### 三、功能详解
##### 3.1 NacosLoadBalancer
* 修复SCA源码`NacosLoadBalancer`中Supplier.get(..)参数request丢失问题。
* 继承SCA类`com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer`其他所有特性。


##### 3.2 `hint`支持
微服务链路中，服务A请求下游服务B(B1...Bn...Bx)，若A服务中通过`服务发现`的实例列表为`B1..Bx`，其中`B1..Bn`对应`metadata`中key`hint`值与当前服务`A`上下文中`hint`匹配时，则请求该下游服务B的所有调用流量都会路由到`B1...Bn`；若服务A上下文中的`hint`没有匹配到`服务发现`中任何实例，则调用流量会路由到所有发现实例`B1...Bx`。
* 服务上下文`hint`获取
```txt
## 1、代码内置默认hint为"default"

## 2、指定服务中所有loadbalancer下游服务筛选默认hint
spring.cloud.loadbalancer.hint.default=test
        
## 3、指定服务中特定下游服务provider-service筛选默认hint
spring.cloud.loadbalancer.clients.provider.hint.default=test

## 4、请求头中添加 X-SC-LB-Hint ，其值为hint，该值实现全链路传递。

## 5、请求头中添加LoadBalancerProperties.hintHeaderName（对应2~6同对象配置）名属性，该值实现全链路传递，hintHeaderName默认名同6(X-SC-LB-Hint)。

## 6、指定服务调用下游服务provider-service筛选hint
spring.cloud.loadbalancer.hint.provider-service=gray

## 7、指定服务调用下游服务provider-service筛选hint
spring.cloud.loadbalancer.clients.provider.hint.provider-service=gray
```
当以上配置或参数同时存在时，优先级从上到下由低到高选择。
> 注：<br>
> 1.在路由hint选择时，当3/7对应的`LoadBalancerProperties`配置存在时，2/6对应`LoadBalancerProperties`不生效。<br>
> 2.全链路hint跨服务传递目前仅支持`feign`实现。<br>
> 3.全链路hint跨服务传递过程中，若存在异步调用，建议通过以下方式执行：<br>
> * 代理方法加注解`@Async`；
> * ApplicationContext容器中`ThreadPoolTaskExecutor`对象、`ThreadPoolTaskExecutor`对象或`ExecutorService`线程池对象，又或者自定义初始化实现`TransferThreadPoolExecutor`线程池来执行。


### 四、配置
| 配置名 | 值类型     | 描述                                               | 默认值   |
|-----|---------|--------------------------------------------------|-------|
|spring.cloud.loadbalancer.nacos.enabled| boolean | 是否启用NacosLoadBalancer路由实现                      | false |
|spring.cloud.loadbalancer.hint.enable| boolean | 是否启用hint                                         | true  |
|spring.cloud.loadbalancer.hint.enableGatewayRequestHeader| boolean | 仅网关服务中生效，是否开启过滤外部所有请求头中全链路hint属性("X-SC-LB-Hint") | false |
