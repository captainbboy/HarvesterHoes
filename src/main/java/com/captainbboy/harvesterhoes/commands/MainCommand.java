package com.captainbboy.harvesterhoes.commands;

import com.captainbboy.harvesterhoes.GeneralUtil;
import com.captainbboy.harvesterhoes.HarvesterHoes;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainCommand implements CommandExecutor {

    HarvesterHoes plugin;

    public MainCommand(HarvesterHoes plg) {
        plugin = plg;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("harvesterhoe")) {
            if (args.length == 0) {
                sender.sendMessage(GeneralUtil.messageWithColorCode("&8&m------------------------------------"));
                sender.sendMessage("");
                sender.sendMessage(GeneralUtil.messageWithColorCode("          &7Version &a["+this.plugin.currVersion+"] &7by &a&ncaptain_bboy"));
                sender.sendMessage("");
                sender.sendMessage(GeneralUtil.messageWithColorCode("&e&l(!) &7/harvesterhoe give &e<player> [haste] [radius] [autosell] [sellmult]"));
                sender.sendMessage(GeneralUtil.messageWithColorCode("&e&l(!) &7/harvesterhoe balance"));
                sender.sendMessage("");
                sender.sendMessage(GeneralUtil.messageWithColorCode("&8&m------------------------------------"));
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
                if (args[1] == null || args[1].equals("")) {
                    sender.sendMessage(GeneralUtil.messageWithColorCode("&8&m------------------------------------"));
                    sender.sendMessage("");
                    sender.sendMessage(GeneralUtil.messageWithColorCode("          &7Version &a["+this.plugin.currVersion+"] &7by &a&ncaptain_bboy"));
                    sender.sendMessage("");
                    sender.sendMessage(GeneralUtil.messageWithColorCode("&e&l(!) &7/harvesterhoe give &e<player> [haste] [radius] [autosell] [sellmult]"));
                    sender.sendMessage("");
                    sender.sendMessage(GeneralUtil.messageWithColorCode("&8&m------------------------------------"));
                }

                if (sender.hasPermission("harvesterhoe.give")) {
                    Player target = this.plugin.getServer().getPlayer(args[1]);
                    sender.sendMessage(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("item-sent-message")));
                    target.sendMessage(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("item-recieved-message")));
                    if (target != null) {
                        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE, 1);
                        ItemMeta meta = hoe.getItemMeta();
                        meta.setDisplayName(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("item-name")));
                        List<String> lores = new ArrayList();
                        Iterator var9 = this.plugin.getConfig().getStringList("item-lore").iterator();

                        Integer hasteValue = 0;
                        if(args.length > 2 && args[2] != null && GeneralUtil.isNumeric(args[2])) {
                            hasteValue = (int)GeneralUtil.getNumber(args[2]);
                        }

                        Integer radiusValue = 1;
                        if(args.length > 3 && args[3] != null && GeneralUtil.isNumeric(args[3])) {
                            radiusValue = (int)GeneralUtil.getNumber(args[3]);
                        }

                        boolean autosellValue = false;
                        if(args.length > 4 && args[4] != null && GeneralUtil.isBoolean(args[4])) {
                            autosellValue = GeneralUtil.getBoolean(args[4]);
                        }

                        Double sellMultiplierValue = 1.0;
                        if(args.length > 5 && args[5] != null && GeneralUtil.isNumeric(args[5])) {
                            sellMultiplierValue = GeneralUtil.getNumber(args[5]);
                        }

                        Boolean autoSellEnabled = false;

                        while(var9.hasNext()) {
                            String s = (String)var9.next();
                            s = s.replaceAll("\\{hasteValue}", String.valueOf(hasteValue));
                            s = s.replaceAll("\\{radiusValue}", String.valueOf(radiusValue));
                            s = s.replaceAll("\\{autosellValue}", String.valueOf(autosellValue));
                            if(s.contains("{autosellEnabled}")) {
                                if(autoSellEnabled)
                                    s = s.replaceAll("\\{autosellEnabled}", "enabled");
                                else
                                    s = s.replaceAll("\\{autosellEnabled}", "disabled");
                            }
                            s = s.replaceAll("\\{sellMultiplierValue}", String.valueOf(sellMultiplierValue));
                            lores.add(GeneralUtil.messageWithColorCode(s));
                        }

                        meta.setLore(lores);
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
                        meta.spigot().setUnbreakable(true);
                        hoe.setItemMeta(meta);

                        NBTItem nbti = new NBTItem(hoe);
                        nbti.setBoolean("isHarvHoe", true);
                        nbti.setInteger("harvHoeHasteLevel", hasteValue);
                        nbti.setInteger("harvHoeRadiusLevel", radiusValue);
                        nbti.setBoolean("harvHoeAutoSell", autosellValue);
                        nbti.setBoolean("harvHoeAutoSellEnabled", false);
                        nbti.setDouble("harvHoeSellMultiplier", sellMultiplierValue);

                        target.getInventory().addItem(new ItemStack[]{nbti.getItem()});
                    }
                } else {
                    sender.sendMessage(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("no-permission-message")));
                }
            } else if (args.length >= 1 && (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal"))) {
                if (sender instanceof Player) {
                    String msg = this.plugin.getConfig().getString("balance-message");
                    msg = msg.replaceAll("\\{amount}", GeneralUtil.formatNumber(this.plugin.getSQLite().getBalance(((Player) sender).getUniqueId())));
                    msg = msg.replaceAll("\\{customCurrency}", this.plugin.getConfig().getString("name-of-harvesterhoe-currency"));
                    sender.sendMessage(GeneralUtil.messageWithColorCode(msg));
                }
            } else {
                sender.sendMessage(GeneralUtil.messageWithColorCode("&8&m------------------------------------"));
                sender.sendMessage("");
                sender.sendMessage(GeneralUtil.messageWithColorCode("          &7Version &a["+this.plugin.currVersion+"] &7by &a&ncaptain_bboy"));
                sender.sendMessage("");
                sender.sendMessage(GeneralUtil.messageWithColorCode("&e&l(!) &7/harvesterhoe give &e<player> [haste] [radius] [autosell] [sellmult]"));
                sender.sendMessage(GeneralUtil.messageWithColorCode("&e&l(!) &7/harvesterhoe balance"));
                sender.sendMessage("");
                sender.sendMessage(GeneralUtil.messageWithColorCode("&8&m------------------------------------"));
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("harvesterhoe.reload")) {
            this.plugin.reloadConfig();
            this.plugin.saveConfig();
            sender.sendMessage(GeneralUtil.messageWithColorCode("&e&l(!) &7Configuration Reloaded!"));
        }

        return true;
    }

}
