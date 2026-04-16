package com.currencyplugin.managers;

import com.currencyplugin.CurrencyPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    
    private final CurrencyPlugin plugin;
    private final DatabaseManager databaseManager;
    private final File dataFolder;
    private final Map<UUID, Map<String, Double>> balances;
    
    public DataManager(CurrencyPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = new DatabaseManager(plugin);
        this.dataFolder = new File(plugin.getDataFolder(), 
            plugin.getConfig().getString("storage.yaml.folder", "playerdata"));
        this.balances = new HashMap<>();
        
        // Инициализация базы данных
        databaseManager.initialize();
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    public double getBalance(UUID playerId, String currencyId) {
        DatabaseManager.StorageType type = databaseManager.getStorageType();
        
        if (type == DatabaseManager.StorageType.YAML) {
            loadPlayerData(playerId);
            return balances.getOrDefault(playerId, new HashMap<>())
                          .getOrDefault(currencyId, getStartingBalance(currencyId));
        } else {
            // Для SQL баз данных - синхронное получение
            try {
                return databaseManager.getBalance(playerId, currencyId).get();
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка получения баланса: " + e.getMessage());
                return getStartingBalance(currencyId);
            }
        }
    }
    
    public void setBalance(UUID playerId, String currencyId, double amount) {
        DatabaseManager.StorageType type = databaseManager.getStorageType();
        
        if (type == DatabaseManager.StorageType.YAML) {
            loadPlayerData(playerId);
            balances.computeIfAbsent(playerId, k -> new HashMap<>()).put(currencyId, amount);
            savePlayerData(playerId);
        } else {
            // Для SQL баз данных - асинхронное сохранение
            databaseManager.setBalance(playerId, currencyId, amount);
        }
    }
    
    public void addBalance(UUID playerId, String currencyId, double amount) {
        double current = getBalance(playerId, currencyId);
        setBalance(playerId, currencyId, current + amount);
    }
    
    public void removeBalance(UUID playerId, String currencyId, double amount) {
        double current = getBalance(playerId, currencyId);
        setBalance(playerId, currencyId, Math.max(0, current - amount));
    }
    
    public boolean hasBalance(UUID playerId, String currencyId, double amount) {
        return getBalance(playerId, currencyId) >= amount;
    }
    
    private double getStartingBalance(String currencyId) {
        Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
        return currency != null ? currency.getStartingBalance() : 0.0;
    }
    
    private void loadPlayerData(UUID playerId) {
        if (balances.containsKey(playerId)) return;
        
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        if (!playerFile.exists()) {
            balances.put(playerId, new HashMap<>());
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        Map<String, Double> playerBalances = new HashMap<>();
        
        for (String currencyId : config.getKeys(false)) {
            playerBalances.put(currencyId, config.getDouble(currencyId));
        }
        
        balances.put(playerId, playerBalances);
    }
    
    private void savePlayerData(UUID playerId) {
        Map<String, Double> playerBalances = balances.get(playerId);
        if (playerBalances == null) return;
        
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, Double> entry : playerBalances.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data for player " + playerId + ": " + e.getMessage());
        }
    }
    
    public void saveAll() {
        DatabaseManager.StorageType type = databaseManager.getStorageType();
        
        if (type == DatabaseManager.StorageType.YAML) {
            for (UUID playerId : balances.keySet()) {
                savePlayerData(playerId);
            }
        }
        // Для SQL баз данных сохранение происходит автоматически
    }
    
    public void unloadPlayer(UUID playerId) {
        DatabaseManager.StorageType type = databaseManager.getStorageType();
        
        if (type == DatabaseManager.StorageType.YAML) {
            savePlayerData(playerId);
            balances.remove(playerId);
        }
        // Для SQL баз данных выгрузка не требуется
    }
    
    public void close() {
        saveAll();
        databaseManager.close();
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
