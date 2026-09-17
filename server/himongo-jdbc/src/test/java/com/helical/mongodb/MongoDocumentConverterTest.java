package com.helical.mongodb;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MongoDocumentConverterTest {

    @Test
    public void discoversNestedObjectIdDateAndMissingFields() {
        Document first = new Document("_id", new ObjectId())
                .append("name", "Ada")
                .append("createdAt", new Date(1_700_000_000_000L))
                .append("address", new Document("city", "London"))
                .append("tags", Arrays.asList("a", "b"));
        Document second = new Document("_id", new ObjectId())
                .append("name", null)
                .append("score", 12);

        Map<String, Integer> columns = MongoDocumentConverter.discoverColumns(Arrays.asList(first, second));
        assertTrue(columns.containsKey("_id"));
        assertTrue(columns.containsKey("name"));
        assertTrue(columns.containsKey("createdAt"));
        assertTrue(columns.containsKey("address.city"));
        assertTrue(columns.containsKey("tags"));
        assertTrue(columns.containsKey("score"));
        assertEquals(Types.TIMESTAMP, (int) columns.get("createdAt"));
        assertEquals(Types.VARCHAR, (int) columns.get("_id"));

        List<Object> row = MongoDocumentConverter.projectRow(first, Arrays.asList(
                "_id", "name", "createdAt", "address.city", "tags", "score"));
        assertEquals(24, ((String) row.get(0)).length());
        assertEquals("Ada", row.get(1));
        assertTrue(row.get(2) instanceof Timestamp);
        assertEquals("London", row.get(3));
        assertTrue(row.get(4) instanceof String);
        assertNull(row.get(5));
    }
}
