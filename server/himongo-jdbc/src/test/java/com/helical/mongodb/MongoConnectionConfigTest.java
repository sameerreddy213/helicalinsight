package com.helical.mongodb;

import org.junit.Test;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class MongoConnectionConfigTest {
    @Test
    public void metadataPatternsTreatIdentifiersLiterally() {
        assertTrue(MongoDatabaseMetaData.matches("audit[1]", "audit[1]"));
        assertTrue(MongoDatabaseMetaData.matches("test_customers", "test\\_customers"));
        assertFalse(MongoDatabaseMetaData.matches("testXcustomers", "test\\_customers"));
        assertTrue(MongoDatabaseMetaData.matches("customers", "cust%"));
    }

    @Test
    public void preservesUriTimeouts() throws Exception {
        MongoConnection.ConnectionConfig config = MongoConnection.ConnectionConfig.parse(
                "mongodb://localhost/demo?connectTimeoutMS=1200&socketTimeoutMS=2300&serverSelectionTimeoutMS=3400", new Properties());
        assertEquals(1200, config.settings.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS));
        assertEquals(2300, config.settings.getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS));
        assertEquals(3400, config.settings.getClusterSettings().getServerSelectionTimeout(TimeUnit.MILLISECONDS));
    }

    @Test
    public void explicitPropertiesOverrideUriOptions() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("connectTimeoutMS", "900");
        MongoConnection.ConnectionConfig config = MongoConnection.ConnectionConfig.parse(
                "mongodb://localhost/demo?connectTimeoutMS=1200", properties);
        assertEquals(900, config.settings.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS));
    }

    @Test
    public void usesSeparateCredentialsAndRedactsUriCredentials() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("user", "reader");
        properties.setProperty("password", "secret");
        MongoConnection.ConnectionConfig config = MongoConnection.ConnectionConfig.parse(
                "mongodb://localhost/demo?authSource=users", properties);
        assertEquals("users", config.settings.getCredential().getSource());
        config = MongoConnection.ConnectionConfig.parse("mongodb://reader:secret@localhost/demo", new Properties());
        assertFalse(config.displayUrl.contains("secret"));
        assertFalse(config.displayUrl.contains("reader"));
        assertThrows(SQLException.class, () -> MongoConnection.ConnectionConfig.parse(
                "mongodb://reader:secret@localhost/demo", properties));
    }

    @Test
    public void requiresDatabaseAndLeavesOtherDriversAlone() throws Exception {
        assertThrows(SQLException.class, () -> MongoConnection.ConnectionConfig.parse("mongodb://localhost", new Properties()));
        assertNull(new MongoJdbcDriver().connect("jdbc:postgresql://localhost/demo", new Properties()));
        assertFalse(new MongoJdbcDriver().acceptsURL(null));
    }
}
