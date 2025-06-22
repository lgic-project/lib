package com.example.lms.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for security-related functions like password hashing.
 */
public class SecurityUtil {
    
    /**
     * Hashes a password using MD5 algorithm.
     * Note: In a production system, we would use a stronger algorithm like BCrypt.
     * 
     * @param password The password to hash
     * @return Hashed password
     */
    public static String hashPassword(String password) {
        try {
            // Get an instance of the MD5 hashing algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            // Convert the password string to bytes and update the digest
            md.update(password.getBytes());
            
            // Get the hash value
            byte[] digest = md.digest();
            
            // Convert the byte array to a hexadecimal string representation
            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);
            
            // Pad with leading zeros to ensure it's 32 characters
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Generates a random token for password reset.
     * 
     * @return A random token
     */
    public static String generateResetToken() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] token = new byte[16];
            secureRandom.nextBytes(token);
            
            // Convert to hexadecimal
            return new BigInteger(1, token).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Error generating reset token", e);
        }
    }
    
    /**
     * Verify if a plain-text password matches a hashed password.
     * 
     * @param plainPassword The plain-text password input by user
     * @param hashedPassword The hashed password from database
     * @return True if they match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(hashedPassword);
    }
}
