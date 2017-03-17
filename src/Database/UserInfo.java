package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import Utilities.Trace;
import Utilities.Trace.Levels;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Class allows managing user information such as username and password 
 * in the database
 * @author WillDeJs
 */
public class UserInfo {

    /**
     * Default constructor builds a UserInfoObject
     */
    public UserInfo() {
        sqliteConn = new SqliteConnection();
        connection = SqliteConnection.getConnection();
    }
    
    /**
     * Verifies whether a user and password set match a user in the database
     * @param username  username
     * @param password  password
     * @return True if user is valid, false otherwise
     */
    public boolean isValidUser(String username, String password) {
        
         String query = "SELECT * FROM USER_INFO WHERE USER=? AND PASSWORD=?;";
         boolean result = false;
         PreparedStatement preparedStatement;
         try {
            
            // Query database to check user
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            
            // if user and password match database then user is valid
            ResultSet set = preparedStatement.executeQuery();
            result = set.next();
            preparedStatement.close();
         } catch (Exception ex) {
             Trace.getTrace().log(UserInfo.class, Levels.ERROR,
                     "Could not verify user " + username, ex);
         }
         return result;
    }
    
    /**
     * Updates the password for a selected user
     * @param username          username
     * @param currentPassword   current password
     * @param newPassword       new password
     * @return 
     */
    public synchronized boolean changePassword(String username, String currentPassword, String newPassword) {
        PreparedStatement preparedStatement;
        String query = "UPDATE USER_INFO SET PASSWORD=? WHERE USER=? AND PASSWORD=?;";
        int changes = 0;
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, currentPassword);
            changes = preparedStatement.executeUpdate();
            preparedStatement.close();
       
        } catch (Exception ex) {
            Trace.getTrace().log(this.getClass(), Levels.ERROR, "Could not update"
                    + "user " + username, ex);
        }    
        return changes > 0;
    }
    
    
    /** Adds a user to database 
     * 
     * @param username  username
     * @param password  password
     * @return  Returns true if user created, false otherwise
     */
    public boolean addUser(String username, String password) {
        
        // add user to database
        String query = "INSERT INTO USER_INFO(USER, PASSWORD) VALUES(?, ?);";
        int changes = 0;
        try {
            if (!userExists(username)) {
                
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                changes = preparedStatement.executeUpdate();
                preparedStatement.close();
               
            }
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(),Levels.ERROR, ex);
        }        
        return changes > 0;
    }
    
    /**
     * Deletes a user from the database
     * @param username  username
     * @return  True if deletion was successful, false otherwise
     */
    public boolean deleteUser(String username) {
        
        // DELETE user FROM database
        String query = "DELETE FROM USER_INFO WHERE USER=?;";
        int result = 0;
        try {
            if (userExists(username)) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, username);
                result = preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(),Levels.ERROR, ex);
        }        
        return result > 0;
    }
    
    /**
     * Determines whether a user is already in database
     * @params username username
     * @returns True if user exists, false otherwise
     */
    public boolean userExists(String username) {
        ResultSet resultSet;
        boolean result = false;
        PreparedStatement p;
        String query = "SELECT * FROM USER_INFO WHERE USER=?;";
        try {
            p = connection.prepareStatement(query);
            p.setString(1, username);
            resultSet = p.executeQuery();
            result = resultSet.next();
            p.close();
        } catch (Exception ex) {
            Trace.getTrace().log(getClass(), Levels.ERROR, ex);
        }
        return result;
    }
    
    /** Retrieves a list of users from database
     * @return list of UserInfoObjects from database, null if no users found
     */
    public ObservableList<UserInfoObject> getUsers() {
        ObservableList<UserInfoObject> list = null;
        PreparedStatement preparedStatement;
        String query = "SELECT * FROM USER_INFO;";
        ResultSet result;
         try {
             
             // get all users and add to the list
             list = FXCollections.observableArrayList();
             preparedStatement = connection.prepareStatement(query);
             result = preparedStatement.executeQuery();
             while (result.next()) {
                 boolean isAdmin = result.getString("IS_ADMIN").equals("YES");
                 list.add( new UserInfoObject(result.getString("USER"),
                        result.getString("PASSWORD"),
                        isAdmin
                 ));
             }
             result.close();
         } catch (Exception ex) {
             Trace.getTrace().log(getClass(), Levels.ERROR, ex);
         }
         return list;
    }
    
    /**
     * Retrieves a single user from database
     * @param username  username of the user
     * @return  UserInfoObject containing user information, null if user does not exist
     */
   public UserInfoObject getUser(String username) {
        String query = "SELECT * FROM USER_INFO WHERE USER=?;";
        ResultSet result;
        UserInfoObject user = null;
        PreparedStatement preparedStatement;
         try {
             // get all users and add to the list
             preparedStatement = connection.prepareStatement(query);
             preparedStatement.setString(1, username);
             result = preparedStatement.executeQuery();
             if (result.next()) {
                boolean isAdmin = result.getString("IS_ADMIN").toUpperCase().equals("YES");
                user =  new UserInfoObject(result.getString("USER"),
                            result.getString("PASSWORD"),
                            isAdmin
                );
             }
             result.close();
         } catch (Exception ex) {
             Trace.getTrace().log(getClass(), Levels.ERROR, ex);
         }
         return user;
    }
    /**
     * Determine if user has administrative privileges 
     * @return true if user is administrator, false otherwise
     */
    public boolean isAdmin(String username) {
        boolean isAdmin = false;
        UserInfoObject user = getUser(username);
        if (user != null) {
            isAdmin = user.isAdmin();
        }
        return isAdmin;
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
        connection.close();
    }
    
    // properties
    private Connection connection;
    private SqliteConnection sqliteConn;
}
