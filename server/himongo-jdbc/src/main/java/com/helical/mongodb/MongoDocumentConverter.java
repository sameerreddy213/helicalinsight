package com.helical.mongodb;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts BSON documents into tabular rows with predictable type handling.
 */
final class MongoDocumentConverter {

    private MongoDocumentConverter() {
    }

    static Map<String, Integer> discoverColumns(List<Document> samples) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (Document document : samples) {
            flatten(document, "", columns);
        }
        if (!columns.containsKey("_id")) {
            Map<String, Integer> withId = new LinkedHashMap<>();
            withId.put("_id", Types.VARCHAR);
            withId.putAll(columns);
            return withId;
        }
        return columns;
    }

    private static void flatten(Document document, String prefix, Map<String, Integer> columns) {
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Document) {
                flatten((Document) value, key, columns);
            } else {
                columns.putIfAbsent(key, jdbcType(value));
            }
        }
    }

    static List<Object> projectRow(Document document, List<String> columnNames) {
        List<Object> row = new ArrayList<>(columnNames.size());
        for (String column : columnNames) {
            Object raw = readPath(document, column);
            row.add(toJdbcValue(raw));
        }
        return row;
    }

    static Object readPath(Document document, String path) {
        if (document == null) {
            return null;
        }
        if (!path.contains(".")) {
            return document.get(path);
        }
        String[] parts = path.split("\\.");
        Object current = document;
        for (String part : parts) {
            if (!(current instanceof Document)) {
                return null;
            }
            current = ((Document) current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    static Object toJdbcValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof ObjectId) {
            return ((ObjectId) value).toHexString();
        }
        if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        }
        if (value instanceof Document) {
            return ((Document) value).toJson();
        }
        if (value instanceof List) {
            return new Document("value", value).toJson();
        }
        if (value instanceof BsonDocument) {
            return ((BsonDocument) value).toJson();
        }
        if (value instanceof BsonArray) {
            return ((BsonArray) value).toString();
        }
        if (value instanceof BsonValue) {
            return value.toString();
        }
        if (value instanceof Double || value instanceof Float) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                return value;
            }
            return value;
        }
        if (value instanceof Boolean || value instanceof String || value instanceof byte[]) {
            return value;
        }
        return String.valueOf(value);
    }

    static int jdbcType(Object value) {
        if (value == null) {
            return Types.VARCHAR;
        }
        if (value instanceof ObjectId || value instanceof String) {
            return Types.VARCHAR;
        }
        if (value instanceof Boolean) {
            return Types.BOOLEAN;
        }
        if (value instanceof Integer) {
            return Types.INTEGER;
        }
        if (value instanceof Long) {
            return Types.BIGINT;
        }
        if (value instanceof Double || value instanceof Float) {
            return Types.DOUBLE;
        }
        if (value instanceof BigDecimal) {
            return Types.DECIMAL;
        }
        if (value instanceof Date) {
            return Types.TIMESTAMP;
        }
        if (value instanceof Document || value instanceof List || value instanceof BsonValue) {
            return Types.VARCHAR;
        }
        return Types.VARCHAR;
    }

    static String typeName(int jdbcType) {
        switch (jdbcType) {
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            default:
                return "VARCHAR";
        }
    }

    static Set<String> orderedKeys(Map<String, Integer> columns) {
        return new LinkedHashSet<>(columns.keySet());
    }
}
