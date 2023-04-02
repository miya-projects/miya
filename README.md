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

2. 必要配置
```shell
config.db.url=jdbc:mysql://localhost:3306/miya?allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true
config.db.username=root
config.db.password=root
```
3. 启动springboot

## 技术栈
    springboot spring-mvc spring-data-jpa hibernate-search jwt等

## [更多]()

## [Q&A]()


