package com.helical.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests against a local MongoDB instance.
 * <p>
 * Requires {@code HIMONGO_IT=true} (or {@code -Dhimongo.it=true}). When enabled,
 * connection failures fail the suite (they are not skipped).
 * Uses an isolated collection {@code himongo_it_customers} so demo {@code customers}
 * seed data is left untouched.
 */
public class MongoJdbcDriverIT {

    private static final String DEFAULT_URI =
            "mongodb://hi_mongo:hi_mongo_dev@127.0.0.1:27017/hi_demo?authSource=admin";
    private static final String IT_COLLECTION = "himongo_it_customers";

    private static String uri;
    private static boolean itEnabled;

    @BeforeClass
    public static void setUpClass() throws Exception {
        itEnabled = "true".equalsIgnoreCase(System.getenv("HIMONGO_IT"))
                || "true".equalsIgnoreCase(System.getProperty("himongo.it"));
        if (!itEnabled) {
            // JUnit 4: skip remaining tests without pretending Mongo was reached.
            org.junit.Assume.assumeTrue(
                    "Set HIMONGO_IT=true (and start Mongo) to run MongoJdbcDriverIT", false);
        }

        uri = System.getenv().getOrDefault("MONGO_URI",
                System.getProperty("mongo.uri", DEFAULT_URI));

        try {
            Class.forName("com.helical.mongodb.MongoJdbcDriver");
        } catch (ClassNotFoundException e) {
            fail("MongoJdbcDriver not on classpath: " + e.getMessage());
        }

        try (MongoClient client = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToClusterSettings(b -> b.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .applyToSocketSettings(b -> b.connectTimeout(5, TimeUnit.SECONDS))
                .build())) {
            MongoDatabase db = client.getDatabase(new ConnectionString(uri).getDatabase());
            db.runCommand(new Document("ping", 1));
            MongoCollection<Document> collection = db.getCollection(IT_COLLECTION);
            collection.drop();
            collection.insertMany(Arrays.asList(
                    new Document("_id", new ObjectId())
                            .append("name", "Ada Lovelace")
                            .append("email", "ada@example.com")
                            .append("createdAt", new Date())
                            .append("address", new Document("city", "London").append("zip", "SW1A"))
                            .append("tags", Arrays.asList("math", "computing"))
                            .append("active", true)
                            .append("score", 100),
                    new Document("_id", new ObjectId())
                            .append("name", "Grace Hopper")
                            .append("email", null)
                            .append("score", 99)
                            .append("active", false)
            ));
        } catch (RuntimeException e) {
            fail("Integration MongoDB is not reachable: " + MongoConnection.safeMessage(e));
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (!itEnabled || uri == null) {
            return;
        }
        try (MongoClient client = MongoClients.create(uri)) {
            client.getDatabase(new ConnectionString(uri).getDatabase()).getCollection(IT_COLLECTION).drop();
        } catch (RuntimeException ignored) {
            // Best-effort cleanup; do not mask test failures.
        }
    }

    @Test
    public void connectsAndReadsSeededData() throws Exception {
        String sql = "SELECT name, address.city, email FROM " + IT_COLLECTION + " LIMIT 10";
        try (Connection connection = DriverManager.getConnection(uri);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals("Ada Lovelace", rs.getString("name"));
            assertEquals("London", rs.getString("address.city"));
            assertEquals("ada@example.com", rs.getString("email"));
            assertTrue(rs.next());
            assertEquals("Grace Hopper", rs.getString("name"));
            assertNull(rs.getString("address.city"));
            assertTrue(rs.wasNull());
            assertFalse(rs.next());
        }
    }

    @Test
    public void selectOneValidationQueryWorks() throws Exception {
        try (Connection connection = DriverManager.getConnection(uri);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT 1")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    public void acceptsHelicalInsightViewPreviewWrapper() throws Exception {
        String wrapped = "select * from (SELECT name, email, score FROM " + IT_COLLECTION
                + " LIMIT 3) foo limit 10";
        try (Connection connection = DriverManager.getConnection(uri);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(wrapped)) {
            assertTrue(rs.next());
            assertEquals("Ada Lovelace", rs.getString("name"));
            assertEquals("ada@example.com", rs.getString("email"));
            assertEquals(100, rs.getInt("score"));
        }
    }

    @Test
    public void defaultCapAppliesWhenLimitOmitted() throws Exception {
        String sql = "SELECT name FROM " + IT_COLLECTION;
        try (Connection connection = DriverManager.getConnection(uri);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            assertTrue(rs.next());
            SQLWarning warning = statement.getWarnings();
            assertNotNull("Expected warning when LIMIT is omitted", warning);
            assertTrue(warning.getMessage().toLowerCase().contains("limit")
                    || warning.getMessage().contains(String.valueOf(MongoSqlExecutor.DEFAULT_QUERY_ROW_CAP)));
        }
    }

    @Test
    public void metadataListsCollectionsAndColumns() throws Exception {
        try (Connection connection = DriverManager.getConnection(uri)) {
            DatabaseMetaData metaData = connection.getMetaData();
            boolean foundCollection = false;
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    if (IT_COLLECTION.equals(tables.getString("TABLE_NAME"))) {
                        foundCollection = true;
                    }
                }
            }
            assertTrue(foundCollection);

            boolean foundCity = false;
            try (ResultSet columns = metaData.getColumns(null, null, IT_COLLECTION, "%")) {
                while (columns.next()) {
                    if ("address.city".equals(columns.getString("COLUMN_NAME"))) {
                        foundCity = true;
                    }
                }
            }
            assertTrue(foundCity);
        }
    }

