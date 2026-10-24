# Labs 2 — Lab 5: Spring and Hibernate integration

Java 11+, Maven, Spring 5.3.20, Hibernate 5.6.9.Final, JPA 2.2.
Standalone console application using Spring's declarative transactions, without Spring Boot.
No HTTP/REST endpoint is required by the handout; that remains a suggested extension.

## Database required — read first

**Yes: this lab uses a real SQL database and a products table.**
The handout's main mode is MySQL. You can use your existing Docker MySQL container.
H2 is also included for local demos and isolated automated tests without a server.

### MySQL setup

1. Start Docker Desktop.
2. Start your previously created container:

```powershell
docker start jee-mysql
```

3. In IntelliJ's MySQL query console, execute database.sql or:

```sql
CREATE DATABASE IF NOT EXISTS product_db CHARACTER SET utf8mb4;
```

4. Default application.properties uses 127.0.0.1:3306, root/password, matching the
   local Docker example. If yours differs, set DB_URL, DB_USER, DB_PASSWORD environment
   variables or edit the properties. Never commit real credentials.
5. Run:

```powershell
cd C:\Users\PC\Documents\study_5\JEE\labs2\lab5
mvn compile exec:java
```

Hibernate automatically creates/updates products; only the database must be created manually.
Refresh IntelliJ's Database panel and enable the product_db schema to see the table.
MySQL execution requires the Docker engine/container to be running.

### H2 demo — no Docker needed

```powershell
cd C:\Users\PC\Documents\study_5\JEE\labs2\lab5
mvn clean test
mvn compile exec:java "-Dlab.db=h2"
```

H2 mode creates an in-memory database automatically. Its data disappears at JVM exit.
It does not populate MySQL product_db. Do not label H2 tests as MySQL execution evidence.

### Connection-only entry point

```powershell
mvn compile exec:java "-Dexec.mainClass=com.microservice.product.TestHibernate"
```

Add "-Dlab.db=h2" for the connection-only H2 test.
Expected: database name and Contexte Spring initialise avec succes.

## IntelliJ

Open pom.xml as a Maven project with JDK 11+.
Run com.microservice.product.MainApp (MySQL by default).
For H2 set VM options to -Dlab.db=h2.
Run ProductIntegrationTest for seven tests. Tests override connection properties and use isolated H2.

## Implemented architecture

- entity/Product: id, name, double price, JPA annotations and complete accessors.
- dao/IDao<T>: save/update/delete/findById/findAll.
- dao/ProductDaoImpl: constructor-injected SessionFactory, getCurrentSession, @Repository.
- config/HibernateConfig: DriverManagerDataSource, package scanning, LocalSessionFactoryBean.
- config/TransactionConfig: HibernateTransactionManager and @EnableTransactionManagement.
- service/ProductService and ProductServiceImpl: full CRUD, validation, read-only queries,
  write transactions and atomic addProducts batch.
- config/AppConfig: component scanning.
- TestHibernate: verifies a real JDBC connection; MainApp: complete scenario.

The handout's org.SessionFactory import is corrected to org.hibernate.SessionFactory.
MySQL/JPA dependencies, Java compiler settings and the missing ProductService interface are supplied.
No manual transaction management or EntityManager is mixed with Spring-managed sessions.
Call services through the Spring context so transaction proxies are used.
Tests prove an earlier INSERT rolls back when a later batch item fails.

Double price is retained to match the requested entity; a financial application should normally
use BigDecimal with a defined precision/scale. Negative, NaN and infinite prices are rejected.
DriverManagerDataSource is intentionally the handout's simple non-pooling source.
This is an educational development setup, not production migrations or a production connection pool.
There is no optimistic version field in the supplied model.

## Expected demo

- Adds Microservice Gateway at 149.99.
- Updates it to 159.99.
- Rejects an invalid batch and prints ROLLBACK CONFIRME.
- Creates/deletes a temporary product and prints SUPPRESSION CONFIRMEE : true.
- Prints the final list and DEMO TERMINEE.

The gateway remains stored for database screenshots. Repeated MySQL runs add another gateway;
total counts can therefore increase. Tests remain isolated regardless of MySQL contents.

## Only four screenshots

1. **Structure and configuration**: IntelliJ tree with entity, DAO, config, service, MainApp,
   TestHibernate, application.properties, tests and pom.xml. Open TransactionConfig or the
   service to show transaction configuration/annotations. Hide passwords.
2. **Successful demo**: added/updated product, ROLLBACK CONFIRME, SUPPRESSION CONFIRMEE,
   final list and successful completion. Run in MySQL mode for the handout's main evidence.
3. **Database schema and data**: product_db -> products with id/name/price columns and stored
   Gateway row. Use a second capture if necessary to keep it readable.
4. **Tests**: 7 tests passed, zero failures/errors and BUILD SUCCESS.

Screenshot command (run MySQL first):

```powershell
mvn compile exec:java 2>&1 | Tee-Object -FilePath screenshot-output.log
Select-String -Path screenshot-output.log -Pattern 'PRODUIT AJOUTE|Total en base|PRODUIT MODIFIE|ROLLBACK CONFIRME|SUPPRESSION CONFIRMEE|LISTE FINALE|DEMO TERMINEE|BUILD SUCCESS'
```

Save real images under screenshots/. If Docker is unavailable, the H2 demo still validates
the application; the MySQL schema screenshot must wait until MySQL has actually run.
