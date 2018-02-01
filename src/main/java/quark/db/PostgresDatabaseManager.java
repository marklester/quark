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

public class PostgresDatabaseManager implements DatabaseManager{

  private PostgresOrderDAO orderDao;
  private DataSource ds;
  String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
  public PostgresDatabaseManager() throws Exception {
//    createDatabase();
    start();
  }
  
//  private void createDatabase() throws IOException {
//    String homeDir = System.getProperty("user.home");
//    Path dataDir = Paths.get(homeDir, ".quark","data");
//    try(database = EmbeddedPostgres.builder().setDataDirectory(dataDir).start();
//        ){
//      
//    }
//
//  }

  public void start() throws Exception {
    HikariConfig config = new HikariConfig();
    String username = "postgres";
    String password = "postgres";
//    String dbName = "postgres";
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    ds = new HikariDataSource(config);
    createTables();
    orderDao = new PostgresOrderDAO(DSL.using(ds, SQLDialect.POSTGRES_9_5),new PGRecordMapper());
  }

  public OrderDAO getOrderDao() {
    return orderDao;
  }

  void createTables() throws SQLException, IOException {
    DSLContext ctx = DSL.using(ds, SQLDialect.POSTGRES_9_5);
    String ddl = Files
        .asCharSource(Paths.get("src/main/resources/quark.sql").toFile(), Charsets.UTF_8).read();
    ctx.execute(ddl);
  }
  
  public static void main(String args[]) throws Exception {
    PostgresDatabaseManager pg = new PostgresDatabaseManager();
  }
}
