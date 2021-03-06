package excel.accounting.db;

import javax.persistence.TemporalType;
import java.sql.SQLType;

/**
 * Sql Column
 */
public class SqlColumn {
    private String fieldName, name;
    private Class<?> type;
    private boolean primaryKey, autoIncrement, nullable;
    private int length;
    private SqlTable joinTable;
    private TemporalType temporalType;
    private int colIndex;
    private SQLType sqlType;

    public SqlColumn(String name, Class<?> type) {
        this.name = name;
        this.type = type;
        nullable = true;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setJoinTable(SqlTable joinTable) {
        this.joinTable = joinTable;
    }

    public SqlTable getJoinTable() {
        return joinTable;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public int getLength() {
        return length;
    }

    public TemporalType getTemporalType() {
        return temporalType;
    }

    public void setTemporalType(TemporalType temporalType) {
        this.temporalType = temporalType;
    }

    public int getColIndex() {
        return colIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public SQLType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SQLType sqlType) {
        this.sqlType = sqlType;
    }

    public boolean isEnumClass() {
        return Enum.class.isAssignableFrom(type);
    }

    @Override
    public String toString() {
        return name + " \t " + type.getName();
    }
}