    @Test
    public void rejectsInvalidCredentialsWithSanitizedError() {
        String badUri = uri.replaceFirst("://[^@]+@", "://hi_mongo:wrong_password@");
        long start = System.currentTimeMillis();
        try {
            DriverManager.getConnection(badUri);
            fail("Expected authentication failure");
        } catch (SQLException e) {
            assertTrue(e.getMessage().toLowerCase().contains("auth")
                    || e.getMessage().toLowerCase().contains("credential")
                    || e.getMessage().toLowerCase().contains("failed"));
            assertFalse(e.getMessage().contains("wrong_password"));
        }
        assertTrue("Auth failure should be bounded", System.currentTimeMillis() - start < 20_000);
    }

    @Test
    public void rejectsMalformedUri() {
        try {
            DriverManager.getConnection("mongodb://");
            fail("Expected malformed URI failure");
        } catch (SQLException e) {
            assertTrue(e.getMessage().toLowerCase().contains("malformed")
                    || e.getMessage().toLowerCase().contains("invalid")
                    || e.getMessage().toLowerCase().contains("must"));
        }
    }

    @Test
    public void unreachableHostFailsWithinTimeout() {
        Properties props = new Properties();
        props.setProperty("serverSelectionTimeoutMS", "2000");
        props.setProperty("connectTimeoutMS", "2000");
        long start = System.currentTimeMillis();
        try {
            DriverManager.getConnection("mongodb://127.0.0.1:1/hi_demo", props);
            fail("Expected timeout");
        } catch (SQLException e) {
            assertTrue(e.getMessage().toLowerCase().contains("timeout")
                    || e.getMessage().toLowerCase().contains("unreachable")
                    || e.getMessage().toLowerCase().contains("failed"));
        }
        assertTrue(System.currentTimeMillis() - start < 10_000);
    }

    @Test
    public void rejectsAmbiguousCredentialSources() {
        Properties props = new Properties();
        props.setProperty("user", "hi_mongo");
        props.setProperty("password", "hi_mongo_dev");
        try {
            DriverManager.getConnection(
                    "mongodb://hi_mongo:hi_mongo_dev@127.0.0.1:27017/hi_demo?authSource=admin", props);
            fail("Expected ambiguity error");
        } catch (SQLException e) {
            assertTrue(e.getMessage().toLowerCase().contains("ambiguous"));
        }
    }
    @Test
    public void aliasesAndEmptyResultsKeepColumnMetadata() throws Exception {
        try (Connection connection = DriverManager.getConnection(uri);
             Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SELECT name AS customer FROM " + IT_COLLECTION + " LIMIT 1")) {
                assertTrue(result.next());
                assertEquals("Ada Lovelace", result.getString("customer"));
            }
            try (ResultSet result = statement.executeQuery("SELECT * FROM " + IT_COLLECTION + " LIMIT 0")) {
                assertFalse(result.next());
                assertTrue(result.findColumn("name") > 0);
            }
        }
    }

    @Test
    public void maxRowsAndDefaultCapActuallyBoundResults() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("defaultQueryLimit", "1");
        try (Connection connection = DriverManager.getConnection(uri, properties);
             Statement statement = connection.createStatement()) {
            ResultSet first = statement.executeQuery("SELECT name FROM " + IT_COLLECTION);
            assertTrue(first.next());
            assertFalse(first.next());
            assertNotNull(statement.getWarnings());
            statement.setMaxRows(1);
            try (ResultSet second = statement.executeQuery("SELECT name FROM " + IT_COLLECTION + " LIMIT 10")) {
                assertTrue(first.isClosed());
                assertTrue(second.next());
                assertFalse(second.next());
                assertNull(statement.getWarnings());
            }
        }
    }

}
