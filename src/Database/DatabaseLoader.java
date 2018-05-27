/*
 * This class serves as base class to all classes which load to the databases.
 * It takes care of oppening and closing connections to the database.
 */
package Database;

import java.sql.Connection;

/**
 *
 * @author Wilmer
 */
public abstract class DatabaseLoader {
    
    
    // connection to database 
    private Connection connection;
    private  SqliteConnection sqliteConn;
    
    /**
     * Constructor, initializes the database connection.
     */
    protected DatabaseLoader() {
        sqliteConn = new SqliteConnection();
        connection = SqliteConnection.getConnection();
    }
    
    /**
     * Retrieves the current SQL database connection.
     * @return  Database SQL connection for the object.
     */
    public Connection getDBConnection() {
        return connection;
    }
    /* Clean up, clsoe up the connection*/
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection.close();
    }
}
