package com.helical.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * JDBC {@link Connection} backed by a MongoDB {@link MongoClient}.
 */
final class MongoConnection implements Connection {

    static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;
    static final int DEFAULT_SERVER_SELECTION_TIMEOUT_MS = 5_000;
    static final int DEFAULT_SOCKET_TIMEOUT_MS = 10_000;
    static final int DEFAULT_SAMPLE_SIZE = 50;

    private final MongoClient client;
    private final String databaseName;
    private final String jdbcUrl;
    private final int sampleSize;
    private final int defaultQueryLimit;
    private boolean closed;
    private boolean readOnly = true;
    private boolean autoCommit = true;
    private String catalog;

    private MongoConnection(MongoClient client, String databaseName, String jdbcUrl, int sampleSize,
            int defaultQueryLimit) {
        this.client = client;
        this.databaseName = databaseName;
        this.jdbcUrl = jdbcUrl;
        this.sampleSize = sampleSize;
        this.defaultQueryLimit = defaultQueryLimit;
        this.catalog = databaseName;
    }

    static MongoConnection open(String url, Properties info) throws SQLException {
        try {
            ConnectionConfig config = ConnectionConfig.parse(url, info);
            MongoClient client = MongoClients.create(config.settings);
            MongoConnection connection = new MongoConnection(client, config.databaseName, config.displayUrl,
                    config.sampleSize, config.defaultQueryLimit);
            connection.verifyConnectivity();
            return connection;
        } catch (SQLException e) {
            throw sanitize(e);
        } catch (MongoSecurityException e) {
            throw sanitize(new SQLException("MongoDB authentication failed. Check username, password, and authSource.",
                    e));
        } catch (MongoTimeoutException e) {
            throw sanitize(new SQLException(
                    "MongoDB server unreachable or timed out within the configured selection timeout.", e));
        } catch (RuntimeException e) {
            throw sanitize(new SQLException("Failed to connect to MongoDB: " + safeMessage(e), e));
        }
    }

    private void verifyConnectivity() throws SQLException {
        try {
            MongoDatabase database = client.getDatabase(databaseName);
            database.runCommand(new Document("ping", 1));
        } catch (MongoSecurityException e) {
            closeQuietly();
            throw sanitize(new SQLException("MongoDB authentication failed. Check username, password, and authSource.",
                    e));
        } catch (MongoTimeoutException e) {
            closeQuietly();
            throw sanitize(new SQLException(
                    "MongoDB server unreachable or timed out within the configured selection timeout.", e));
        } catch (RuntimeException e) {
            closeQuietly();
            throw sanitize(new SQLException("MongoDB connectivity check failed: " + safeMessage(e), e));
        }
    }

    MongoDatabase getMongoDatabase() throws SQLException {
        checkOpen();
        return client.getDatabase(databaseName);
    }

    MongoClient getClient() throws SQLException {
        checkOpen();
        return client;
    }

    String getDatabaseName() {
        return databaseName;
    }

    int getSampleSize() {
        return sampleSize;
    }

    int getDefaultQueryLimit() {
        return defaultQueryLimit;
    }

    String getJdbcUrl() {
        return jdbcUrl;
    }

    static String normalizeUrl(String url) {
        String trimmed = url.trim();
        if (trimmed.regionMatches(true, 0, "jdbc:", 0, 5)) {
            return trimmed.substring(5);
        }
        return trimmed;
    }

    static SQLException sanitize(SQLException exception) {
        return new SQLException(safeMessage(exception), exception.getSQLState(), exception.getErrorCode());
    }

