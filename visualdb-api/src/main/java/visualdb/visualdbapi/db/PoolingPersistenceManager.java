package visualdb.visualdbapi.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PoolingPersistenceManager {

    private static final Logger logger = Logger.getLogger(PoolingPersistenceManager.class.getName());
    private static PoolingPersistenceManager instance;

  public static PoolingPersistenceManager getPersistenceManager() {
    if (instance == null) {
      instance = new PoolingPersistenceManager();
    }
    return instance;
  }

  private HikariDataSource dataSource;

  private PoolingPersistenceManager() {
    try {
      Class.forName("org.postgresql.Driver");
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(
        "jdbc:postgresql://localhost:5432/VisualDB?currentSchema=public"
      );
      config.setUsername(System.getenv("DB_USER"));
      config.setPassword(System.getenv("DB_PASSWORD"));

      config.addDataSourceProperty(
        "dataSourceClassName",
        "org.postgresql.ds.PGSimpleDataSource"
      );
      config.addDataSourceProperty("maximumPoolSize", "25");

      dataSource = new HikariDataSource(config);
    } catch (ClassNotFoundException ex) {
        logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public void terminateDataSource() {
    try {
      this.dataSource.close();
      Enumeration<Driver> en = DriverManager.getDrivers();
      while (en.hasMoreElements()) {
        DriverManager.deregisterDriver(en.nextElement());
      }
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }
}
