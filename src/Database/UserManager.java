package Database;

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
public class UserManager extends DatabaseLoader {

    
    private static UserManager userInfo;
    
    /**
     * Default constructor builds a UserInfoObject
     */
    private UserManager() {
        super();
    }
    
    public static UserManager getManager() {
        if(userInfo == null) 
            userInfo = new UserManager();
        return userInfo;
    }
    /**
     * Verifies whether a user and password set match a user in the database
     * @param username  username
     * @param password  password
     * @return True if user is valid, false otherwise
     */
    public static  boolean isValidUser(String username, String password) {
        
         String query = "SELECT * FROM USER_INFO WHERE USER=? AND PASSWORD=?;";
         boolean result = false;
         PreparedStatement preparedStatement;
         try {
            
            // Query database to check user
            preparedStatement = userInfo.getDBConnection().prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            
            // if user and password match database then user is valid
            ResultSet set = preparedStatement.executeQuery();
            result = set.next();
            preparedStatement.close();
         } catch (Exception ex) {
             Trace.getTrace().log(UserManager.class, Levels.ERROR,
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
    public static synchronized boolean changePassword(String username, String newPassword) {
        PreparedStatement preparedStatement;
        String query = "UPDATE USER_INFO SET PASSWORD=? WHERE USER=?;";
        int changes = 0;
        try {
            preparedStatement = userInfo.getDBConnection().prepareStatement(query);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, username);
            changes = preparedStatement.executeUpdate();
            preparedStatement.close();
       
        } catch (Exception ex) {
            Trace.getTrace().log(userInfo.getClass(), Levels.ERROR, "Could not update"
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
    public static boolean addUser(String username, String password) {
        
        // add user to database
        String query = "INSERT INTO USER_INFO(USER, PASSWORD) VALUES(?, ?);";
        int changes = 0;
        try {
            if (!userExists(username)) {
                
                PreparedStatement preparedStatement = userInfo.getDBConnection().prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                changes = preparedStatement.executeUpdate();
                preparedStatement.close();
               
            }
        } catch (Exception ex) {
            Trace.getTrace().log(userInfo.getClass(),Levels.ERROR, ex);
        }        
        return changes > 0;
    }
    
    /**
     * Deletes a user from the database
     * @param username  username
     * @return  True if deletion was successful, false otherwise
     */
    public static boolean deleteUser(String username) {
        
        // DELETE user FROM database
        String query = "DELETE FROM USER_INFO WHERE USER=?;";
        int result = 0;
        try {
            if (userExists(username)) {
                PreparedStatement preparedStatement = userInfo.getDBConnection().prepareStatement(query);
                preparedStatement.setString(1, username);
                result = preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (Exception ex) {
            Trace.getTrace().log(userInfo.getClass(),Levels.ERROR, ex);
        }        
        return result > 0;
    }
    
    /**
     * Determines whether a user is already in database
     * @params username username
     * @returns True if user exists, false otherwise
     */
    public static boolean userExists(String username) {
        ResultSet resultSet;
        boolean result = false;
        PreparedStatement p;
        String query = "SELECT * FROM USER_INFO WHERE USER=?;";
        try {
            p = userInfo.getDBConnection().prepareStatement(query);
            p.setString(1, username);
            resultSet = p.executeQuery();
            result = resultSet.next();
            p.close();
        } catch (Exception ex) {
            Trace.getTrace().log(userInfo.getClass(), Levels.ERROR, ex);
        }
        return result;
    }
    
    /** Retrieves a list of users from database
     * @return list of UserInfoObjects from database, null if no users found
     */
    public  static ObservableList<UserInfoObject> getUsers() {
        ObservableList<UserInfoObject> list = null;
        PreparedStatement preparedStatement;
        String query = "SELECT * FROM USER_INFO;";
        ResultSet result;
         try {
             
             // get all users and add to the list
             list = FXCollections.observableArrayList();
             preparedStatement = userInfo.getDBConnection().prepareStatement(query);
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
             Trace.getTrace().log(userInfo.getClass(), Levels.ERROR, ex);
         }
         return list;
    }
    
    /**
     * Retrieves a single user from database
     * @param username  username of the user
     * @return  UserInfoObject containing user information, null if user does not exist
     */
   public static UserInfoObject getUser(String username) {
        String query = "SELECT * FROM USER_INFO WHERE USER=?;";
        ResultSet result;
        UserInfoObject user = null;
        PreparedStatement preparedStatement;
         try {
             // get all users and add to the list
             preparedStatement = userInfo.getDBConnection().prepareStatement(query);
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
             Trace.getTrace().log(userInfo.getClass(), Levels.ERROR, ex);
         }
         return user;
    }
    /**
     * Determine if user has administrative privileges 
     * @return true if user is administrator, false otherwise
     */
    public static boolean isAdmin(String username) {
        boolean isAdmin = false;
        UserInfoObject user = userInfo.getUser(username);
        if (user != null) {
            isAdmin = user.isAdmin();
        }
        return isAdmin;
    }
}
