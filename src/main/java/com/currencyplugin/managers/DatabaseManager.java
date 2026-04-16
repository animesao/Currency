package com.currencyplugin.managers;

import com.currencyplugin.CurrencyPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    
    private final CurrencyPlugin plugin;
    private HikariDataSource dataSource;
    private StorageType storageType;
    
    public enum StorageType {
        YAML, SQLITE, MYSQL
    }
    
    public DatabaseManager(CurrencyPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        String typeString = plugin.getConfig().getString("storage.type", "SQLite").toUpperCase();
        
        try {
            storageType = StorageType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный тип хранилища: " + typeString + ". Используется SQLite.");
            storageType = StorageType.SQLITE;
        }
        
        if (storageType == StorageType.MYSQL || storageType == StorageType.SQLITE) {
            setupDatabase();
        }
    }
    
    private void setupDatabase() {
        HikariConfig config = new HikariConfig();
        
        if (storageType == StorageType.MYSQL) {
            setupMySQL(config);
        } else if (storageType == StorageType.SQLITE) {
            setupSQLite(config);
        }
        
        try {
            dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().info("База данных успешно подключена (" + storageType + ")");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка подключения к базе данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupMySQL(HikariConfig config) {
        ConfigurationSection mysql = plugin.getConfig().getConfigurationSection("storage.mysql");
        
        String host = mysql.getString("host", "localhost");
        int port = mysql.getInt("port", 3306);
        String database = mysql.getString("database", "minecraft");
        String username = mysql.getString("username", "root");
        String password = mysql.getString("password", "password");
        
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // Настройки пула
        ConfigurationSection pool = mysql.getConfigurationSection("pool");
        if (pool != null) {
            config.setMaximumPoolSize(pool.getInt("maximum-pool-size", 10));
            config.setMinimumIdle(pool.getInt("minimum-idle", 2));
            config.setConnectionTimeout(pool.getLong("connection-timeout", 30000));
            config.setIdleTimeout(pool.getLong("idle-timeout", 600000));
            config.setMaxLifetime(pool.getLong("max-lifetime", 1800000));
        }
        
        // Дополнительные свойства
        ConfigurationSection properties = mysql.getConfigurationSection("properties");
        if (properties != null) {
            for (String key : properties.getKeys(false)) {
                config.addDataSourceProperty(key, properties.getString(key));
            }
        }
        
        config.setPoolName("CurrencyPlugin-MySQL");
    }
    
    private void setupSQLite(HikariConfig config) {
        ConfigurationSection sqlite = plugin.getConfig().getConfigurationSection("storage.sqlite");
        
        String filename = sqlite.getString("filename", "currency.db");
        String path = sqlite.getString("path", "data/");
        
        File dataFolder = new File(plugin.getDataFolder(), path);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File dbFile = new File(dataFolder, filename);
        
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setPoolName("CurrencyPlugin-SQLite");
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    }
    
    private void createTables() {
        String sql;
        
        if (storageType == StorageType.MYSQL) {
            sql = "CREATE TABLE IF NOT EXISTS currency_balances (" +
                  "player_uuid VARCHAR(36) NOT NULL, " +
                  "currency_id VARCHAR(64) NOT NULL, " +
                  "balance DOUBLE NOT NULL DEFAULT 0.0, " +
                  "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                  "PRIMARY KEY (player_uuid, currency_id), " +
                  "INDEX idx_player (player_uuid), " +
                  "INDEX idx_currency (currency_id)" +
                  ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS currency_balances (" +
                  "player_uuid TEXT NOT NULL, " +
                  "currency_id TEXT NOT NULL, " +
                  "balance REAL NOT NULL DEFAULT 0.0, " +
                  "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                  "PRIMARY KEY (player_uuid, currency_id)" +
                  ");";
        }
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка создания таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource не инициализирован");
        }
        return dataSource.getConnection();
    }
    
    public CompletableFuture<Double> getBalance(UUID playerId, String currencyId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT balance FROM currency_balances WHERE player_uuid = ? AND currency_id = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, currencyId);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка получения баланса: " + e.getMessage());
                e.printStackTrace();
            }
            
            return 0.0;
        });
    }
    
    public CompletableFuture<Void> setBalance(UUID playerId, String currencyId, double amount) {
        return CompletableFuture.runAsync(() -> {
            String sql;
            
            if (storageType == StorageType.MYSQL) {
                sql = "INSERT INTO currency_balances (player_uuid, currency_id, balance) " +
                      "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE balance = ?";
            } else {
                sql = "INSERT OR REPLACE INTO currency_balances (player_uuid, currency_id, balance) " +
                      "VALUES (?, ?, ?)";
            }
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, playerId.toString());
                stmt.setString(2, currencyId);
                stmt.setDouble(3, amount);
                
                if (storageType == StorageType.MYSQL) {
                    stmt.setDouble(4, amount);
                }
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка установки баланса: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public CompletableFuture<Map<String, Double>> getAllBalances(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Double> balances = new HashMap<>();
            String sql = "SELECT currency_id, balance FROM currency_balances WHERE player_uuid = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, playerId.toString());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    balances.put(rs.getString("currency_id"), rs.getDouble("balance"));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка получения всех балансов: " + e.getMessage());
                e.printStackTrace();
            }
            
            return balances;
        });
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Соединение с базой данных закрыто");
        }
    }
    
    public StorageType getStorageType() {
        return storageType;
    }
}
