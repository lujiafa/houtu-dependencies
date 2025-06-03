### 一、功能描述
通过注解`@AutoFeign`提供Interface接口自动发布到`HandlerMapping`能力，即省去部分轻量且繁琐的Controller方法。

### 二、主要Maven依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>xxx</version>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>xxx</version>
</dependency>
```

### 三、功能详解
##### 3.1 接口自动发布
在`@FeignClient`注解的类或方法上加上注解`@AutoFeign`，如果该方法上也配置了`@RequestMapping`，则该接口会被自动发布到`HandlerMapping`中，可通过http或consumer服务请求访问。
* 注解可同时配置于`接口`、`实现类`、`接口方法`、`实现类方法`任意位置，优先级从前往后由低到高。
* 可通过有效`@AutoFeign`中属性`value()`来控制启用或禁用自动发布功能，true为启用，false为禁用，默认为true。
* 可通过有效`@AutoFeign`中属性`responseBody()`来控制是否将返回参数解析到响应体，功能同`@Controller`中的`@ResponseBody`或`@RestController`，true为启用，false为禁用，默认为false。
> 注：当同时拥有uri的`Controller`等实现和`@AutoFeign`自动发布实现时，会优先级按SpringMVC默认实现执行。