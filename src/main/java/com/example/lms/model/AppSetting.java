package com.example.lms.model;

import java.time.LocalDateTime;

/**
 * Model class for AppSetting entity.
 * Represents application settings stored in the database.
 */
public class AppSetting {
    private int id;
    private String settingKey;
    private String settingValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public AppSetting() {
    }

    // Constructor with essential fields
    public AppSetting(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    // Constructor with all fields
    public AppSetting(int id, String settingKey, String settingValue, 
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Alias for getSettingKey() to maintain compatibility with controller
     * @return the setting key
     */
    public String getKey() {
        return getSettingKey();
    }
    
    /**
     * Alias for getSettingValue() to maintain compatibility with controller
     * @return the setting value
     */
    public String getValue() {
        return getSettingValue();
    }
    
    /**
     * Alias for setSettingValue() to maintain compatibility with controller
     * @param value the setting value to set
     */
    public void setValue(String value) {
        setSettingValue(value);
    }

    @Override
    public String toString() {
        return "AppSetting{" +
                "id=" + id +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                '}';
    }
}
