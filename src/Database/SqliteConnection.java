package Database;
import Utilities.Trace;
import java.io.File;
import java.sql.*;

/**
 * Class allows connecting to the database throw sqlite jdbc
 * 
 * Created by WillDeJs on 12/3/2016.
 */
public class SqliteConnection {
    
    
    // Database connection
     private Connection con = null;
     
     public SqliteConnection() {
        try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + "bin"
                    + File.separator + "SalesAndInventory.sqlite");

        }catch (Exception e) {
               Trace.getTrace().log(SqliteConnection.class, 
                    Trace.Levels.FATAL_ERROR,
                    " Could not connect to database!", e);
        }
     }
    /**
     * Create a connection to the database
     * @return sql.connection to database
     */
    public synchronized Connection getConnection2(){
        return con;
    }
    
    public static synchronized Connection getConnection(){
        Connection con = null;
          try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + "bin"
                    + File.separator + "SalesAndInventory.sqlite");

        }catch (Exception e) {
               Trace.getTrace().log(SqliteConnection.class, 
                    Trace.Levels.FATAL_ERROR,
                    " Could not connect to database!", e);
        }
          return con;
    }
    /**
     * Determines if a connection to the database is established
     * @return true if connected, false otherwise
     */
    public synchronized boolean isConnected(){
        try {
            return !con.isClosed();
        } catch(Exception ex){
            // log error
            Trace.getTrace().log(SqliteConnection.class, Trace.Levels.ERROR, ex);
            return false;
        }
    }
}
