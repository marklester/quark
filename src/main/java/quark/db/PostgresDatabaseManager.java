package quark.db;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Represent how to interact with the datastore 
 * TODO should test postgres since cockroach
 * is a bit slow
 */
public class PostgresDatabaseManager implements DatabaseManager{
  static String username = "maxroach";
  static String password = "";
  static String jdbcurl = "jdbc:postgresql://127.0.0.1:26257/quark?sslmode=disable";

  private PostgresOrderDAO orderDao;
  private DataSource ds;
  public PostgresDatabaseManager() throws Exception {
    start();
  }
  
  public PostgresDatabaseManager(DataSource postgresDatabase) throws SQLException, IOException {
    ds = postgresDatabase;
    createTables();
    orderDao = new PostgresOrderDAO(DSL.using(ds, SQLDialect.POSTGRES));
  }
  
  public void start() throws Exception {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcurl);
    config.setUsername(username);
    config.setPassword(password);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    ds = new HikariDataSource(config);
    createTables();
    orderDao = new PostgresOrderDAO(DSL.using(ds, SQLDialect.POSTGRES));
  }

  public OrderDAO getOrderDao() {
    return orderDao;
  }

  void createTables() throws SQLException, IOException {
    DSLContext ctx = DSL.using(ds, SQLDialect.POSTGRES_9_4);
    String ddl = Files
        .asCharSource(Paths.get("src/main/resources/quark.sql").toFile(), Charsets.UTF_8).read();
    ctx.execute(ddl);
  }
}
