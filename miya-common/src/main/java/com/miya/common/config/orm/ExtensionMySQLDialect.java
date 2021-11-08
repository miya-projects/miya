package com.miya.common.config.orm;

import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * 扩展原有的dialect，使得支持更多的mysql特性
 */
public class ExtensionMySQLDialect extends MySQL57Dialect {

    public ExtensionMySQLDialect() {
        super();
//        registerKeyword("regexp");
        registerFunction("regexp", new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "?1 REGEXP ?2"));
    }

    /**
     * 该系统不使用外键约束，DDL中不生成外键
     * @param constraintName
     * @param foreignKeyDefinition
     * @return
     */
    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String foreignKeyDefinition) {
        // return super.getAddForeignKeyConstraintString(constraintName, foreignKeyDefinition);
        return "";
    }

    /**
     * 该系统不使用外键约束，DDL中不生成外键
     * @param constraintName
     * @param foreignKey
     * @param referencedTable
     * @param primaryKey
     * @param referencesPrimaryKey
     * @return
     */
    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        // return super.getAddForeignKeyConstraintString(constraintName, foreignKey, referencedTable, primaryKey, referencesPrimaryKey);
        return "";
    }
}
