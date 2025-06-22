package com.example.lms.util;

import com.example.lms.model.AppSetting;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Utility class for email operations in the application.
 * This is primarily used for sending password reset emails.
 */
public class EmailUtil {

    /**
     * Send a password reset email to a user.
     * 
     * @param recipientEmail The email address of the recipient
     * @param resetToken The password reset token
     * @param userName The name of the user
     * @return True if email was sent successfully, false otherwise
     */
    public static boolean sendPasswordResetEmail(String recipientEmail, String resetToken, String userName) {
        try {
            // Retrieve email settings from the database
            Connection connection = DatabaseConnection.getConnection();
            
            String emailAddress = getSettingValue(connection, "email_address");
            String emailPassword = getSettingValue(connection, "email_password");
            String smtpHost = getSettingValue(connection, "smtp_host");
            String smtpPort = getSettingValue(connection, "smtp_port");
            String appName = getSettingValue(connection, "app_name");
            
            DatabaseConnection.closeConnection(connection);
            
            // Check if email settings are configured
            if (emailAddress == null || emailAddress.isEmpty() || 
                emailPassword == null || emailPassword.isEmpty()) {
                System.err.println("Email settings not configured in the database");
                return false;
            }

            // Set mail properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailAddress, emailPassword);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            
            // Set subject and content
            message.setSubject("Password Reset Request - " + appName);
            
            // HTML email content
            String htmlContent = 
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                ".container { width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                "h2 { color: #333366; }" +
                ".button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; " +
                "text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }" +
                "footer { margin-top: 20px; font-size: 12px; color: #888; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h2>Hello " + userName + ",</h2>" +
                "<p>We received a request to reset your password for your account at " + appName + ".</p>" +
                "<p>Your password reset code is: <strong>" + resetToken + "</strong></p>" +
                "<p>Please use this code in the password reset form to create a new password.</p>" +
                "<p>If you did not request a password reset, please ignore this email.</p>" +
                "<p>Thank you,<br>The " + appName + " Team</p>" +
                "<footer>This is an automated message, please do not reply to this email.</footer>" +
                "</div>" +
                "</body>" +
                "</html>";
                
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Send message
            Transport.send(message);
            
            System.out.println("Password reset email sent to: " + recipientEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get a setting value from the app_setting table.
     * 
     * @param connection The database connection
     * @param key The setting key to retrieve
     * @return The setting value or null if not found
     */
    private static String getSettingValue(Connection connection, String key) {
        try {
            String query = "SELECT setting_value FROM app_setting WHERE setting_key = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, key);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("setting_value");
            }
            
            rs.close();
            stmt.close();
            
        } catch (Exception e) {
            System.err.println("Error retrieving setting: " + key + " - " + e.getMessage());
        }
        
        return null;
    }
}
