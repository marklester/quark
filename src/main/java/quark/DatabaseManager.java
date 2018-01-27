package quark;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import quark.orders.OrderDAO;

/**
 * Represent how to interact with the datastore 
 * TODO should test mariadb or postgres since cockroach
 * is a bit slow
 */
public class DatabaseManager {
  static String username = "maxroach";
  static String password = "";
  static String jdbcurl = "jdbc:postgresql://127.0.0.1:26257/quark?sslmode=disable";

  private OrderDAO orderDao;
  private HikariDataSource ds;

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
    orderDao = new OrderDAO(DSL.using(ds, SQLDialect.POSTGRES));
  }

  public OrderDAO getOrderDao() {
    return orderDao;
  }

  void createTables() throws SQLException, IOException {
    DSLContext ctx = DSL.using(ds, SQLDialect.POSTGRES_9_4);
    String ddl = Files.toString(Paths.get("src/main/resources/quark.sql").toFile(), Charsets.UTF_8);
    ctx.execute(ddl);
  }
}
