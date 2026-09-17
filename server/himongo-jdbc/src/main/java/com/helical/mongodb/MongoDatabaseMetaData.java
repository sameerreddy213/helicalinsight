package com.helical.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MongoDatabaseMetaData implements DatabaseMetaData {

    private final MongoConnection connection;

    MongoDatabaseMetaData(MongoConnection connection) {
        this.connection = connection;
    }

    @Override
    public boolean allProceduresAreCallable() {
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() {
        return true;
    }

    @Override
    public String getURL() {
        return connection.getJdbcUrl();
    }

    @Override
    public String getUserName() {
        return "";
    }

    @Override
    public boolean isReadOnly() {
        return connection.isReadOnly();
    }

    @Override
    public boolean nullsAreSortedHigh() {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() {
        return true;
    }

    @Override
    public boolean nullsAreSortedAtStart() {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() {
        return false;
    }

    @Override
    public String getDatabaseProductName() {
        return "MongoDB";
    }

    @Override
    public String getDatabaseProductVersion() {
        return "unknown";
    }

    @Override
    public String getDriverName() {
        return "Helical MongoDB JDBC Driver";
    }

    @Override
    public String getDriverVersion() {
        return "1.0.0";
    }

    @Override
    public int getDriverMajorVersion() {
        return 1;
    }

    @Override
    public int getDriverMinorVersion() {
        return 0;
    }

    @Override
    public boolean usesLocalFiles() {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() {
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() {
        return true;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() {
        return true;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() {
        return true;
    }

    @Override
    public String getIdentifierQuoteString() {
        return "\"";
    }

    @Override
    public String getSQLKeywords() {
        return "";
    }

    @Override
    public String getNumericFunctions() {
        return "";
    }

    @Override
    public String getStringFunctions() {
        return "";
    }

    @Override
    public String getSystemFunctions() {
        return "";
    }

    @Override
    public String getTimeDateFunctions() {
        return "";
    }

    @Override
    public String getSearchStringEscape() {
        return "\\";
    }

    @Override
    public String getExtraNameCharacters() {
        return "";
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() {
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() {
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() {
        return true;
    }

    @Override
    public boolean nullPlusNonNullIsNull() {
        return true;
    }

    @Override
    public boolean supportsConvert() {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() {
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() {
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() {
        return false;
    }

    @Override
    public boolean supportsGroupBy() {
        return false;
    }

    @Override
    public boolean supportsGroupByUnrelated() {
        return false;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() {
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() {
        return false;
    }

    @Override
    public boolean supportsMultipleResultSets() {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() {
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() {
        return false;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() {
        return true;
    }

    @Override
    public boolean supportsCoreSQLGrammar() {
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() {
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() {
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() {
        return false;
    }

    @Override
    public String getSchemaTerm() {
        return "schema";
    }

    @Override
    public String getProcedureTerm() {
        return "procedure";
    }

    @Override
    public String getCatalogTerm() {
        return "database";
    }

    @Override
    public boolean isCatalogAtStart() {
        return true;
    }

    @Override
    public String getCatalogSeparator() {
        return ".";
    }

    @Override
    public boolean supportsSchemasInDataManipulation() {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() {
        return true;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInIns() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() {
        return false;
    }

    @Override
    public boolean supportsUnion() {
        return false;
    }

    @Override
    public boolean supportsUnionAll() {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() {
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() {
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() {
        return 128;
    }

    @Override
    public int getMaxColumnsInGroupBy() {
        return 0;
    }

    @Override
    public int getMaxColumnsInIndex() {
        return 0;
    }

    @Override
    public int getMaxColumnsInOrderBy() {
        return 0;
    }

    @Override
    public int getMaxColumnsInSelect() {
        return 0;
    }

    @Override
    public int getMaxColumnsInTable() {
        return 0;
    }

    @Override
    public int getMaxConnections() {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() {
        return 0;
    }

    @Override
    public int getMaxIndexLength() {
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() {
        return 128;
    }

    @Override
    public int getMaxProcedureNameLength() {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() {
        return 128;
    }

    @Override
    public int getMaxRowSize() {
        return 0;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        return false;
    }

    @Override
    public int getMaxStatementLength() {
        return 0;
    }

    @Override
    public int getMaxStatements() {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() {
        return 128;
    }

    @Override
    public int getMaxTablesInSelect() {
        return 1;
    }

    @Override
    public int getMaxUserNameLength() {
        return 128;
    }

    @Override
    public int getDefaultTransactionIsolation() {
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) {
        return level == Connection.TRANSACTION_NONE;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
        return false;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) {
        return emptyResult(List.of("PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "REMARKS", "PROCEDURE_TYPE",
                "SPECIFIC_NAME"));
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
                                         String columnNamePattern) {
        return emptyResult(List.of("PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "COLUMN_NAME", "COLUMN_TYPE",
                "DATA_TYPE", "TYPE_NAME", "PRECISION", "LENGTH", "SCALE", "RADIX", "NULLABLE", "REMARKS"));
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
            throws SQLException {
        List<String> columns = List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS", "TYPE_CAT",
                "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION");
        Map<String, Integer> columnTypes = varcharTypes(columns);
        List<List<Object>> rows = new ArrayList<>();
        if (types != null) {
            boolean includeTables = false;
            for (String type : types) {
                if ("TABLE".equalsIgnoreCase(type) || "COLLECTION".equalsIgnoreCase(type)) {
                    includeTables = true;
                    break;
                }
            }
            if (!includeTables) {
                return new MongoResultSet(columns, columnTypes, rows);
            }
        }
        String effectiveCatalog = catalog == null || catalog.isEmpty() ? connection.getDatabaseName() : catalog;
        if (!effectiveCatalog.equals(connection.getDatabaseName())) {
            return new MongoResultSet(columns, columnTypes, rows);
        }
        for (String collection : connection.getMongoDatabase().listCollectionNames()) {
            if (tableNamePattern != null && !"%".equals(tableNamePattern) && !matches(collection, tableNamePattern)) {
                continue;
            }
            List<Object> row = new ArrayList<>();
            row.add(effectiveCatalog);
            row.add(null); // no Mongo schema layer; matches Helical null-schema table listing
            row.add(collection);
            row.add("TABLE");
            row.add("MongoDB collection");
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            rows.add(row);
        }
        return new MongoResultSet(columns, columnTypes, rows);
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return getSchemas(null, null);
    }

    @Override
    public ResultSet getCatalogs() {
        List<String> columns = List.of("TABLE_CAT");
        Map<String, Integer> types = varcharTypes(columns);
        List<List<Object>> rows = Collections.singletonList(Collections.singletonList(connection.getDatabaseName()));
        return new MongoResultSet(columns, types, rows);
    }

    @Override
    public ResultSet getTableTypes() {
        List<String> columns = List.of("TABLE_TYPE");
        Map<String, Integer> types = varcharTypes(columns);
        List<List<Object>> rows = Collections.singletonList(Collections.singletonList("TABLE"));
        return new MongoResultSet(columns, types, rows);
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        List<String> columns = List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME",
                "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS",
                "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION",
                "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE",
                "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN");
        Map<String, Integer> columnTypes = varcharTypes(columns);
        // Override a few numeric types for DATA_TYPE etc. — keep simple as VARCHAR/INTEGER mix via converter.
        columnTypes.put("DATA_TYPE", Types.INTEGER);
        columnTypes.put("COLUMN_SIZE", Types.INTEGER);
        columnTypes.put("NULLABLE", Types.INTEGER);
        columnTypes.put("ORDINAL_POSITION", Types.INTEGER);

        List<List<Object>> rows = new ArrayList<>();
        String effectiveCatalog = catalog == null || catalog.isEmpty() ? connection.getDatabaseName() : catalog;
        if (!effectiveCatalog.equals(connection.getDatabaseName())) {
            return new MongoResultSet(columns, columnTypes, rows);
        }

        for (String collection : connection.getMongoDatabase().listCollectionNames()) {
            if (tableNamePattern != null && !"%".equals(tableNamePattern) && !matches(collection, tableNamePattern)) {
                continue;
            }
            MongoCollection<Document> mongoCollection = connection.getMongoDatabase().getCollection(collection);
            List<Document> samples = new ArrayList<>();
            for (Document document : mongoCollection.find().limit(connection.getSampleSize())) {
                samples.add(document);
            }
            Map<String, Integer> discovered = MongoDocumentConverter.discoverColumns(samples);
            int ordinal = 1;
            for (Map.Entry<String, Integer> entry : discovered.entrySet()) {
                String columnName = entry.getKey();
                if (columnNamePattern != null && !"%".equals(columnNamePattern)
                        && !matches(columnName, columnNamePattern)) {
                    continue;
                }
                List<Object> row = new ArrayList<>();
                row.add(effectiveCatalog);
                row.add(null);
                row.add(collection);
                row.add(columnName);
                row.add(entry.getValue());
                row.add(MongoDocumentConverter.typeName(entry.getValue()));
                row.add(255);
                row.add(null);
                row.add(null);
                row.add(10);
                row.add(DatabaseMetaData.columnNullable);
                row.add(null);
                row.add(null);
                row.add(null);
                row.add(null);
                row.add(255);
                row.add(ordinal++);
                row.add("YES");
                row.add(null);
                row.add(null);
                row.add(null);
                row.add(null);
                row.add("NO");
                row.add("NO");
                rows.add(row);
            }
        }
        return new MongoResultSet(columns, columnTypes, rows);
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) {
        return emptyResult(List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "GRANTOR", "GRANTEE",
                "PRIVILEGE", "IS_GRANTABLE"));
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) {
        return emptyResult(List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "GRANTOR", "GRANTEE", "PRIVILEGE",
                "IS_GRANTABLE"));
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) {
        return emptyResult(List.of("SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH",
                "DECIMAL_DIGITS", "PSEUDO_COLUMN"));
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) {
        return emptyResult(List.of("SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH",
                "DECIMAL_DIGITS", "PSEUDO_COLUMN"));
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) {
        List<String> columns = List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "KEY_SEQ", "PK_NAME");
        Map<String, Integer> types = varcharTypes(columns);
        types.put("KEY_SEQ", Types.INTEGER);
        List<List<Object>> rows = new ArrayList<>();
        List<Object> row = new ArrayList<>();
        row.add(connection.getDatabaseName());
        row.add(null);
        row.add(table);
        row.add("_id");
        row.add(1);
        row.add("PRIMARY");
        rows.add(row);
        return new MongoResultSet(columns, types, rows);
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) {
        return emptyResult(List.of("PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT",
                "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME",
                "PK_NAME", "DEFERRABILITY"));
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) {
        return getImportedKeys(catalog, schema, table);
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
                                       String foreignCatalog, String foreignSchema, String foreignTable) {
        return getImportedKeys(parentCatalog, parentSchema, parentTable);
    }

    @Override
    public ResultSet getTypeInfo() {
        return emptyResult(List.of("TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX", "LITERAL_SUFFIX",
                "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE", "UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE",
                "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE", "SQL_DATA_TYPE",
                "SQL_DATETIME_SUB", "NUM_PREC_RADIX"));
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) {
        return emptyResult(List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "NON_UNIQUE", "INDEX_QUALIFIER",
                "INDEX_NAME", "TYPE", "ORDINAL_POSITION", "COLUMN_NAME", "ASC_OR_DESC", "CARDINALITY", "PAGES",
                "FILTER_CONDITION"));
    }

    @Override
    public boolean supportsResultSetType(int type) {
        return type == ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        return type == ResultSet.TYPE_FORWARD_ONLY && concurrency == ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() {
        return false;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) {
        return emptyResult(List.of("TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "CLASS_NAME", "DATA_TYPE", "REMARKS",
                "BASE_TYPE"));
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean supportsSavepoints() {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
        return false;
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) {
        return emptyResult(List.of("TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SUPERTYPE_CAT", "SUPERTYPE_SCHEM",
                "SUPERTYPE_NAME"));
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) {
        return emptyResult(List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "SUPERTABLE_NAME"));
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
                                   String attributeNamePattern) {
        return emptyResult(List.of("TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "ATTR_NAME", "DATA_TYPE", "ATTR_TYPE_NAME",
                "ATTR_SIZE", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "ATTR_DEF", "SQL_DATA_TYPE",
                "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG",
                "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE"));
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) {
        return holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getResultSetHoldability() {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getDatabaseMajorVersion() {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() {
        return 0;
    }

    @Override
    public int getJDBCMajorVersion() {
        return 4;
    }

    @Override
    public int getJDBCMinorVersion() {
        return 2;
    }

    @Override
    public int getSQLStateType() {
        return sqlStateSQL;
    }

    @Override
    public boolean locatorsUpdateCopy() {
        return false;
    }

    @Override
    public boolean supportsStatementPooling() {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        // MongoDB has no schema layer. Returning an empty set lets Helical's metadata
        // UI treat collections as catalog children (via fetchTables with schemas:[]),
        // instead of a nested same-named schema node that never expands to tables.
        List<String> columns = List.of("TABLE_SCHEM", "TABLE_CATALOG");
        Map<String, Integer> types = varcharTypes(columns);
        return new MongoResultSet(columns, types, new ArrayList<>());
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() {
        return emptyResult(List.of("NAME", "MAX_LEN", "DEFAULT_VALUE", "DESCRIPTION"));
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) {
        return emptyResult(List.of("FUNCTION_CAT", "FUNCTION_SCHEM", "FUNCTION_NAME", "REMARKS", "FUNCTION_TYPE",
                "SPECIFIC_NAME"));
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
                                        String columnNamePattern) {
        return emptyResult(List.of("FUNCTION_CAT", "FUNCTION_SCHEM", "FUNCTION_NAME", "COLUMN_NAME", "COLUMN_TYPE",
                "DATA_TYPE", "TYPE_NAME", "PRECISION", "LENGTH", "SCALE", "RADIX", "NULLABLE", "REMARKS",
                "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SPECIFIC_NAME"));
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
                                      String columnNamePattern) {
        return emptyResult(List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", "COLUMN_SIZE",
                "DECIMAL_DIGITS", "NUM_PREC_RADIX", "COLUMN_USAGE", "REMARKS", "CHAR_OCTET_LENGTH", "IS_NULLABLE"));
    }

    @Override
    public boolean generatedKeyAlwaysReturned() {
        return false;
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

    static boolean matches(String value, String pattern) {
        if (pattern == null) return true;
        StringBuilder regex = new StringBuilder();
        boolean escaped = false;
        for (char ch : pattern.toCharArray()) {
            if (escaped) {
                regex.append(java.util.regex.Pattern.quote(String.valueOf(ch)));
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '%') {
                regex.append(".*");
            } else if (ch == '_') {
                regex.append('.');
            } else {
                regex.append(java.util.regex.Pattern.quote(String.valueOf(ch)));
            }
        }
        if (escaped) regex.append(java.util.regex.Pattern.quote("\\"));
        return value.matches(regex.toString());
    }

    private static Map<String, Integer> varcharTypes(List<String> columns) {
        Map<String, Integer> types = new LinkedHashMap<>();
        for (String column : columns) {
            types.put(column, Types.VARCHAR);
        }
        return types;
    }

    private static MongoResultSet emptyResult(List<String> columns) {
        return new MongoResultSet(columns, varcharTypes(columns), new ArrayList<>());
    }
}
