package com.miya.system.module.common;

import cn.hutool.core.util.ClassUtil;
import com.miya.common.module.base.BaseApi;
import com.miya.system.config.web.ReadableEnum;
import com.miya.system.module.common.dto.DropDownItemDTO;
import com.miya.common.model.dto.base.R;
import com.miya.system.module.role.model.QSysRole;
import com.miya.system.module.user.model.QSysUser;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.SysUserRepository;
import com.miya.system.module.role.SysDefaultRoles;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.util.CastUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 各种下拉框接口
 */
@RequestMapping("/dp")
@RestController
@Api(tags = {"下拉框可选项接口"})
@Validated
public class DropDownListApi extends BaseApi implements InitializingBean {

    @Resource
    private SysUserRepository sysUserRepository;

    private static final Map<String, List<Map<String, String>>> MAP = new HashMap<>();

    @Resource(name = "scanPackageForReadableEnum")
    private List<String> scanPackageForReadableEnum;


    /**
     * 获取角色列表 下拉框使用
     */
    @GetMapping(value = "role")
    @ApiOperation(value = "获取角色列表", notes = "不分页 下拉框使用")
    public R<?> sysRoleList() {
        QSysRole qSysRole = QSysRole.sysRole;
        JPAQuery<DropDownItemDTO> query = qf.select(
                Projections.bean(DropDownItemDTO.class,
                        qSysRole.id.as(DropDownItemDTO.Fields.value),
                        qSysRole.name.as(DropDownItemDTO.Fields.label))
        ).from(qSysRole);
        return R.successWithData(query.fetch());
    }

    @GetMapping(value = "departments")
    @ApiOperation(value = "获取部门列表", notes = "不分页 下拉框使用")
    public R<?> departments() {
        // todo
        QSysRole qSysRole = QSysRole.sysRole;
        JPAQuery<DropDownItemDTO> query = qf.select(
                Projections.bean(DropDownItemDTO.class,
                        qSysRole.id.as(DropDownItemDTO.Fields.value),
                        qSysRole.name.as(DropDownItemDTO.Fields.label))
        ).from(qSysRole);
        return R.successWithData(query.fetch());
    }

    @GetMapping("enumsKey")
    @ApiOperation("查询枚举key")
    public R<Set<String>> enums() {
        return R.successWithData(MAP.keySet());
    }

    @GetMapping(value = "enums")
    @ApiOperation("查询枚举项")
    public R<List<Map<String, String>>> queryEnum(@ApiParam("查询哪个枚举项") @RequestParam String key) {
        return R.successWithData(MAP.getOrDefault(key, CastUtils.cast(Collections.EMPTY_LIST)));
    }

    @GetMapping("users")
    @ApiOperation(value = "查询所有用户")
    public R<List<DropDownItemDTO>> users(@ApiParam("搜索用户名") String key) {
        BooleanBuilder bb = new BooleanBuilder();
        Optional.ofNullable(key).ifPresent(k -> bb.and(QSysUser.sysUser.name.contains(key)));
        Iterable<SysUser> all = sysUserRepository.findAll(bb);
        List<DropDownItemDTO> result = StreamSupport.stream(all.spliterator(), false).map(user -> {
            DropDownItemDTO dropDownItem = new DropDownItemDTO();
            dropDownItem.setLabel(user.getName());
            dropDownItem.setValue(user.getId());
            return dropDownItem;
        }).collect(Collectors.toList());
        return R.successWithData(result);
    }

    @GetMapping("usersForRole")
    @ApiOperation(value = "查询指定角色的用户")
    public R<List<DropDownItemDTO>> usersForRole(@ApiParam("搜索用户名") String key, @NotBlank String role) {
        SysDefaultRoles sysDefaultRoles;
        try {
            sysDefaultRoles = SysDefaultRoles.valueOf(role);
        }catch (IllegalArgumentException e){
            return R.errorWithMsg("角色不存在");
        }
        BooleanBuilder bb = new BooleanBuilder();
        Optional.ofNullable(key).ifPresent(k -> bb.and(QSysUser.sysUser.name.contains(key)));
        bb.and(QSysUser.sysUser.roles.contains(sysDefaultRoles.getSysRole()));
        Iterable<SysUser> all = sysUserRepository.findAll(bb);
        List<DropDownItemDTO> result = StreamSupport.stream(all.spliterator(), false).map(user -> {
            DropDownItemDTO dropDownItem = new DropDownItemDTO();
            dropDownItem.setLabel(user.getName());
            dropDownItem.setValue(user.getId());
            return dropDownItem;
        }).collect(Collectors.toList());
        return R.successWithData(result);
    }

    @Override
    public void afterPropertiesSet() {
        List<Class<? extends Enum<? extends Enum<?>>>> classes = CastUtils.cast(scanPackageForReadableEnum.stream().flatMap(p -> ClassUtil.scanPackageBySuper(p, ReadableEnum.class)
                .stream().filter(Class::isEnum)).collect(Collectors.toList()));

        classes.forEach(clazz -> {
            String name = clazz.getName();
            String key = name.substring(name.lastIndexOf(".") + 1)
                    .replace("$", ".");
            List<Map<String, String>> list = new ArrayList<>();
            for (Enum<? extends Enum<?>> e : clazz.getEnumConstants()) {
                Map<String, String> map = new HashMap<>();
                String label = e.name();
                if (e instanceof ReadableEnum){
                    label = ((ReadableEnum) e).getName();
                }
                map.put("value", e.name());
                map.put("label", label);
                list.add(map);
            }
            MAP.put(key, list);
        });
    }

}
