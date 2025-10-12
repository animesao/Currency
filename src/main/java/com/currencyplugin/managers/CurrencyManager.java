package com.currencyplugin.managers;

import com.currencyplugin.CurrencyPlugin;
import com.currencyplugin.commands.CurrencyCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.util.*;

public class CurrencyManager {
    
    private final CurrencyPlugin plugin;
    private final Map<String, Currency> currencies;
    private final Map<String, Currency> commandMap;
    
    public CurrencyManager(CurrencyPlugin plugin) {
        this.plugin = plugin;
        this.currencies = new HashMap<>();
        this.commandMap = new HashMap<>();
    }
    
    public void loadCurrencies() {
        currencies.clear();
        commandMap.clear();
        
        ConfigurationSection currenciesSection = plugin.getConfig().getConfigurationSection("currencies");
        if (currenciesSection == null) {
            plugin.getLogger().warning("No currencies found in config!");
            return;
        }
        
        for (String currencyId : currenciesSection.getKeys(false)) {
            ConfigurationSection currencySection = currenciesSection.getConfigurationSection(currencyId);
            if (currencySection == null) continue;
            
            String displayName = currencySection.getString("display-name", currencyId);
            String command = currencySection.getString("command", currencyId);
            String placeholder = currencySection.getString("placeholder", currencyId);
            double startingBalance = currencySection.getDouble("starting-balance", 0.0);
            
            Map<String, String> permissions = new HashMap<>();
            ConfigurationSection permSection = currencySection.getConfigurationSection("permissions");
            if (permSection != null) {
                for (String key : permSection.getKeys(false)) {
                    permissions.put(key, permSection.getString(key));
                }
            }
            
            Map<String, String> messages = new HashMap<>();
            ConfigurationSection msgSection = currencySection.getConfigurationSection("messages");
            if (msgSection != null) {
                for (String key : msgSection.getKeys(false)) {
                    messages.put(key, msgSection.getString(key));
                }
            }
            
            Currency currency = new Currency(currencyId, displayName, command, placeholder, 
                                            startingBalance, permissions, messages);
            currencies.put(currencyId, currency);
            commandMap.put(command.toLowerCase(), currency);
            
            plugin.getLogger().info("Loaded currency: " + currencyId + " (/" + command + ")");
        }
    }
    
    public void registerCommands() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            
            for (Currency currency : currencies.values()) {
                CurrencyCommand currencyCommand = new CurrencyCommand(plugin, currency);
                commandMap.register("currencyplugin", currencyCommand);
                plugin.getLogger().info("Registered command: /" + currency.getCommand());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Currency getCurrency(String id) {
        return currencies.get(id);
    }
    
    public Currency getCurrencyByCommand(String command) {
        return commandMap.get(command.toLowerCase());
    }
    
    public Currency getCurrencyByPlaceholder(String placeholder) {
        return currencies.values().stream()
                .filter(c -> c.getPlaceholder().equalsIgnoreCase(placeholder))
                .findFirst()
                .orElse(null);
    }
    
    public Collection<Currency> getCurrencies() {
        return currencies.values();
    }
    
    public Map<String, Currency> getCurrenciesMap() {
        return currencies;
    }
}
