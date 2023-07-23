package com.miya.common.config.orm;

import com.miya.common.exception.DataDuplicateException;
import com.miya.common.exception.DataTooLongException;
import org.hibernate.JDBCException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.tool.schema.internal.StandardForeignKeyExporter;
import org.hibernate.tool.schema.internal.TableMigrator;
import org.hibernate.tool.schema.spi.Exporter;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import java.sql.SQLException;
import java.sql.Types;
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
    public TableMigrator getTableMigrator() {
        return super.getTableMigrator();
    }

    private final SizeStrategy sizeStrategy = new SizeStrategyImpl() {
        @Override
        public Size resolveSize(
                JdbcType jdbcType,
                JavaType<?> javaType,
                Integer precision,
                Integer scale,
                Long length) {
            final Size size = new Size();
            switch ( jdbcType.getDdlTypeCode() ) {
                case Types.BIT:
                    // MySQL allows BIT with a length up to 64 (less the default length 255)
                    if ( length != null ) {
                        return Size.length( Math.min( Math.max( length, 1 ), 64 ) );
                    }
                    break;
                case SqlTypes.TIME:
                case SqlTypes.TIME_WITH_TIMEZONE:
                case SqlTypes.TIME_UTC:
                case SqlTypes.TIMESTAMP:
                case SqlTypes.TIMESTAMP_WITH_TIMEZONE:
                case SqlTypes.TIMESTAMP_UTC:
                    size.setPrecision( javaType.getDefaultSqlPrecision( ExtensionMySQLDialect.this, jdbcType ) );
//                    处理数据库列大小和模型中不一致的问题 mysql版本大于"5.6.4"都这么计算length
//                    mysql8.0驱动中 {@see com.mysql.cj.jdbc.DatabaseMetaDataUsingInfoSchema#158}
                    if (size.getPrecision() > 0) {
                        length = 19 + size.getPrecision() + 1L;
                    }else {
                        length = 19L;
                    }
                    if ( scale != null && scale != 0 ) {
                        throw new IllegalArgumentException("scale has no meaning for timestamps");
                    }
                    break;
                default:
                    return super.resolveSize( jdbcType, javaType, precision, scale, length );
            }
            if ( length != null ) {
                size.setLength( length );
            }
            return size;
        }
    };

    @Override
    public SizeStrategy getSizeStrategy() {
        return sizeStrategy;
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
