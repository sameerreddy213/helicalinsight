package com.helical.mongodb;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * PreparedStatement that supports only parameter-free SQL for the MongoDB adapter.
 * Helical Insight validation and metadata paths use plain statements; parameterized
 * SQL beyond {@code SELECT 1} is rejected rather than silently misinterpreted.
 */
final class MongoPreparedStatement extends MongoStatement implements PreparedStatement {

    private final String sql;

    MongoPreparedStatement(MongoConnection connection, String sql) {
        super(connection);
        this.sql = sql;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return executeQuery(sql);
    }

    @Override
    public int executeUpdate() throws SQLException {
        return executeUpdate(sql);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void clearParameters() {
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public boolean execute() throws SQLException {
        return execute(sql);
    }

    @Override
    public void addBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch updates are not supported");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("PreparedStatement metadata is not pre-computed");
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("Parameter metadata is not supported");
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw unsupportedParameters();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw unsupportedParameters();
    }

    private static SQLException unsupportedParameters() {
        return new SQLFeatureNotSupportedException(
                "Parameterized SQL is not supported by the MongoDB adapter. Use literal SQL forms.");
    }
}
