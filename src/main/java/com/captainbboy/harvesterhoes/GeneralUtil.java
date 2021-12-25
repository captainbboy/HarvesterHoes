package com.captainbboy.harvesterhoes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GeneralUtil {

    public static String messageWithColorCode(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void givePlayerItem(Player player, Material m) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(new ItemStack[]{new ItemStack(m)});
        } else if (getSlot(player, m) != -1) {
            player.getInventory().addItem(new ItemStack[]{new ItemStack(m)});
        } else {
            player.sendMessage(messageWithColorCode("&a&l(!) &7Your &ainventory &7is full!"));
            player.getWorld().dropItem(player.getLocation(), new ItemStack(m));
        }

    }

    public static int getSlot(Player p, Material m) {
        for(int i = 0; i < p.getInventory().getSize(); ++i) {
            if (p.getInventory().getItem(i).getType() == m && p.getInventory().getItem(i).getAmount() < p.getInventory().getItem(i).getMaxStackSize()) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static double getNumber(String strNum) {
        double d;
        try {
            d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return 0.0d;
        }
        return d;
    }

    public static boolean isBoolean(String str) {
        if(str.equalsIgnoreCase("false") || str.equalsIgnoreCase("true"))
            return true;
        return false;
    }

    public static boolean getBoolean(String str) {
        if(str.equalsIgnoreCase("true"))
            return true;
        return false;
    }

    public static Double roundToHundredths(Double x) {
        return(0.01 * Math.floor(x * 100.0));
    }

    public static Double roundToHundred(double input) {
        long i = (long) Math.ceil(input);
        return Double.valueOf(((i + 99) / 100) * 100);
    };

}
