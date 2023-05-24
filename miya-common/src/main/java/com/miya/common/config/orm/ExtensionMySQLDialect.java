package com.miya.common.config.orm;

import com.miya.common.exception.DataDuplicateException;
import com.miya.common.exception.DataTooLongException;
import org.hibernate.JDBCException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.tool.schema.internal.StandardForeignKeyExporter;
import org.hibernate.tool.schema.spi.Exporter;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 扩展原有的dialect，使得支持更多的mysql特性
 */
public class ExtensionMySQLDialect extends MySQLDialect {

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        SqmFunctionRegistry functionRegistry = functionContributions.getFunctionRegistry();
        functionRegistry.registerPattern("regexp", "?1 REGEXP ?2");
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
            // Data truncation: Data too long for column 'username' at row 1
            final Pattern dataTooLongPattern = Pattern.compile("column '(\\w+)'");
            // Duplicate entry 'admin' for key 'sys_user.username'
            final Pattern dataDuplicatePattern = Pattern.compile("Duplicate entry '(.+?)' for key '.+\\.(.+?)'");

            @Override
            public JDBCException convert(SQLException sqlException, String message, String sql) {
                switch (sqlException.getErrorCode()) {
                    case 1406 -> {
                        // 数据输入太长
                        Matcher matcher = dataTooLongPattern.matcher(sqlException.getMessage());
                        String filedName = matcher.find() ? matcher.group(1) : null;
                        return new DataTooLongException(sqlException.getMessage(), sqlException, filedName);
                    }
                    case 1062 -> {
                        // 数据重复，违反唯一约束
                        Matcher matcher2 = dataDuplicatePattern.matcher(sqlException.getMessage());
                        if (matcher2.find()) {
                            String filedName2 = matcher2.group(2);
                            return new DataDuplicateException(sqlException.getMessage(), sqlException, filedName2, matcher2.group(1));
                        }
                        return new DataDuplicateException(sqlException.getMessage(), sqlException, null, null);
                    }
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
        public String[] getSqlCreateStrings(ForeignKey foreignKey, Metadata metadata, SqlStringGenerationContext context) {
            return NO_COMMANDS;
        }

        @Override
        public String[] getSqlDropStrings(ForeignKey foreignKey, Metadata metadata, SqlStringGenerationContext context) {
            return NO_COMMANDS;
        }

    }

}
