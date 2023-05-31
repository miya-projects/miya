miya-system作为一个业务模块，可直接启动，也可被其他业务模块依赖。

## 系统组件介绍

### 用户
1. 默认密码  
新建用户的默认密码和重置用户密码时的默认密码可通过创建`SysUserCustomizer`bean来修改，如果你想设置初始密码为手机号后六位，那可以：
```java
    @Bean
    public SysUserCustomizer sysUserCustomizer() {
        return SysUserCustomizer.builder().passwordGeneratorForNewUser(u -> {
        String phone = u.getPhone();
        return phone.substring(phone.length() - 7, phone.length() - 1);
        }).build();
    }
```
2. 用户首选项  
TODO

### 部门
非常朴实无华的部门管理功能,暂无任何扩展点

### 角色&权限
1. 功能定义  
   miya认为web系统中的功能是一组前端资源(路由/按钮/其他业务组件)和一组后端接口的组合。 且定义功能时，功能正在开发。

   由此，功能的定义是在后端的classpath中的 `business.json`文件中编写，如：
```json
 [
  {
    "name": "系统",
    "code": "sys",
    "children": [
      {
        "name": "角色",
        "code": "role",
        "children": [
          {
            "name": "查看",
            "code": "view"
          },
          {
            "name": "新增",
            "code": "add"
          }
        ]
      }
    ]
  }
]
```
以上文件描述了系统中角色模块中有两个功能，查看和新增。其中每一个功能都对应一个code，将功能节点的code和所有父节点用:连接起来即是该功能的fullCode，
Miya使用fullCode来表示一个功能的唯一性，如【角色查看】功能的fullCode为`sys:role:view`，前端也通过fullCode来控制菜单、路由、或其他组件的显隐

Miya默认搜索以下位置的business.json文件来作为功能定义文件`config/business.json, business.json, business/*.json, config/business/*.json`
你也可以通过以下方式更改这个设置
```java
    @Bean
    public SysUserCustomizer sysUserCustomizer() {
        return SysUserCustomizer.builder().businessFileName(Collections.singletonList("function.json")).build();
    }
```
2. api权限控制  
    定义好了功能文件，配置功能权限变的简单起来，只需在controller方法上加上注解`@Acl(business = "sys:user:add")`  
    1. 无需登录即可访问 加上`@Acl(userType = Acl.NotNeedLogin.class)`  
    2. 区分系统中有多种类型的用户
    如后台的管理员用户SysUser和手机app的客户Customer，你需要防止app用户带着token访问后端接口，那只需要标明用户类型即可， 
    如`@Acl(business = "sys:user:view", userType = SysUser.class)`
    3. 类级别配置 你也可以在controller class上指定这些，该类中所有方法将会生效，且可被方法级别的注解覆盖
3. 系统默认角色
    开发中总是会遇到有特殊的角色需进行特殊处理。这时只需定义枚举类且实现`DefaultRole`接口,方便判断。
```java
boolean isAdmin = SysDefaultRoles.ADMIN.hasThisRole(user);
```
4. 数据权限  
   TODO

### 数据字典
Emmm, 好像没什么可说的，非常常用和普通的数据字典模块，几乎不需要扩展点，直接用就好。

### 对象存储
 Miya编写了3套存储文件的实现，使用方式如下
1. 裸文件系统`BareSysFileService`
```properties
config.oss.type=bare
# 指定文件存储位置
config.oss.bare.upload-absolute-path=/upload
```
2. Minio `MinioSysFileService`
```properties
config.oss.type=minio
config.oss.minio.endpoint=
config.oss.minio.bucket-name=
config.oss.minio.access-key=
config.oss.minio.secret-key=
```
如果bucket没有被创建，服务会自动创建bucket
   3. Aliyun Oss `AliyunSysFileService`
```properties
config.oss.type=aliyun
config.oss.aliyun.endpoint=
config.oss.aliyun.bucket-name=
config.oss.aliyun.secret-key=
config.oss.aliyun.access-key=
```
如果bucket没有被创建，服务会自动创建bucket

#### 如何扩展其他的OSS实现?
实现`SysFileService`接口并注册为bean即可。

### 业务日志
1. 事件方式记录日志(可将业务事件继承自LogEvent，则会同时记录日志，不必单独分发LogEvent事件)
```java
LogEvent event = new LogEvent(content, operationType, businessId, extra);
applicationContext.publishEvent(event);
```
2. 日志服务记录日志
```java
logService.log(content, operationType, businessId, extra);
```

### 系统配置
可管理对系统的动态调整配置，可存放业务配置

[//]: # (### BOD)

[//]: # (1. 什么是BOD?  )

[//]: # (   全称`Backup On Delete`，是一种逻辑删除的替代方案，在数据删除前将数据备份到另外一个数据库schema的办法。)

[//]: # (2. 为什么要在删除数据时备份?  )

[//]: # (   普通方式的逻辑删除或多或少对系统产生一些副作用&#40;唯一约束&#41;和维护开销&#40;查询索引维护等&#41;, 在用户误操作删除数据后，提供除binlog外的另一种数据备份方式  )

[//]: # (   删除数据备份是一种悲观的设计思路，主张在删除后恢复，但更应该做的是认真考虑业务场景是否需要删除数据，这里仁者见仁，参考[《不要删除数据》]&#40;https://www.infoq.cn/article/2009/09/Do-Not-Delete-Data/&#41;)

[//]: # (3. 如何使用?)

[//]: # (   1. 增加如下配置)

[//]: # (      ```properties)

[//]: # (      config.backup-on-delete.enable=true)

[//]: # (      config.backup-on-delete.url=jdbc:mysql://192.168.2.170:3306/miya-backup?allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true)

[//]: # (      config.backup-on-delete.username=root)

[//]: # (      config.backup-on-delete.password=root)

[//]: # (      ```)

[//]: # (   2. 在需要做删除备份的实体类上增加`@BackupOnDelete`)

[//]: # (   3. 数据源创建好数据库即可，miya会自动为你创建和源数据一样的表结构)

### 邮件
配置发件邮箱
```java
    @Bean
    public MailAccount mailAccount(){
        MailAccount account = new MailAccount();
        account.setHost("smtp.qq.com");
        account.setPort(587);
        account.setAuth(true);
        account.setFrom("from");
        account.setUser("user");
        account.setPass("pass");
        return account;
    }
```
然后
```java
    private final EMailService emailService;
    emailService.sendText(emails, "subject", "content");
```

### 错误码定义
TODO `ResponseCode`

### 下载中心
TODO
### 短信
TODO
### 通知
TODO
### 动态字段
TODO
### 搜索服务
TODO

## Q&A

### 为什么将功能的定义放到后端的json中?
很多系统中将功能定义放置到数据库中，且配有界面方便进行动态配置，但其实我们在UI界面增加这一菜单后，只是多了个菜单，并不意味着
这个功能开发好了。功能的菜单和实现功能的代码是强耦合的，独立的菜单是无意义的。
将需要控制权限的功能放在json中，在开发功能时去维护，功能标识code和功能代码同时维护是合理的。前后端通过fullCode统一

### 


```shell
mvn clean install org.apache.maven.plugins:maven-deploy-plugin:2.8:deploy -DskipTests
```

## 模块外部依赖
    * 数据库
    * 邮件 (optional)
    * Aliyun SMS (optional)
    * 阿里云 OSS (optional)
    * Redis (optional)
    * ElasicSearch (optional)
