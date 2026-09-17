# Verification

Verified locally on 17 September 2026 against the MongoDB demo stack. Commands were run from this checkout; the earlier submission's screenshots and results are not reused here.

## Driver

```bash
MONGO_PORT=27019 HI_PORT=18080 docker compose -p sameer-helical -f docker-compose.mongodb.yml up -d mongodb postgres
MONGO_URI='mongodb://hi_mongo:hi_mongo_dev@127.0.0.1:27019/hi_demo?authSource=admin' \
  mvn -f server/himongo-jdbc/pom.xml verify -Pintegration
```

Result: **24 tests passed, 0 failures, 0 errors, 0 skipped**.

| Suite | Tests | Coverage |
| --- | ---: | --- |
| MongoSqlParserTest | 7 | Projections, aliases, nested fields, quoted strings, preview wrappers, limits, unsupported SQL |
| MongoConnectionConfigTest | 5 | URI/property timeout precedence, authentication settings, redaction, driver selection, metadata patterns |
| MongoDocumentConverterTest | 1 | BSON conversion and nested document fields |
| MongoJdbcDriverIT | 11 | Live connection, authentication failure, unreachable server, metadata, queries, previews, empty results, row caps, result lifecycle |

The test run used Java 21, Maven 3.8.7 and MongoDB 7. The adapter compiles for Java 11. Integration tests are explicitly enabled with `-Pintegration` and fail if the database is unavailable.

## Application

The React production build and Java backend packaging completed successfully. The frontend emitted existing lint warnings.

The existing datasource component suites for create/edit, flat files and JNDI passed: **3 suites, 3 tests**. These are component checks, not proof of the full browser workflow.

The final Docker image was rebuilt from the tested source and started on `localhost:18080` with a fresh application repository volume. The running backend uses Java 25 and Tomcat 11.

| Check | Method | Result |
| --- | --- | --- |
| MongoDB driver appears under Data Sources | Browser | Pass; Mongodb is listed under No SQL & Big Data |
| Connection test with separate credentials | Browser | Pass; connection test successful |
| Test → Save → confirm | Browser | Pass; datasource created successfully |
| Reopen, edit name, update | Browser | Pass; saved as MongoDB assessment demo, connection ID 1000 |
| Saved connection after full page refresh | Browser | Pass; edited name and connection remain in View |
| Catalog and collection discovery | Browser | Pass; hi_demo → customers |
| Field discovery | Browser | Pass; _id, name, email, score, active, createdAt, tags, address.city, address.zip |
| Views execution and eye preview | Browser | Pass; three customer rows with expected nulls |
| MongoDB URI with embedded credentials | Application API | Pass; existing core/dataSource/test service |
| PostgreSQL connection | Application API | Pass; existing PostgreSQL driver and core/dataSource/test service |
| Update missing the required type field | Application API | Pass; RequiredParameterIsNullException, no NullPointerException |

Preview query:

```sql
SELECT name, email, score FROM customers LIMIT 3
```

The application wrapped it as `select * from (...) foo limit 10`. The preview returned Ada Lovelace (100), Grace Hopper (99, null email), and Alan Turing (null score).

The PostgreSQL check verifies connectivity through the existing application service; it is not a full regression suite for all PostgreSQL reporting features. Browser screenshots from the earlier repository were not copied into this submission.

## Limits

The tests validate the documented read-only SQL subset. They do not establish support for joins, aggregates, sorting, full Adhoc-generated SQL, Atlas/SRV deployment, Instant BI, or every existing datasource. See [mongodb-setup.md](mongodb-setup.md).
