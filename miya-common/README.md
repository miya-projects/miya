## miya-common模块提供一些和具体业务无关的配置和功能

### 可覆盖接口
如果使用了一些jar包内提供的web接口且不能满足需求，可以重新实现该接口，使用Order注解将顺序调整为小于0即可覆盖掉之前的接口，如：
```java
    // jar包内的接口
    @PostMapping("login")
    public R<?> login(){
        log.info("jar包内登录成功");
        return R.success();
    }

    // 新实现的接口
    @Order(-1)
    @PostMapping("login")
    public R<?> login(){
        log.info("覆写后的登录成功");
        return R.success();
    }
```
这样即可覆盖掉原有的登录接口，值得注意的是，任何接口如果未标注@Order，那默认会认为是@Order(0)，即如果想覆盖接口，一定比0小才行。数字越小，优先级越高。具体逻辑见`com.miya.common.config.web.RepeatableRequestMappingHandlerMapping`。

### 常用数据校验
miya提供`@ValidFieldString`注解扩展了`hibernate-validation`的校验种类。
```java
@ValidFieldString(type = ValidFieldString.ValidType.EMAIL)
```
@ValidFieldString支持的校验类型包括
```
PHONE                       手机号
ID_CARD                     身份证号
EMAIL                       电子邮件
NUMBER                      数字
WORD                        字母
MONEY                       货币
ZIP_CODE                    邮政编码
PLATE_NUMBER                中国车牌号
URL                         url
CHINESE                     汉字
GENERAL                     标识符
CRON_EXPRESS                cronTab表达式
```

### Form -> Entity转换
在miya中，前端传过来的参数一般命名为xxxForm，继承BaseForm<Entity>后，可使用mergeToNewPo转换为entity。
很多情况下，前端传来的参数和entity字段类型并不一致，如传来是id，entity中是实体，这时使用Form封装前端传来的参数，再做转换。
```java

@ApiModel
@Getter
@Setter
public class SysUserForm extends BaseForm<SysUser> {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @ValidFieldString(type = ValidFieldString.ValidType.PHONE)
    private String phone;

    @FieldMapping(mappingClass = SysFile.class)
    private String avatar;

    private SysUser.Sex sex;

    @FieldMapping(mappingClass = SysRole.class)
    private Set<String> roles;

    @FieldMapping(mappingClass = SysDepartment.class)
    private Set<String> departments;

}

public class SysUser extends BaseEntity {

    private String username;
    private String phone;
    private Sex sex;
    private Set<SysDepartment> departments;
    private Set<SysRole> roles;
    private SysFile avatar;

}

@PostMapping
public R<?> save(@Validated SysUserForm sysUserForm) {
    SysUser sysUser = sysUserForm.mergeToNewPo();
    return R.success();
}

```

### 基于模板的excel导出
miya使用xlsx实现excel模板的导出功能
```java
XlsxUtil.export(templateInputStream, outputStream, data);
```

### 系统配置
实现系统配置的存取，更新等。
```java
    // 注入supplier，每次get从缓存中获取，如有更新，则重新获取(单点缓存)
    @Value("#{sysConfigService.getSupplier(T(com.miya.common.module.config.SystemConfigKeys).SYSTEM_VERSION)}")
    private Supplier<String> version;

    // 获取Optional
    Optional<String> val = sysConfigService.get(SystemConfigKeys.SYSTEM_NAME);

    // 获取值
    String exportWay = configService.getValOrDefaultVal(SystemConfigKeys.EXPORT_WAY);
```
