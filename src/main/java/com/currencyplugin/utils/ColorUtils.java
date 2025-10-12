package com.currencyplugin.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static final Pattern RGB_PATTERN = Pattern.compile("<rgb\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)>");
    
    public static String colorize(String text) {
        if (text == null) return "";
        
        text = translateHexColorCodes(text);
        text = translateRgbColorCodes(text);
        text = ChatColor.translateAlternateColorCodes('&', text);
        
        return text;
    }
    
    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    private static String translateRgbColorCodes(String message) {
        Matcher matcher = RGB_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            try {
                int r = Integer.parseInt(matcher.group(1));
                int g = Integer.parseInt(matcher.group(2));
                int b = Integer.parseInt(matcher.group(3));
                
                if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                    String hex = String.format("#%02X%02X%02X", r, g, b);
                    matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of(hex).toString());
                } else {
                    matcher.appendReplacement(buffer, matcher.group(0));
                }
            } catch (NumberFormatException e) {
                matcher.appendReplacement(buffer, matcher.group(0));
            }
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
