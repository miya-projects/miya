package com.miya.system.module.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 该类没实际用处，演示动态投影，动态投影的类必须拥有唯一的拥有所有需映射成员的构造函数
 * @link {https://stackoverflow.com/questions/53347063/why-are-interface-projections-much-slower-than-constructor-projections-and-entit}
 * Dynamic projections look pretty cool and fast, but must have exactly one constructor. No more, no less.
 * Otherwise Spring Data throws an exception,
 * because it doesn't know which one to use (it takes constructor parameters to determine which data to retrieve from DB).
 */

/**
 * 用户简单dto
 */
@Getter
@Setter
@AllArgsConstructor
public class SysUserSimpleDTO {

    private String id;
    private String name;

}
