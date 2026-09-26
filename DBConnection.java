package pims;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * Central JDBC connection helper for the Pharmacy Inventory Management System.
 *
 * The connection settings can be changed in two ways:
 *   1. Edit the constants below, or
 *   2. Place a file called "db.properties" in the project folder containing:
 *          url=jdbc:mysql://localhost:3306/pims_db
 *          user=root
 *          password=yourpassword
 */
public class DBConnection {

    private static String URL  = "jdbc:mysql://localhost:3306/pims_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String USER = "root";
    private static String PASS = "root";   // <-- change to your MySQL root password

    private static boolean loaded = false;

    private static void init() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            File f = new File("db.properties");
            if (f.exists()) {
                Properties p = new Properties();
                try (FileInputStream in = new FileInputStream(f)) {
                    p.load(in);
                }
                URL  = p.getProperty("url", URL);
                USER = p.getProperty("user", USER);
                PASS = p.getProperty("password", PASS);
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "MySQL JDBC Driver not found.\n\n"
                    + "In NetBeans: right-click the project > Properties > Libraries >\n"
                    + "Add JAR/Folder > select mysql-connector-j-x.x.x.jar",
                    "Driver Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not read db.properties: " + e.getMessage(),
                    "Configuration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Returns a brand new connection. Always close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        init();
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Simple start-up test so the user gets a friendly message instead of a stack trace. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the MySQL database.\n\n"
                    + e.getMessage() + "\n\n"
                    + "Checklist:\n"
                    + " 1. Is the MySQL server running?\n"
                    + " 2. Did you run database.sql to create 'pims_db'?\n"
                    + " 3. Are the username/password in DBConnection.java correct?",
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
