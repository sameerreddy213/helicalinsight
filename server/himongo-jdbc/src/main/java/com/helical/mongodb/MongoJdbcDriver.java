package com.helical.mongodb;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * JDBC entry point expected by Helical Insight ({@code com.helical.mongodb.MongoJdbcDriver}).
 * Accepts native MongoDB URIs ({@code mongodb://}, {@code mongodb+srv://}) and optional
 * {@code jdbc:} prefixes used by some tooling.
 */
public class MongoJdbcDriver implements Driver {

    static {
        try {
            DriverManager.registerDriver(new MongoJdbcDriver());
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        return MongoConnection.open(url, info == null ? new Properties() : info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            return false;
        }
        String normalized = MongoConnection.normalizeUrl(url);
        return normalized.startsWith("mongodb://") || normalized.startsWith("mongodb+srv://");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        DriverPropertyInfo user = new DriverPropertyInfo("user", info == null ? null : info.getProperty("user"));
        user.description = "MongoDB username";
        DriverPropertyInfo password = new DriverPropertyInfo("password", null);
        password.description = "MongoDB password";
        DriverPropertyInfo database = new DriverPropertyInfo("database",
                info == null ? null : info.getProperty("database"));
        database.description = "Target database name (required when omitted from the URI path)";
        DriverPropertyInfo authSource = new DriverPropertyInfo("authSource",
                info == null ? null : info.getProperty("authSource"));
        authSource.description = "Authentication database (defaults to admin when credentials are supplied)";
        return new DriverPropertyInfo[]{user, password, database, authSource};
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("java.util.logging is not used by himongo-jdbc");
    }
}
