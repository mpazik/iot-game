package dzida.server.app.database;

import dzida.server.app.Configuration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private Connection connection;

    public void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(postgresUrl(), Configuration.databaseUser(), Configuration.databasePassword());

            DatabaseMetaData dbmd = connection.getMetaData();
            System.out.println("Connected with " + dbmd.getDriverName() +
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
                throw new IllegalStateException("Can not close not open database connection");
            }
            connection.close();
        } catch (SQLException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private String postgresUrl() {
        return "jdbc:postgresql://" + Configuration.databaseUrl() + "/" + Configuration.databaseName();
    }
}
