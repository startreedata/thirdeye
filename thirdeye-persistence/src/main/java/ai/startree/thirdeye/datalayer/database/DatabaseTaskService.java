package ai.startree.thirdeye.datalayer.database;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DatabaseTaskService {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseTaskService.class);

  private final DataSource dataSource;
  private final Counter dbExceptionCounter;
  private final Counter dbCallCounter;

  @Inject
  public DatabaseTaskService(final DataSource dataSource,
      final MetricRegistry metricRegistry) {
    this.dataSource = dataSource;

    dbExceptionCounter = metricRegistry.counter("dbExceptionCounter");
    dbCallCounter = metricRegistry.counter("dbCallCounter");
  }


  /**
   * Use at your own risk!!! Ensure to close the connection after using it or it can cause a leak.
   */
  public Connection getConnection()
      throws SQLException {
    // ensure to close the connection
    return dataSource.getConnection();
  }

  <T> T runTask(final QueryTask<T> task, final T defaultReturnValue) {
    dbCallCounter.inc();

    Connection connection = null;
    try {
      connection = getConnection();
      // Enable transaction
      connection.setAutoCommit(false);
      final T t = task.handle(connection);
      // Commit this transaction
      connection.commit();
      return t;
    } catch (final Exception e) {
      LOG.error("Exception while executing query task", e);
      dbExceptionCounter.inc();

      // Rollback transaction in case json table is updated but index table isn't due to any errors (duplicate key, etc.)
      if (connection != null) {
        try {
          connection.rollback();
        } catch (final SQLException e1) {
          LOG.error("Failed to rollback SQL execution", e);
        }
      }
      return defaultReturnValue;
    } finally {
      // Always close connection before leaving
      if (connection != null) {
        try {
          connection.close();
        } catch (final SQLException e) {
          LOG.error("Failed to close connection", e);
        }
      }
    }
  }

  protected interface QueryTask<T> {

    T handle(Connection connection) throws Exception;
  }
}
