package quark.db;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.Duration;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.common.io.CharStreams;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import quark.simulation.MarketSimulator;

public class PostgresDatabaseManager implements DatabaseManager {

  private PostgresOrderDAO orderDao;
  private DataSource ds;
  String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";

  public PostgresDatabaseManager() throws Exception {
    // createDatabase();
    start();
  }

  // private void createDatabase() throws IOException {
  // String homeDir = System.getProperty("user.home");
  // Path dataDir = Paths.get(homeDir, ".quark","data");
  // try(database = EmbeddedPostgres.builder().setDataDirectory(dataDir).start();
  // ){
  //
  // }
  //
  // }

  public void start() throws Exception {
    HikariConfig config = new HikariConfig();
    String username = "postgres";
    String password = "postgres";
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    ds = new HikariDataSource(config);
    createTables();
    orderDao = new PostgresOrderDAO(getContext(), new OrderRecordMapper());
  }

  public OrderDAO getOrderDao() {
    return orderDao;
  }

  DSLContext getContext() {
    return DSL.using(ds, SQLDialect.POSTGRES_9_5);
  }

  void createTables() throws SQLException, IOException {
    DSLContext ctx = getContext();
    String ddl = CharStreams.toString(
        new InputStreamReader(getClass().getClassLoader().getResourceAsStream("quark.sql")));
    ctx.execute(ddl);
  }

  @Override
  public MarketSimulator getMarketSimulator(Duration tickRate) {
    return new MarketSimulator(getContext(), tickRate, getOrderDao());
  }
}
