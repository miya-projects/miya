# MIYA 中后台管理系统

## MIYA是什么?
    MIYA是一个追求简单易用的中后台软件开发包。你可以轻而易举得到一些实用的功能，基于springboot。

## 包含组件

|   模块   |   介绍   |
| ---- | ---- |
|  [miya-common](./miya-common/README.md)    |  不包含实际的业务功能    |
|  [miya-system](./miya-system/README.md)    |  系统级常用的功能模块，用户、角色、机构等    |
|  miya-examples    | 使用例子     |

## 在线体验
[https://miya.rxxy.icu](https://miya.rxxy.icu)

## 快速开始
1. 依赖配置
```xml
<dependency>
    <groupId>io.github.rxxy</groupId>
    <artifactId>miya-system</artifactId>
    <version>${latest}</version>
</dependency>
```
如果你想尝鲜快照版本，请添加如下代码
```xml
    <repositories>
        <repository>
            <id>central-snapshots</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>
```

2. 配置

### 必要配置
```shell
spring.datasource.url=jdbc:mysql://localhost:3306/miya?allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```
### 可选配置
#### ORM配置(请自行判断是否要开启)
```shell
#logging.level.org.hibernate.SQL=DEBUG
spring.jpa.properties.hibernate.show_sql=false
spring.jpa.properties.hibernate.hbm2ddl.auto=update
# 是否开启数据审计
spring.jpa.properties.hibernate.integration.envers.enabled=false
spring.jpa.properties.org.hibernate.envers.default_catalog=miya-deleted
```
####
```shell
# 开启特殊字符过滤(全局trim前端字符串参数的首尾空格，并去除如ZWSP这类不可见字符，@RequestBody接参不进行处理)
config.enable-special-character-filter=true
```

3. 启动springboot

## 技术栈
    springboot spring-mvc spring-data-jpa hibernate-search jwt等

## [更多]()

## [Q&A]()


