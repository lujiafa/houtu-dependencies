### 一、功能描述
* 登录Session会话跟踪、会话校验和会话数据获取等等认证能力；
* 会话用户的方法级功能鉴权能力，也可以自主实现数据级鉴权，可支持角色鉴权和权限鉴权；
* 防重放检测与拦截；
* 提供请求数据签名验证能力。

### 二、主要Maven依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>xxx</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>xxx</version>
</dependency>
```

### 三、功能详解
##### 3.1 Session会话模块
会话模块默认为启用状态，不使用可能会影响鉴权等功能。若想要明确关闭此功能，可设置配置`web.security.checkSession=false`关闭。
* Session相关配置<br>
  可查看`com.houtu.websecurity.prop.SecurityProperties.SessionProperties`类。
* 自主实现用户登录，登录成功时通过`SessionContext`创建并保存会话数据。如下示例:
```java
public ResponseData login(...) {
        ...
        Session session = SessionContext.create();
        session.setAttribute("xx", XX); //用户自定义信息
        SessionContext.save(session); // 存储会话信息并输出到HttpServletResponse（配置默认输出响应头，字段名"sid"）
        ...
}
```
* 互斥会话场景<br>
  <b>场景：</b>通过某个或某些维度实现单点登录。 <br>
  <b>实现方式：</b>通过`k1:v1`->`sessionId`，多个维度时即多维度都关联`sessionId`，会话检测时若任一维度不匹配即视为会话已失效。
  <b>示例：</b>实现用户单点登录，用户可通过用户名、手机号、邮箱实现登录
```java
public ResponseData login(...) {
        ...
        String sessionId = UUIDUtils.genUUIDString();
        Map<String, String> mutexMap = new HashMap<String, String>(3);
        mutexMap.put("userId", user.getUserId());
        Session session = SessionContext.create(sessionId, mutexMap);
        //session.setAttribute("xx", XX);
        SessionContext.save(session);
        ...
}
```
示例中通过`userId`作为互斥维度条件，即同一个`userId`在另一个位置登录时，上一个`userId`对应登录自动失效。当多个互斥维度时，任一维度检测到已在其他地方登录，上一个会话立即失效。<br>
* 自定义数据存储与获取。如下示例：
```java
public ResponseData login(...){
        ...
        Session session = SessionContext.create();
        session.setAttribute("userInfo",User); // 将用户对象添加到会话中，User需实现接口java.io.Serializable
        SessionContext.save(session);
        ...
}

@CheckSession
public ResponseData pay() {
        ...
        Session session = SessionContext.get();
        User user = session.get("userInfo");
        ...
}
```
* 会话检测
  * 会话检测需要配置`web.security.checkSession=true`，默认值即为true
  * 方法或类上加注解`CheckSession`，当方法和类上同时配置时，注解遵循局部配置优先级高于全局配置原则，注解中可通过`value`启用(true)或禁用(false)来控制是否会话检测，默认为true。
* 登出会话
通过`SessionContext.remove()`实现会话移除。

##### 3.2 签名功能
签名模块默认为启用状态，不使用也不会影响整体功能。若想要明确关闭此功能，可设置配置`web.security.checkSign=false`关闭。
* 请求头中必须包含`rid`参数，为requestId数据，可使用UUID或其他全局唯一，当配置`web.security.enableHeader=false`（默认值为true）时该参数需和普通参数一起请求；
* 在方法上加上注解`CheckSign`，当方法和类上同时配置时，注解遵循局部配置优先级高于全局配置原则，注解中可通过`value()`启用(true)或禁用(false)来控制是否签名检测，默认为true。<br>
* 默认签名方式为`HMacMd5`；
* 默认签名密钥为`d2VuZGFvcmVu`，可通过`web.security.sign.defaultSignKey`设置自定义默认密钥；
* 登录后签名密钥可通过自定义登录设置来覆盖默认密钥，实现登录后验签密钥动态变化，从而实现进一步的安全验证升级，但需注意登录验签密钥仅在`@CheckSession`和`@CheckSign`同时生效时生效，以防止特殊情况下的响应混淆。如下示例：
```java
public ResponseData login(...) {
        ...
        Session session = SessionContext.create();
        session.setAttribute(SecurityConstant.SIGN_KEY_ATTR_NAME, "123"); //登录后验签密钥设置
        SessionContext.save(session);
        ...
}
```
* 若`HMacMd5`仍不满足业务验签要求，可通过继承`com.houtu.websecurity.sign.AbstractSignatureValidator`或实现接口`com.houtu.websecurity.sign.SignatureValidator`来实现自定义签名验证器，自定义实现后通过`@Bean`生成对象到spring容器即可替换默认签名验证器。

###### 3.3 防重放
作用为防止终端底层或有人恶意抓包重放请求，默认为关闭，可以通过`web.security.checkRepeat=true`开启。
* 请求头中必须好办`rid`参数，为requestId数据，可使用UUID或其他全局唯一，当配置`web.security.enableHeader=false`（默认值为true）时该参数需和普通参数一起请求。

##### 3.4 角色权限
角色权限模块默认为启用状态，不使用也不会影响整体功能。若想要明确关闭此功能，可设置配置`web.security.checkPermission=false`关闭。
* 登录赋值权限数据
```java
public ResponseData login(...) {
        ...
        Session session = SessionContext.create();
        session.addRole("admin"); // 用户赋予admin角色，也可以通过session.addRoles(List)批量添加角色
        session.addPermission("menu.update"); //用户赋予"menu.update"权限，也可以通过session.addPermissions(List)批量添权限
        SessionContext.save(session);
        ...
}
```
* 鉴权
  * 角色验证
    在方法上加上注解`RequiresRole`，对其`value`赋予访问当前方法所需角色，`value`位多个值时可通过`logic`来设置是逻辑`OR`(拥有任一角色即可)或是`AND`(必须同时拥有所有角色)缺省默认为`OR`。
  * 权限验证
    在方法上加上注解`RequiresPermission`，对其`value`赋予访问当前方法所需权限，`value`位多个值时可通过`logic`来设置是逻辑`OR`(拥有任一权限即可)或是`AND`(必须同时拥有所有权限)，缺省默认为`OR`。