    static String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return "Unknown MongoDB error";
        }
        String message = throwable.getMessage();
        // Avoid leaking credentials that may appear in URI-shaped messages.
        return message.replaceAll("(?i)(mongodb(?:\\+srv)?://)([^/@\\s]+):([^/@\\s]+)@", "$1****:****@")
                .replaceAll("(?i)password=[^&,\\s]+", "password=****");
    }

    private void closeQuietly() {
        try {
            client.close();
        } catch (RuntimeException ignored) {
            // ignore
        }
        closed = true;
    }

    private void checkOpen() throws SQLException {
        if (closed) {
            throw new SQLException("Connection is closed");
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        checkOpen();
        return new MongoStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkOpen();
        return new MongoPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatement is not supported for MongoDB");
    }

    @Override
    public String nativeSQL(String sql) {
        return sql;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override
    public boolean getAutoCommit() {
        return autoCommit;
    }

    @Override
    public void commit() throws SQLException {
        checkOpen();
    }

    @Override
    public void rollback() throws SQLException {
        checkOpen();
    }

    @Override
    public void close() {
        if (!closed) {
            client.close();
            closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkOpen();
        return new MongoDatabaseMetaData(this);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        if (catalog != null && !catalog.isEmpty() && !databaseName.equals(catalog)) {
            throw new SQLException("Changing the MongoDB database requires a new connection");
        }
        this.catalog = databaseName;
    }

    @Override
    public String getCatalog() {
        return catalog;
    }

    @Override
    public void setTransactionIsolation(int level) {
        // MongoDB transactions are not exposed via this adapter.
    }

    @Override
    public int getTransactionIsolation() {
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public SQLWarning getWarnings() {
        return null;
    }

    @Override
    public void clearWarnings() {
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatement is not supported for MongoDB");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new SQLFeatureNotSupportedException("Type maps are not supported");
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Type maps are not supported");
    }

    @Override
    public void setHoldability(int holdability) {
    }

    @Override
    public int getHoldability() {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints are not supported");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints are not supported");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints are not supported");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints are not supported");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatement is not supported for MongoDB");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob is not supported");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob is not supported");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob is not supported");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML is not supported");
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        if (closed) {
            return false;
        }
        try {
            client.getDatabase(databaseName).runCommand(new Document("ping", 1));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public void setClientInfo(String name, String value) {
    }

    @Override
    public void setClientInfo(Properties properties) {
    }

    @Override
    public String getClientInfo(String name) {
        return null;
    }

    @Override
    public Properties getClientInfo() {
        return new Properties();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException("Array is not supported");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Struct is not supported");
    }

    @Override
    public void setSchema(String schema) {
    }

    @Override
    public String getSchema() {
        return null;
    }

    @Override
    public void abort(Executor executor) {
        close();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) {
    }

    @Override
    public int getNetworkTimeout() {
        return DEFAULT_SOCKET_TIMEOUT_MS;
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

    /**
     * Parsed connection settings with a single credential source of truth.
     */
    static final class ConnectionConfig {
        final MongoClientSettings settings;
        final String databaseName;
        final String displayUrl;
        final int sampleSize;
        final int defaultQueryLimit;

        private ConnectionConfig(MongoClientSettings settings, String databaseName, String displayUrl, int sampleSize,
                int defaultQueryLimit) {
            this.settings = settings;
            this.databaseName = databaseName;
            this.displayUrl = displayUrl;
            this.sampleSize = sampleSize;
            this.defaultQueryLimit = defaultQueryLimit;
        }

        static ConnectionConfig parse(String url, Properties info) throws SQLException {
            String normalized = normalizeUrl(url);
            if (!(normalized.startsWith("mongodb://") || normalized.startsWith("mongodb+srv://"))) {
                throw new SQLException("Unsupported MongoDB JDBC URL. Expected mongodb:// or mongodb+srv://");
            }

            String user = firstNonBlank(info.getProperty("user"), info.getProperty("username"),
                    info.getProperty("userName"));
            String password = info.getProperty("password");
            String propertyDatabase = firstNonBlank(info.getProperty("database"), info.getProperty("databaseName"));
            String authSource = firstNonBlank(info.getProperty("authSource"), info.getProperty("authSourceDatabase"));
            int sampleSize = parsePositiveInt(info.getProperty("sampleSize"), DEFAULT_SAMPLE_SIZE);
            int defaultQueryLimit = parsePositiveInt(info.getProperty("defaultQueryLimit"),
                    MongoSqlExecutor.DEFAULT_QUERY_ROW_CAP);
            int connectTimeout = parsePositiveInt(info.getProperty("connectTimeoutMS"), DEFAULT_CONNECT_TIMEOUT_MS);
            int serverSelectionTimeout = parsePositiveInt(info.getProperty("serverSelectionTimeoutMS"),
                    DEFAULT_SERVER_SELECTION_TIMEOUT_MS);
            int socketTimeout = parsePositiveInt(info.getProperty("socketTimeoutMS"), DEFAULT_SOCKET_TIMEOUT_MS);

            ConnectionString connectionString;
            try {
                connectionString = new ConnectionString(normalized);
            } catch (IllegalArgumentException e) {
                throw new SQLException("Malformed MongoDB URI: " + safeMessage(e), e);
            }

            boolean uriHasCredentials = connectionString.getCredential() != null;
            boolean propsHaveCredentials = user != null && !user.isEmpty();
            if (uriHasCredentials && propsHaveCredentials) {
                throw new SQLException(
                        "Ambiguous MongoDB credentials: supply credentials either in the URI or as user/password properties, not both.");
            }

            String databaseName = connectionString.getDatabase();
            if (databaseName == null || databaseName.isEmpty()) {
                databaseName = propertyDatabase;
            } else if (propertyDatabase != null && !propertyDatabase.isEmpty()
                    && !propertyDatabase.equals(databaseName)) {
                throw new SQLException(
                        "Ambiguous MongoDB database: URI path database and database property differ. Use only one.");
            }
            if (databaseName == null || databaseName.isEmpty()) {
                throw new SQLException("MongoDB database name is required in the URI path or as the database property.");
            }

            MongoClientSettings.Builder builder = MongoClientSettings.builder()
                    .applyToClusterSettings(b -> b.serverSelectionTimeout(serverSelectionTimeout, TimeUnit.MILLISECONDS))
                    .applyToSocketSettings(b -> b.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                            .readTimeout(socketTimeout, TimeUnit.MILLISECONDS))
                    .applyConnectionString(connectionString);
            // Explicit JDBC properties override URI options; defaults do not.
            if (info.getProperty("serverSelectionTimeoutMS") != null) {
                builder.applyToClusterSettings(b -> b.serverSelectionTimeout(serverSelectionTimeout, TimeUnit.MILLISECONDS));
            }
            if (info.getProperty("connectTimeoutMS") != null) {
                builder.applyToSocketSettings(b -> b.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS));
            }
            if (info.getProperty("socketTimeoutMS") != null) {
                builder.applyToSocketSettings(b -> b.readTimeout(socketTimeout, TimeUnit.MILLISECONDS));
            }

            if (propsHaveCredentials) {
                String source = authSource;
                if (source == null || source.isEmpty()) {
                    source = queryParam(normalized, "authSource");
                }
                if (source == null || source.isEmpty()) {
                    source = "admin";
                }
                char[] pwd = password == null ? new char[0] : password.toCharArray();
                builder.credential(MongoCredential.createCredential(user, source, pwd));
            }

            String displayUrl = redactedUrl(normalized);
            return new ConnectionConfig(builder.build(), databaseName, displayUrl, sampleSize, defaultQueryLimit);
        }

        private static String queryParam(String mongoUrl, String key) {
            int q = mongoUrl.indexOf('?');
            if (q < 0 || q == mongoUrl.length() - 1) {
                return null;
            }
            String[] pairs = mongoUrl.substring(q + 1).split("&");
            for (String pair : pairs) {
                int eq = pair.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                if (key.equalsIgnoreCase(pair.substring(0, eq))) {
                    return pair.substring(eq + 1);
                }
            }
            return null;
        }

        private static String redactedUrl(String url) {
            return url.replaceAll("(?i)(mongodb(?:\\+srv)?://)([^/@]+):([^/@]+)@", "$1****:****@");
        }

        private static String firstNonBlank(String... values) {
            if (values == null) {
                return null;
            }
            for (String value : values) {
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
            return null;
        }

        private static int parsePositiveInt(String value, int defaultValue) throws SQLException {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                int parsed = Integer.parseInt(value.trim());
                if (parsed <= 0) {
                    throw new SQLException("Timeout/sample values must be positive integers");
                }
                return parsed;
            } catch (NumberFormatException e) {
                throw new SQLException("Invalid numeric connection property: " + value);
            }
        }
    }
}
