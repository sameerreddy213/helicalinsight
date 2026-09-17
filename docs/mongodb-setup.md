# MongoDB connectivity

This adds `com.helical.mongodb.MongoJdbcDriver` to Helical Insight's existing JDBC datasource workflow. MongoDB is a reporting datasource; PostgreSQL still stores the application's own data.

## Run the demo

Install Docker with Compose, then run from the repository root:

```bash
git clone https://github.com/sameerreddy213/helicalinsight.git
cd helicalinsight
docker compose -f docker-compose.mongodb.yml up --build -d
```

The first build compiles the React client, Java backend, and MongoDB driver. It requires internet access and enough memory for the frontend build (8 GB available is recommended). No local Java, Node, or paid JDBC bridge is required for this route.

Open http://localhost:8080/hi-ee/ and sign in with `hiadmin` / `hiadmin`.

If another application uses the default ports:

```bash
HI_PORT=18080 MONGO_PORT=27019 docker compose -f docker-compose.mongodb.yml up --build -d
```

Use the same port overrides for later Compose commands. The MongoDB hostname **inside the app** remains `mongodb:27017` regardless of the host port.

```bash
docker compose -f docker-compose.mongodb.yml ps
docker compose -f docker-compose.mongodb.yml logs --tail=100 hiee
docker compose -f docker-compose.mongodb.yml down
```

Named volumes retain saved connections, reports, and database data across restarts. The MongoDB entrypoint copies the initial application repository into an empty volume and installs the freshly built driver on every start. It does not rewrite source files on the host. This demo stack contains the app, PostgreSQL, and MongoDB; Instant BI and browser-based exports require the separate upstream setup.

## Configure a connection

1. Open **Data Sources**, then choose **Mongodb** under **No SQL & Big Data** (also visible under All).
2. Enter Host `mongodb`, Port `27017`, Database Name `hi_demo`, and Datasource Name `MongoDB demo`.
3. Enter User Name `hi_mongo` and Password `hi_mongo_dev`.
4. Click **Advanced** and enter `?authSource=admin` in **Other Options**. Keep **Tomcat Datasource** selected.
5. Click **Test Connection**, then **Save Datasource**, and confirm when prompted.
6. The **View** tab lists the saved connection. Use its edit action to update it or **Test** to retest it.

The resulting URI is `mongodb://mongodb:27017/hi_demo?authSource=admin`, with credentials passed separately. These are local demo credentials only.

The driver also accepts a complete credential-bearing URI through JDBC or the application's advanced URL configuration:

```text
mongodb://hi_mongo:hi_mongo_dev@mongodb:27017/hi_demo?authSource=admin
```

Leave the separate username/password fields blank when the URI includes credentials. Do not supply credentials in both places.

Browse `hi_demo` → `customers` in Metadata. Select the collection checkbox, right-click it, and choose **Add to Metadata**. Expand the added table to see fields such as `address.city`. In **Views**, click **Add**, enter this query, click **Execute**, wait for it to finish, then click the eye icon to preview:

```sql
SELECT name, email, score FROM customers LIMIT 3
```

Other supported examples:

```sql
SELECT name AS customer, address.city AS city FROM customers LIMIT 10
SELECT name, score FROM customers WHERE active = true LIMIT 10
SELECT name FROM customers WHERE name = 'Ada Lovelace' LIMIT 1
```

The seed includes Ada Lovelace, Grace Hopper, and Alan Turing, with missing fields, arrays, dates, booleans, and nested documents.

## Use an existing installation

Build the standalone adapter with Java 11+ and Maven:

```bash
bash scripts/build-himongo-jdbc.sh
```

Copy `server/hi-repository/System/Drivers/himongo-jdbc-1.0.0.jar` into the installation's `hi-repository/System/Drivers` directory. Apply the MongoDB entries from these files and restart Helical Insight:

- `System/Admin/databaseDrivers.properties`: standard and SRV URI templates.
- `System/Admin/Static/DataSourcesList.groovy`: driver display name and category.
- `System/Admin/sqlDialects.properties`: one MongoDB dialect mapping, using the existing MySQL-style preview syntax.

