/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Provides a way to hash a user password before storing into database it uses 
 * SHA-1 Algorithm
 * This is just a simple based on an oracle example not meant to be super secure
 * Check NumberPhile videos on youtube on hashing to see how INSECURE this can be
 * @author WillDeJs
 */
public class PasswordHash {
    
    /**
     * Gets a hashed value of the specified argument using the SHA-1 algorithm
     * @param password  string to hash
     * @return  hashed version of the password string
     */
    public static String getHashedPassword(String password) {
        MessageDigest msgDigest;
        StringBuffer hashedPassword = new StringBuffer();
        
        try {
            msgDigest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = msgDigest.digest(password.getBytes());
            for (Byte b : bytes ) {
                hashedPassword.append(String.format("%02x", b));
            }
        } catch(NoSuchAlgorithmException ex){
            Trace.getTrace().log(PasswordHash.class, Trace.Levels.ERROR,
                    "Error hashing password", ex);
        }
        return hashedPassword.toString();
    }
    
}
