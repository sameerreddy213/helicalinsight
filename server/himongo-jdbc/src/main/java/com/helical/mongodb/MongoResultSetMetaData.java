package com.helical.mongodb;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

final class MongoResultSetMetaData implements ResultSetMetaData {

    private final List<String> columnNames;
    private final Map<String, Integer> columnTypes;

    MongoResultSetMetaData(List<String> columnNames, Map<String, Integer> columnTypes) {
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public boolean isAutoIncrement(int column) {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) {
        return true;
    }

    @Override
    public boolean isSearchable(int column) {
        return true;
    }

    @Override
    public boolean isCurrency(int column) {
        return false;
    }

    @Override
    public int isNullable(int column) {
        return ResultSetMetaData.columnNullable;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        int type = getColumnType(column);
        return type == Types.INTEGER || type == Types.BIGINT || type == Types.DOUBLE || type == Types.DECIMAL;
    }

    @Override
    public int getColumnDisplaySize(int column) {
        return 64;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return getColumnName(column);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return columnNames.get(index(column));
    }

    @Override
    public String getSchemaName(int column) {
        return "";
    }

    @Override
    public int getPrecision(int column) {
        return 0;
    }

    @Override
    public int getScale(int column) {
        return 0;
    }

    @Override
    public String getTableName(int column) {
        return "";
    }

    @Override
    public String getCatalogName(int column) {
        return "";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        String name = getColumnName(column);
        return columnTypes.getOrDefault(name, Types.VARCHAR);
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return MongoDocumentConverter.typeName(getColumnType(column));
    }

    @Override
    public boolean isReadOnly(int column) {
        return true;
    }

    @Override
    public boolean isWritable(int column) {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        switch (getColumnType(column)) {
            case Types.INTEGER:
                return Integer.class.getName();
            case Types.BIGINT:
                return Long.class.getName();
            case Types.DOUBLE:
                return Double.class.getName();
            case Types.BOOLEAN:
                return Boolean.class.getName();
            case Types.TIMESTAMP:
                return java.sql.Timestamp.class.getName();
            default:
                return String.class.getName();
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("Not a wrapper for " + iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    private int index(int column) throws SQLException {
        if (column < 1 || column > columnNames.size()) {
            throw new SQLException("Invalid column index: " + column);
        }
        return column - 1;
    }
}
