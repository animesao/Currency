package com.currencyplugin.managers;

import java.util.HashMap;
import java.util.Map;

public class Currency {
    
    private final String id;
    private final String displayName;
    private final String command;
    private final String placeholder;
    private final double startingBalance;
    private final Map<String, String> permissions;
    private final Map<String, String> messages;
    
    public Currency(String id, String displayName, String command, String placeholder, 
                   double startingBalance, Map<String, String> permissions, Map<String, String> messages) {
        this.id = id;
        this.displayName = displayName;
        this.command = command;
        this.placeholder = placeholder;
        this.startingBalance = startingBalance;
        this.permissions = permissions != null ? permissions : new HashMap<>();
        this.messages = messages != null ? messages : new HashMap<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCommand() {
        return command;
    }
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public double getStartingBalance() {
        return startingBalance;
    }
    
    public String getPermission(String type) {
        return permissions.getOrDefault(type, "currency." + id + "." + type);
    }
    
    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }
    
    public Map<String, String> getPermissions() {
        return permissions;
    }
    
    public Map<String, String> getMessages() {
        return messages;
    }
}
