package com.currencyplugin;

import com.currencyplugin.managers.CurrencyManager;
import com.currencyplugin.managers.DataManager;
import com.currencyplugin.placeholders.CurrencyPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CurrencyPlugin extends JavaPlugin {
    
    private static CurrencyPlugin instance;
    private CurrencyManager currencyManager;
    private DataManager dataManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        dataManager = new DataManager(this);
        currencyManager = new CurrencyManager(this);
        
        currencyManager.loadCurrencies();
        currencyManager.registerCommands();
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CurrencyPlaceholder(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
        
        getLogger().info("CurrencyPlugin enabled! Loaded " + currencyManager.getCurrencies().size() + " currencies.");
    }
    
    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("CurrencyPlugin disabled!");
    }
    
    public static CurrencyPlugin getInstance() {
        return instance;
    }
    
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
}
