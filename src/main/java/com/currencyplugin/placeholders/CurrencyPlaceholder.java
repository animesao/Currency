package com.currencyplugin.placeholders;

import com.currencyplugin.CurrencyPlugin;
import com.currencyplugin.managers.Currency;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class CurrencyPlaceholder extends PlaceholderExpansion {
    
    private final CurrencyPlugin plugin;
    
    public CurrencyPlaceholder(CurrencyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "currency";
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return "CurrencyPlugin";
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        Currency currency = plugin.getCurrencyManager().getCurrencyByPlaceholder(params);
        if (currency == null) {
            return "Unknown currency: " + params;
        }
        
        double balance = plugin.getDataManager().getBalance(player.getUniqueId(), currency.getId());
        return String.format("%.2f", balance);
    }
}