To include the connection-form fix, build and deploy the updated frontend as well. The Docker route does this automatically. The adapter is deliberately a standalone Maven project because Helical discovers datasource JARs from `System/Drivers`, independently of the application WAR.

For a host-side database without the full application:

```bash
docker compose -f docker/mongodb/docker-compose.yml up -d
```

Connect to `127.0.0.1:27017` from host tools. Standard MongoDB and `mongodb+srv://` URIs are accepted, with optional `jdbc:` prefix. SRV connections require working DNS, network access, and the server's TLS configuration; no Atlas account is part of the local demo.

## Implementation

| Component | Change |
| --- | --- |
| `server/himongo-jdbc` | JDBC driver using MongoDB Java sync driver 4.11.5; shaded JAR with JDBC service registration |
| Connection | URI validation, credential handling, ping, finite timeouts, credential-redacted errors |
| Metadata | Database as catalog, collections as tables, sampled document fields as columns; no schema layer |
| Query | Read-only SELECT parser, projection and aliases, one equality predicate, row limits, Helical Views preview wrapper |
| Data conversion | ObjectId as string, dates as timestamps, dotted nested fields, arrays/documents as JSON, absent values as null |
| Frontend | Store the clicked Test/Save action synchronously before form submission |
| Backend validation | Return validation errors for missing connection-update fields instead of dereferencing null |
| Demo | Separate Compose stack, seeded MongoDB, build driver from source, persistent app repository |

The implementation follows the existing driver discovery, encrypted connection storage, JDBC metadata, and statement/result-set paths. It builds on the earlier MongoDB adapter at https://github.com/SOWJANYAKAGITHA/helical-insight-mongodb, with a stricter SQL parser, preserved aliases and URI timeouts, empty-result metadata, bounded statement fetching, and automated integration-test execution. Upstream licenses are retained.

## Query scope and limits

This is a read-only JDBC adapter, not a full SQL engine. It supports `SELECT 1`, field lists or `*`, optional aliases, one `WHERE field = literal` predicate, `LIMIT`, and the application's `SELECT * FROM (...) alias LIMIT n` preview wrapper. Quoted identifiers and SQL string escaping (`'O''Brien'`) are supported.

Unsupported syntax raises `SQLException`: joins, grouping, ordering, aggregates, general subqueries, writes, and bound prepared-statement parameters. Full Adhoc report builders may generate SQL outside this subset. A parameter-free prepared statement can execute supported SQL.

Without `LIMIT`, queries return at most 200 rows and expose a JDBC warning. `defaultQueryLimit` can change that cap. `Statement.setMaxRows` further restricts the database fetch. `LIMIT 0` returns metadata without result rows. Empty-result metadata is inferred from a separate bounded collection sample; fields absent from the sample may not appear. Mixed document schemas are not a fixed relational schema.

Connection properties include `sampleSize` (default 50), `defaultQueryLimit` (200), `connectTimeoutMS` (5000), `serverSelectionTimeoutMS` (5000), and `socketTimeoutMS` (10000). URI timeout options take precedence over defaults; explicit JDBC properties take precedence over the URI. Query timeout is passed to MongoDB as `maxTime` for document reads.

## Tests

Unit tests:

```bash
mvn -f server/himongo-jdbc/pom.xml test
```

Live integration tests, after starting MongoDB:

```bash
MONGO_URI='mongodb://hi_mongo:hi_mongo_dev@127.0.0.1:27017/hi_demo?authSource=admin' \
  mvn -f server/himongo-jdbc/pom.xml verify -Pintegration
```

The integration profile enables Maven Failsafe. A missing database fails the run rather than silently skipping it. Tests use the `himongo_it_customers` collection and delete it afterward; use a disposable demo database. Checks cover connection/authentication, timeout failures, metadata, preview SQL, aliases, empty results, row caps, and statement lifecycle.

See [verification.md](verification.md) for results from this checkout.
