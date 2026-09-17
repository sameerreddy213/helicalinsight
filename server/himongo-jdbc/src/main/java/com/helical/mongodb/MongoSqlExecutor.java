package com.helical.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

final class MongoSqlExecutor {
    static final int DEFAULT_QUERY_ROW_CAP = 200;

    private MongoSqlExecutor() {
    }

    static MongoResultSet execute(MongoConnection connection, String sql, int maxRows, int timeout)
            throws SQLException {
        MongoSqlParser.Query query = MongoSqlParser.parse(sql);
        if (query.probe) {
            String label = query.columns.get(0).label;
            return new MongoResultSet(Collections.singletonList(label), Collections.singletonMap(label, Types.INTEGER),
                    query.limit != null && query.limit == 0 ? Collections.emptyList()
                            : Collections.singletonList(Collections.singletonList(1)));
        }
        String collectionName = query.collection;
        List<String> collections = connection.getMongoDatabase().listCollectionNames().into(new ArrayList<>());
        if (!collections.contains(collectionName) && collectionName.startsWith(connection.getDatabaseName() + ".")) {
            collectionName = collectionName.substring(connection.getDatabaseName().length() + 1);
        }
        if (!collections.contains(collectionName)) {
            throw new SQLException("Unknown MongoDB collection: " + query.collection);
        }
        MongoCollection<Document> collection = connection.getMongoDatabase().getCollection(collectionName);
        int limit = query.limit == null ? connection.getDefaultQueryLimit() : query.limit;
        if (maxRows > 0) limit = Math.min(limit, maxRows);
        List<Document> documents = new ArrayList<>();
        // Mongo's limit(0) means unlimited, whereas SQL LIMIT 0 means no rows.
        if (limit > 0) {
            FindIterable<Document> find = collection.find(query.filter).limit(limit);
            if (timeout > 0) find.maxTime(timeout, TimeUnit.SECONDS);
            try (MongoCursor<Document> cursor = find.iterator()) {
                while (cursor.hasNext()) documents.add(cursor.next());
            }
        }
        List<Document> samples = documents;
        if (samples.isEmpty()) {
            samples = new ArrayList<>();
            FindIterable<Document> sample = collection.find().limit(connection.getSampleSize());
            if (timeout > 0) sample.maxTime(timeout, TimeUnit.SECONDS);
            try (MongoCursor<Document> cursor = sample.iterator()) {
                while (cursor.hasNext()) samples.add(cursor.next());
            }
        }
        Map<String, Integer> discovered = MongoDocumentConverter.discoverColumns(samples);
        if (query.columns.isEmpty()) {
            for (String field : discovered.keySet()) query.columns.add(new MongoSqlParser.Column(field, field));
        }
        List<String> fields = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Map<String, Integer> types = new LinkedHashMap<>();
        for (MongoSqlParser.Column column : query.columns) {
            fields.add(column.field);
            labels.add(column.label);
            types.put(column.label, discovered.getOrDefault(column.field, Types.VARCHAR));
        }
        List<List<Object>> rows = new ArrayList<>();
        for (Document document : documents) rows.add(MongoDocumentConverter.projectRow(document, fields));
        MongoResultSet result = new MongoResultSet(labels, types, rows);
        if (query.limit == null) {
            result.setWarningMessage("MongoDB query capped at " + limit
                    + " rows because LIMIT was omitted. Add LIMIT n for an explicit bound.");
        }
        return result;
    }
}
