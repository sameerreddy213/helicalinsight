package com.helical.mongodb;

import org.junit.Test;
import java.sql.SQLException;
import static org.junit.Assert.*;

public class MongoSqlParserTest {
    @Test
    public void readsPlainProjectionLists() throws Exception {
        MongoSqlParser.Query query = MongoSqlParser.parse("SELECT name, email, score FROM customers LIMIT 3");
        assertEquals(3, query.columns.size());
        assertEquals("email", query.columns.get(1).label);
    }

    @Test
    public void keepsAliasesAndNestedFields() throws Exception {
        MongoSqlParser.Query query = MongoSqlParser.parse(
                "SELECT name AS customer, address.city city FROM hi_demo.customers LIMIT 3;");
        assertEquals("hi_demo.customers", query.collection);
        assertEquals("customer", query.columns.get(0).label);
        assertEquals("address.city", query.columns.get(1).field);
        assertEquals("city", query.columns.get(1).label);
        assertEquals(Integer.valueOf(3), query.limit);
    }

    @Test
    public void stringsCanContainSqlWordsAndEscapedQuotes() throws Exception {
        MongoSqlParser.Query query = MongoSqlParser.parse(
                "SELECT * FROM customers WHERE name = 'O''Brien AND Sons LIMIT 5' LIMIT 2");
        assertEquals("O'Brien AND Sons LIMIT 5", query.filter.get("name"));
        assertEquals(Integer.valueOf(2), query.limit);
    }

    @Test
    public void unwrapsPreviewAndUsesSmallestLimit() throws Exception {
        MongoSqlParser.Query query = MongoSqlParser.parse(
                "select * from (select * from (SELECT name FROM customers LIMIT 5) a LIMIT 2) foo LIMIT 10");
        assertEquals("customers", query.collection);
        assertEquals(Integer.valueOf(2), query.limit);
    }

    @Test
    public void allowsZeroAndQuotedIdentifiers() throws Exception {
        MongoSqlParser.Query query = MongoSqlParser.parse("SELECT `name` AS \"Customer name\" FROM \"audit.events\" LIMIT 0");
        assertEquals("Customer name", query.columns.get(0).label);
        assertEquals("audit.events", query.collection);
        assertEquals(Integer.valueOf(0), query.limit);
    }

    @Test
    public void readsNumericAndBooleanFilters() throws Exception {
        assertEquals(-12.5, MongoSqlParser.parse("SELECT * FROM customers WHERE score = -12.5").filter.get("score"));
        assertEquals(true, MongoSqlParser.parse("SELECT * FROM customers WHERE active = true").filter.get("active"));
    }

    @Test
    public void rejectsUnsupportedSqlBeforeQueryExecution() {
        String[] invalid = {
                "SELECT DISTINCT name FROM customers", "DELETE FROM customers", "SELECT COUNT(*) FROM customers", "SELECT * FROM customers ORDER BY name",
                "SELECT * FROM customers WHERE name = 'Ada' OR active = true", "SELECT * FROM customers LIMIT -1",
                "SELECT * FROM customers LIMIT 9999999999999999", "SELECT * FROM customers; DELETE FROM customers",
                "SELECT * FROM customers WHERE name = 'unfinished", "SELECT name AS x, score AS x FROM customers",
                "SELECT * FROM customers WHERE email = NULL", "SELECT name + score FROM customers"
        };
        for (String sql : invalid) {
            assertThrows(sql, SQLException.class, () -> MongoSqlParser.parse(sql));
        }
    }
}
