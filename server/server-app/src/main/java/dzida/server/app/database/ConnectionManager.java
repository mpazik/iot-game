package dzida.server.app.database;

import dzida.server.app.Configuration;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final Logger log = Logger.getLogger(ConnectionManager.class);
    private Connection connection;

    public void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(postgresUrl(), Configuration.databaseUser(), Configuration.databasePassword());

            DatabaseMetaData dbmd = connection.getMetaData();
            log.info("Connected with " + dbmd.getDriverName() +
                    " " + dbmd.getDriverVersion() + "{ " + dbmd.getDriverMajorVersion() + "," + dbmd.getDriverMinorVersion() + " }" +
                    " to " + dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion() + "\n");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public ConnectionProvider getConnectionProvider() {
        if (connection == null) {
            throw new IllegalStateException("Database connection is not open. Connect to database first");
        }
        return new ConnectionProvider(connection);
    }

    public void close() {
        try {
            if (connection == null) {
                throw new IllegalStateException("Can not stop not open database connection");
            }
            connection.close();
            log.info("Closed connection with database.");
        } catch (SQLException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private String postgresUrl() {
        return "jdbc:postgresql://" + Configuration.databaseUrl() + "/" + Configuration.databaseName();
    }
}
