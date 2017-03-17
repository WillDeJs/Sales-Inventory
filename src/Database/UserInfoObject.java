package Database;

import java.io.Serializable;

/**
 * Class allows creation of a user object containing username and password
 * for a user.
 * @author WillDeJs
 */
public class UserInfoObject implements Serializable{

    /**
     * Builds a UserInfoObject
     * @param userName  username    
     * @param password  password
     * @param isAdmin   System administrator flag
     */
    public UserInfoObject(String userName, String password, boolean isAdmin) {
        this.userName = userName;
        this.password = password;
        this.isAdmin = isAdmin;
    }
    
    /**
     * Retrieves username
     * @return username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets user name
     * @param userName   username 
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Retrieves user's password
     * @return  password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets a user password
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieves user IS_ADMIN flag. The flag determines if the user is system
     * administrator
     * @return  true if user is administrator, false otherwise
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Sets user as system administrator
     * @param isAdmin   user administrator falg, true if user is admin, false 
     * 
     */
    public void setAdminFlag(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    
    // user properties
    private String userName;
    private String password;
    private boolean isAdmin;
    
}
