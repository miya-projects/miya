## miya-common模块提供一些和具体业务无关的配置和功能

### 可覆盖接口
打包后的接口一般会提供一些扩展满足不同的业务需求，但如果这些配置还是不能够满足需求，那可以写一个一样的接口覆盖掉这个接口，标注@Order注解即可
```java
    @Order(-1)
    @Acl(userType = Acl.NotNeedLogin.class)
    @ApiOperation(value = "登陆")
    @PostMapping("login")
    public R<?> login(){
        log.info("登录成功");
        return R.success();
    }
```
这样即可覆盖掉原有的登录接口，值得注意的是，任何接口如果未标注@Order，那默认会认为是@Order(0)，即如果想覆盖接口，一定比0小才行。数字越小，优先级越高。

### 常用数据校验
miya使用hibernate-validation来校验前端参数。
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

### Form Entity转换
在miya中，前端传过来的参数一般命名为xxxForm，继承BaseForm<Entity>后，可使用mergeToNewPo转换为entity。
很多情况下，前端传来的参数和entity字段类型并不一致，如传来是id，entity中是实体，这时使用Form封装前端传来的参数，再做转换。
```java

@ApiModel
@Getter
@Setter
public class SysUserForm extends BaseForm<SysUser> {

    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("电话")
    @ValidFieldString(type = ValidFieldString.ValidType.PHONE)
    private String phone;

    @ApiModelProperty("头像文件id")
    @FieldMapping(mappingClass = SysFile.class)
    private String avatar;

    @ApiModelProperty("性别")
    private SysUser.Sex sex;

    @FieldMapping(mappingClass = SysRole.class)
    private Set<String> roles;

    @FieldMapping(mappingClass = SysDepartment.class)
    private Set<String> departments;

}
```

