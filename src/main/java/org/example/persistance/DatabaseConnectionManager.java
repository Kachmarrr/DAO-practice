package org.example.persistance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionManager {

    private final String url;
    public final Properties properties;

    public DatabaseConnectionManager(String host, String databaseName,
                                     String username, String password) {
        this.url = "jdbc:postgresql://" + host + "/" + databaseName;
        this.properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);

    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver"); // this clearly shows that we have a driver.
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return DriverManager.getConnection(this.url, this.properties);
    }

    public static DatabaseConnectionManager fromClasspathResource(String resourceName) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            p.load(in);
        }
        String host = p.getProperty("db.host");
        String db = p.getProperty("db.name", "postgres");
        String user = p.getProperty("db.user");
        String pass = p.getProperty("db.pass");
        return new DatabaseConnectionManager(host, db, user, pass);
    }



}

