/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Database.UserInfo;
import Database.UserInfoObject;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import Utilities.Trace.*;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Class that allows retrieval and storage of user log information during runtime
 * It contains methods that read and serialized data to/from files
 * @author WillDeJs
 */
public class RuntimeInfo {
  
    /**
     * Retrieves a the information for the currently logged user
     * @return UserInfoObject containing the information for the user, null if user does'nt exist.
     */
    public static UserInfoObject getLoggedUser() {
        UserInfoObject user = null;
        try {
            in = new ObjectInputStream(new FileInputStream("bin" + File.separator  + "runtime" + File.separator + "runcred.ser"));
            user =  (UserInfoObject) in.readObject();
        } catch(Exception ex) {
            Trace.getTrace().log(RuntimeInfo.class, Levels.FATAL_ERROR, "Error reading runtime properties", ex);
        }
        return user;
    }
    
    /**
     * Sets the user logged in the system in runtime for later recovery
     * @param user  UserInfoObject containing user information
     */
    public static void setLoggedUser(UserInfoObject user) {
        UserInfo userInfo = null;
        try {
            userInfo = new UserInfo();
            out = new ObjectOutputStream(new FileOutputStream("bin" + File.separator  + "runtime" + File.separator + "runcred.ser"));
            out.writeObject(user);
        } catch(Exception ex) {
            Trace.getTrace().log(RuntimeInfo.class, Levels.FATAL_ERROR, "Error reading runtime properties", ex);
        }
    }
    
    /**
     * Sets the user logged in the system in runtime for later recovery
     * @param username username of the user
     */
    public static void setLoggedUser(String username) {
        UserInfo userInfo = new UserInfo();
        setLoggedUser(userInfo.getUser(username));
    }
    
    
    // properties
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
}
