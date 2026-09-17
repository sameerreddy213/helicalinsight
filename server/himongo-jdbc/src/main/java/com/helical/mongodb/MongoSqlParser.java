package com.helical.mongodb;

import org.bson.Document;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Parser for read-only collection queries and Helical's preview wrapper. */
final class MongoSqlParser {
    static final class Column {
        final String field;
        final String label;

        Column(String field, String label) {
            this.field = field;
            this.label = label;
        }
    }

    static final class Query {
        String collection;
        final List<Column> columns = new ArrayList<>();
        Document filter = new Document();
        Integer limit;
        boolean probe;
    }

    private final String sql;
    private int position;
    private String token;
    private char quote;

    private MongoSqlParser(String sql) throws SQLException {
        this.sql = sql;
        advance();
    }

    static Query parse(String sql) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new SQLException("SQL is empty");
        }
        MongoSqlParser parser = new MongoSqlParser(sql);
        Query query = parser.select(0);
        parser.take(";");
        if (parser.token != null) {
            throw parser.unsupported();
        }
        return query;
    }

    private Query select(int depth) throws SQLException {
        if (depth > 16) {
            throw new SQLException("Too many nested preview wrappers");
        }
        expect("SELECT");
        if (keyword("DISTINCT") || keyword("ALL")) throw unsupported();
        Query query = new Query();
        if (take("1")) {
            query.probe = true;
            query.columns.add(new Column("1", take("AS") ? identifier() : "1"));
            return query;
        }
        boolean wildcard = take("*");
        if (!wildcard) {
            do {
                String field = path();
                String label = field;
                if (take("AS")) {
                    label = identifier();
                } else if (isIdentifier() && !keyword("FROM")) {
                    label = identifier();
                }
                for (Column column : query.columns) {
                    if (column.label.equals(label)) {
                        throw new SQLException("Duplicate column label: " + label);
                    }
                }
                query.columns.add(new Column(field, label));
            } while (take(","));
        }
        expect("FROM");
        if (take("(")) {
            if (!wildcard) {
                throw unsupported();
            }
            query = select(depth + 1);
            expect(")");
            if (take("AS")) {
                identifier();
            } else if (token != null && (quote != 0 || !keyword("LIMIT"))
                    && !keyword(")") && !keyword(";")) {
                identifier();
            }
        } else {
            query.collection = path();
            if (take("WHERE")) {
                String field = path();
                expect("=");
                query.filter = new Document(field, literal());
            }
        }
        if (take("LIMIT")) {
            if (quote != 0 || token == null || !token.matches("[0-9]+")) {
                throw new SQLException("LIMIT must be a non-negative integer");
            }
            try {
                int limit = Integer.parseInt(token);
                query.limit = query.limit == null ? limit : Math.min(query.limit, limit);
            } catch (NumberFormatException e) {
                throw new SQLException("LIMIT exceeds the supported integer range");
            }
            advance();
        }
        return query;
    }

    private Object literal() throws SQLException {
        if (quote == '\'') {
            String value = token;
            advance();
            return value;
        }
        if (take("TRUE")) return true;
        if (take("FALSE")) return false;
        if (token == null || quote != 0) throw unsupported();
        String value = token;
        advance();
        try {
            if (value.matches("[-+]?[0-9]+")) return Long.parseLong(value);
            if (value.matches("[-+]?[0-9]+(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?")) {
                double number = Double.parseDouble(value);
                if (Double.isFinite(number)) return number;
            }
        } catch (NumberFormatException e) {
            throw new SQLException("Numeric literal is out of range");
        }
        throw new SQLException("WHERE requires a quoted string, number or boolean literal");
    }

    private String path() throws SQLException {
        StringBuilder value = new StringBuilder(identifier());
        while (take(".")) value.append('.').append(identifier());
        return value.toString();
    }

    private boolean isIdentifier() {
        return token != null && (quote == '"' || quote == '`'
                || (quote == 0 && token.matches("[A-Za-z_][A-Za-z0-9_]*")));
    }

    private String identifier() throws SQLException {
        if (token == null || quote == '\'' || (quote == 0 && !token.matches("[A-Za-z_][A-Za-z0-9_]*"))) {
            throw unsupported();
        }
        String value = token;
        advance();
        return value;
    }

    private boolean keyword(String value) {
        return quote == 0 && value.equalsIgnoreCase(token);
    }

    private boolean take(String value) throws SQLException {
        if (!keyword(value)) return false;
        advance();
        return true;
    }

    private void expect(String value) throws SQLException {
        if (!take(value)) throw unsupported();
    }

    private void advance() throws SQLException {
        while (position < sql.length() && Character.isWhitespace(sql.charAt(position))) position++;
        quote = 0;
        if (position == sql.length()) {
            token = null;
            return;
        }
        char first = sql.charAt(position++);
        if (first == '\'' || first == '"' || first == '`') {
            quote = first;
            StringBuilder value = new StringBuilder();
            while (position < sql.length()) {
                char ch = sql.charAt(position++);
                if (ch == first) {
                    if (position < sql.length() && sql.charAt(position) == first) {
                        position++;
                        value.append(first);
                    } else {
                        token = value.toString();
                        return;
                    }
                } else value.append(ch);
            }
            throw new SQLException("Unterminated SQL string or identifier");
        }
        int start = position - 1;
        if (Character.isDigit(first) || first == '+' || first == '-') {
            while (position < sql.length() && "0123456789.eE+-".indexOf(sql.charAt(position)) >= 0) position++;
        } else if (Character.isLetter(first) || first == '_') {
            while (position < sql.length() && (Character.isLetterOrDigit(sql.charAt(position)) || sql.charAt(position) == '_')) position++;
        }
        token = sql.substring(start, position);
    }

    private SQLException unsupported() {
        return new SQLException("Unsupported MongoDB SQL near character " + position
                + ". Use SELECT *|fields FROM collection [WHERE field = literal] [LIMIT n].");
    }
}
