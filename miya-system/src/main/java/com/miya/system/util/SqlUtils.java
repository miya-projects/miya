package com.miya.system.util;

import java.util.Map;
import java.util.Objects;

/**
 * @author fanchao
 * @description 获取一些常用简单的sql 工具类
 * @date 2018-11-09
 */
public class SqlUtils {

    public final static String FUZZY_QUERY_FIELD = "fuzzyQueryField";
    public final static String FUZZY_QUERY_VALUE = "fuzzyQueryValue";
    public final static String FUZZY_QUERY_OTHER_TABLE_INFO = "fuzzyQueryOtherTableInfo";

    /**
     * 获取分页sql
     *
     * @param pageSize    页数大小
     * @param currentPage 当前页码
     * @return
     */
    public static String getPagingSQL(Integer pageSize, Integer currentPage) {
        pageSize = Objects.isNull(pageSize) ? 10 : pageSize;
        currentPage = Objects.isNull(currentPage) ? 1 : currentPage;
        return " LIMIT " + (currentPage - 1) * pageSize + "," + pageSize + " ";
    }


    /**
     * 获取 Condition条件
     *
     * @param map pageSize            每页记录数
     *            currentPage         要查询页码
     *            fuzzyQueryField     要模糊查询的字段 （多个字段用英文逗号分隔）
     *            fuzzyQueryValue     要模糊查询的值
     *            fuzzyQueryOtherTableInfo 需要模糊查询外表的信息(多个表用逗号分隔，表里多个数据用减号分隔)
     *            格式:
     *            外键字段1-外键关联的表名1-外表1模糊查询的字段1-外表1模糊查询的字段2-...,
     *            外键字段2-外键关联的表名2-外表2模糊查询的字段1-外表2模糊查询的字段2-...,
     *            ......
     *            eg：
     *            view_spot_id-view_spot-name-place,...
     *            <p>
     *            update_time_start   更新开始时间
     *            update_time_end     更新结束时间
     *            create_time_start   创建开始时间
     *            create_time_end     创建结束时间
     * @return 获取 Condition 条件
     * @author 樊超
     */
    public static String getCondition(Map<String, String> map) {
        String condition = " WHERE 1 = 1";
        //获取需要模糊查询的条件sql
        condition = getFuzzyQueryCondition(condition, map);
        //获取精确查询的条件
        condition += getAccurateQueryCondition(map);
        return condition;
    }


    /**
     * 获取模糊查询的条件
     *
     * @param condition 模糊查询条件之前还需要加的条件
     * @param map       存放条件的信息 (详情请看  getCondition 方法注释)
     * @return 字符串
     * <p>
     * 注意：此方法中 fuzzyQueryOtherTableInfoArrayArray数组
     * 下标[0] 存放的是 外键id字段 (本表中的字段)
     * 下标[1] 存放的是  外键关联的表名
     * 其余的则是 外表需要模糊查询的字段
     */
    public static String getFuzzyQueryCondition(String condition, Map<String, String> map) {
        String fuzzyQueryField = map.get("fuzzyQueryField");
        String fuzzyQueryValue = Objects.isNull(map.get("fuzzyQueryValue")) ? "" : map.get("fuzzyQueryValue");
        String fuzzyQueryOtherTableInfo = map.get("fuzzyQueryOtherTableInfo");
        // 判断模糊查询字段与 模糊查询值是否为null
        boolean whetherPerform = Objects.nonNull(fuzzyQueryField) && Objects.nonNull(fuzzyQueryValue);
        if (whetherPerform || Objects.nonNull(fuzzyQueryOtherTableInfo)) {
            condition += " and (";
            if (Objects.nonNull(fuzzyQueryOtherTableInfo)) {
                // 包含多个其他表用 ',' (英文状态下的逗号)分隔
                String[] fuzzyQueryOtherTableInfoArray = fuzzyQueryOtherTableInfo.split(",");
                for (int i = 0; i < fuzzyQueryOtherTableInfoArray.length; i++) {
                    //其他表里的信息() 用 '-'( 英文状态下的减号)分隔
                    String[] fuzzyQueryOtherTableInfoArrayArray = fuzzyQueryOtherTableInfoArray[i].split("-");
                    condition += fuzzyQueryOtherTableInfoArrayArray[0] + " in (SELECT id FROM " + fuzzyQueryOtherTableInfoArrayArray[1] + " WHERE ";
                    //遍历其他表字段信息
                    for (int j = 2; j < fuzzyQueryOtherTableInfoArrayArray.length; j++) {
                        if (j == 2) {
                            condition += fuzzyQueryOtherTableInfoArrayArray[j] + " like '%" + fuzzyQueryValue + "%'";
                        } else {
                            condition += " or " + fuzzyQueryOtherTableInfoArrayArray[j] + " like '%" + fuzzyQueryValue + "%'";
                        }
                    }
                    if (i != fuzzyQueryOtherTableInfoArray.length - 1 || whetherPerform) {
                        condition += ")  or ";
                    } else {
                        condition += ") ";
                    }

                }
            }
            // 当模糊查询字段与值为null 时不执行
            if (whetherPerform) {
                String[] fuzzyQueryFields = fuzzyQueryField.split(",");
                for (int i = 0; i < fuzzyQueryFields.length; i++) {
                    if (i == 0) {
                        condition += fuzzyQueryFields[i] + " like '%" + fuzzyQueryValue + "%'";
                    } else {
                        condition += " or " + fuzzyQueryFields[i] + " like '%" + fuzzyQueryValue + "%'";
                    }
                }

            }
            condition += ")";
        }
        return condition;

    }

    /**
     * 获取精准查询的条件(目前只添加时间)
     *
     * @param map 存放条件的信息 (详情请看  getCondition方法 注释)
     * @param
     * @return 返回精准查询的条件
     */
    public static String getAccurateQueryCondition(Map<String, String> map) {
        String update_time_start = map.get("update_time_start");
        String update_time_end = map.get("update_time_end");
        String create_time_start = map.get("create_time_start");
        String create_time_end = map.get("create_time_end");
        String condition = "";
        //添加日期查询的条件
        if (!(Objects.isNull(update_time_start) && Objects.isNull(update_time_end))) {
            condition += " and update_time >= '" + update_time_start + "' and update_time <='" + update_time_end + "'";
        }
        if (!(Objects.isNull(create_time_start) && Objects.isNull(create_time_end))) {
            condition += " and create_time >= '" + create_time_start + "' and create_time <= '" + create_time_end + "'";
        }
        return condition;

    }
}
