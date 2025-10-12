package com.currencyplugin.commands;

import com.currencyplugin.CurrencyPlugin;
import com.currencyplugin.managers.Currency;
import com.currencyplugin.managers.DataManager;
import com.currencyplugin.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyCommand extends Command {
    
    private final CurrencyPlugin plugin;
    private final Currency currency;
    private final DataManager dataManager;
    
    public CurrencyCommand(CurrencyPlugin plugin, Currency currency) {
        super(currency.getCommand());
        this.plugin = plugin;
        this.currency = currency;
        this.dataManager = plugin.getDataManager();
        
        setDescription("Команды для валюты " + currency.getDisplayName());
        setUsage("/" + currency.getCommand() + " <balance|give|take|pay|set>");
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtils.colorize("&cЭта команда только для игроков!"));
                return true;
            }
            
            Player player = (Player) sender;
            if (!player.hasPermission(currency.getPermission("view"))) {
                player.sendMessage(ColorUtils.colorize(currency.getMessage("no-permission")));
                return true;
            }
            
            double balance = dataManager.getBalance(player.getUniqueId(), currency.getId());
            String message = currency.getMessage("balance")
                    .replace("{amount}", String.format("%.2f", balance));
            player.sendMessage(ColorUtils.colorize(message));
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "balance":
            case "bal":
                return handleBalance(sender, args);
            case "give":
                return handleGive(sender, args);
            case "take":
                return handleTake(sender, args);
            case "pay":
                return handlePay(sender, args);
            case "set":
                return handleSet(sender, args);
            default:
                sender.sendMessage(ColorUtils.colorize("&cИспользование: " + getUsage()));
                return true;
        }
    }
    
    private boolean handleBalance(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtils.colorize("&cУкажите имя игрока!"));
                return true;
            }
            
            Player player = (Player) sender;
            double balance = dataManager.getBalance(player.getUniqueId(), currency.getId());
            String message = currency.getMessage("balance")
                    .replace("{amount}", String.format("%.2f", balance));
            player.sendMessage(ColorUtils.colorize(message));
            return true;
        }
        
        if (!sender.hasPermission(currency.getPermission("view"))) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("no-permission")));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("player-not-found")));
            return true;
        }
        
        double balance = dataManager.getBalance(target.getUniqueId(), currency.getId());
        String message = currency.getMessage("balance-other")
                .replace("{player}", target.getName())
                .replace("{amount}", String.format("%.2f", balance));
        sender.sendMessage(ColorUtils.colorize(message));
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission(currency.getPermission("give"))) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("no-permission")));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cИспользование: /" + currency.getCommand() + " give <игрок> <сумма>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("player-not-found")));
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("invalid-amount")));
            return true;
        }
        
        dataManager.addBalance(target.getUniqueId(), currency.getId(), amount);
        
        String senderMessage = currency.getMessage("give-success")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{player}", target.getName());
        sender.sendMessage(ColorUtils.colorize(senderMessage));
        
        String targetMessage = currency.getMessage("give-received")
                .replace("{amount}", String.format("%.2f", amount));
        target.sendMessage(ColorUtils.colorize(targetMessage));
        
        return true;
    }
    
    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission(currency.getPermission("take"))) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("no-permission")));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cИспользование: /" + currency.getCommand() + " take <игрок> <сумма>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("player-not-found")));
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("invalid-amount")));
            return true;
        }
        
        dataManager.removeBalance(target.getUniqueId(), currency.getId(), amount);
        
        String senderMessage = currency.getMessage("take-success")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{player}", target.getName());
        sender.sendMessage(ColorUtils.colorize(senderMessage));
        
        String targetMessage = currency.getMessage("take-removed")
                .replace("{amount}", String.format("%.2f", amount));
        target.sendMessage(ColorUtils.colorize(targetMessage));
        
        return true;
    }
    
    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&cЭта команда только для игроков!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission(currency.getPermission("pay"))) {
            player.sendMessage(ColorUtils.colorize(currency.getMessage("no-permission")));
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage(ColorUtils.colorize("&cИспользование: /" + currency.getCommand() + " pay <игрок> <сумма>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ColorUtils.colorize(currency.getMessage("player-not-found")));
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage(ColorUtils.colorize("&cНельзя передать валюту самому себе!"));
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize(currency.getMessage("invalid-amount")));
            return true;
        }
        
        if (!dataManager.hasBalance(player.getUniqueId(), currency.getId(), amount)) {
            double balance = dataManager.getBalance(player.getUniqueId(), currency.getId());
            String message = currency.getMessage("insufficient-funds")
                    .replace("{balance}", String.format("%.2f", balance));
            player.sendMessage(ColorUtils.colorize(message));
            return true;
        }
        
        dataManager.removeBalance(player.getUniqueId(), currency.getId(), amount);
        dataManager.addBalance(target.getUniqueId(), currency.getId(), amount);
        
        String senderMessage = currency.getMessage("pay-success")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{player}", target.getName());
        player.sendMessage(ColorUtils.colorize(senderMessage));
        
        String targetMessage = currency.getMessage("pay-received")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{player}", player.getName());
        target.sendMessage(ColorUtils.colorize(targetMessage));
        
        return true;
    }
    
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission(currency.getPermission("set"))) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("no-permission")));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cИспользование: /" + currency.getCommand() + " set <игрок> <сумма>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("player-not-found")));
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.colorize(currency.getMessage("invalid-amount")));
            return true;
        }
        
        dataManager.setBalance(target.getUniqueId(), currency.getId(), amount);
        
        String message = currency.getMessage("set-success")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{player}", target.getName());
        sender.sendMessage(ColorUtils.colorize(message));
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> actions = Arrays.asList("balance", "give", "take", "pay", "set");
            completions.addAll(actions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("balance")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("give") || 
                                        args[0].equalsIgnoreCase("take") || 
                                        args[0].equalsIgnoreCase("pay") ||
                                        args[0].equalsIgnoreCase("set"))) {
            completions.add("<сумма>");
        }
        
        return completions;
    }
}
