package com.miya.common.config.orm;

import com.miya.common.exception.DataTooLongException;
import org.hibernate.JDBCException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.tool.schema.internal.StandardForeignKeyExporter;
import org.hibernate.tool.schema.spi.Exporter;
import org.hibernate.type.StandardBasicTypes;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     */
    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String foreignKeyDefinition) {
        return "";
    }

    /**
     * 该系统不使用外键约束，DDL中不生成外键
     * @param constraintName
     * @param foreignKey
     * @param referencedTable
     * @param primaryKey
     * @param referencesPrimaryKey
     */
    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return "";
    }

    NoForeignKeyExporter foreignKeyExporter = new NoForeignKeyExporter(this);

    @Override
    public Exporter<ForeignKey> getForeignKeyExporter() {
        return this.foreignKeyExporter;
    }

    @Override
    public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {

        SQLExceptionConversionDelegate conversionDelegate = super.buildSQLExceptionConversionDelegate();
        return new SQLExceptionConversionDelegate() {
            final Pattern fieldPattern = Pattern.compile("column '(\\w+)'");

            @Override
            public JDBCException convert(SQLException sqlException, String message, String sql) {
                switch ( sqlException.getErrorCode() ) {
                    case 1406:
                        // 数据输入太长
                        Matcher matcher = fieldPattern.matcher(sqlException.getMessage());
                        String filedName = matcher.find()?matcher.group(1):null;
                        return new DataTooLongException(sqlException.getMessage(), sqlException, filedName);
                }
                return conversionDelegate.convert(sqlException, message, sql);
            }
        };

    }

    /**
     * 屏蔽hibernate生成外键
     */
    public static class NoForeignKeyExporter extends StandardForeignKeyExporter {

        public NoForeignKeyExporter(Dialect dialect) {
            super(dialect);
        }

        @Override
        public String[] getSqlDropStrings(ForeignKey foreignKey, Metadata metadata) {
            return new String[]{""};
        }

        @Override
        public String[] getSqlDropStrings(ForeignKey foreignKey, Metadata metadata, SqlStringGenerationContext context) {
            return NO_COMMANDS;
        }

    }

}
