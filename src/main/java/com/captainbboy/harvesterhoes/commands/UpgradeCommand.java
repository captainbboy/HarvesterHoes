package com.captainbboy.harvesterhoes.commands;

import com.captainbboy.harvesterhoes.GeneralUtil;
import com.captainbboy.harvesterhoes.HarvesterHoes;
import com.captainbboy.harvesterhoes.SQLite.SQLite;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeCommand implements CommandExecutor {

    HarvesterHoes plugin;

    public UpgradeCommand(HarvesterHoes plg) {
        plugin = plg;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("upgrade")) {
            if(sender instanceof Player) {
                Player p = (Player) sender;
                NBTItem nbti = new NBTItem(p.getItemInHand());
                if (nbti.hasKey("isHarvHoe") && nbti.getBoolean("isHarvHoe")) {
                    Integer hasteLevel = nbti.getInteger("harvHoeHasteLevel");
                    if(hasteLevel == null)
                        hasteLevel = 0;
                    Integer radiusLevel = nbti.getInteger("harvHoeRadiusLevel");
                    if(radiusLevel == 0)
                        radiusLevel = 1;
                    Boolean autoSell = nbti.getBoolean("harvHoeAutoSell");
                    Double sellMultiplier = nbti.getDouble("harvHoeSellMultiplier");
                    if(sellMultiplier == 0.0)
                        sellMultiplier = 1.0;

                    Inventory inv = Bukkit.createInventory(p, InventoryType.CHEST, GeneralUtil.messageWithColorCode("&5&lHarvester Hoe Upgrades"));
                    ItemStack[] items = inv.getContents();
                    for (int i = 0; i < items.length; i++) {
                        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
                        items[i] = glass;
                    }

                    // Get config values
                    FileConfiguration config = this.plugin.getConfig();
                    String baseName = config.getString("upgrade-item-name");
                    List<String> baseLore = config.getStringList("upgrade-item-lore");

                    // Make haste upgrader
                    ItemStack hasteUpgrade = new ItemStack(Material.GOLD_PICKAXE, 1);
                    ItemMeta hasteMeta = hasteUpgrade.getItemMeta();
                    hasteMeta.addEnchant(Enchantment.DURABILITY, 3, true);
                    hasteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    hasteMeta.setDisplayName(GeneralUtil.messageWithColorCode(baseName.replaceAll("\\{type}", "Haste")));
                    List<String> hasteLore =  new ArrayList<>();
                    Double hasteUpgradePrice = calculatePrice(config.getDouble("haste-price-start"), config.getDouble("haste-price-exponent-rate"), Double.valueOf(hasteLevel));
                    for (String s : baseLore) {
                        s = s.replaceAll("\\{currentValue}", String.valueOf(hasteLevel));
                        if (s.contains("{nextValue}")) {
                            if (hasteLevel >= config.getInt("max-haste")) {
                                s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                            } else {
                                s = s.replaceAll("\\{nextValue}", String.valueOf(hasteLevel + 1));
                            }
                        }
                        s = s.replaceAll("\\{price}", GeneralUtil.formatNumber(hasteUpgradePrice));
                        hasteLore.add(GeneralUtil.messageWithColorCode(s));
                    }
                    hasteMeta.setLore(hasteLore);
                    hasteUpgrade.setItemMeta(hasteMeta);
                    items[10] = hasteUpgrade;

                    // Make radius upgrader
                    ItemStack radiusUpgrade = new ItemStack(Material.DIAMOND_PICKAXE, 1);
                    ItemMeta radiusMeta = radiusUpgrade.getItemMeta();
                    radiusMeta.addEnchant(Enchantment.DURABILITY, 3, true);
                    radiusMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    radiusMeta.setDisplayName(GeneralUtil.messageWithColorCode(baseName.replaceAll("\\{type}", "Radius")));
                    List<String> radiusLore =  new ArrayList<>();
                    Double radiusUpgradePrice = calculatePrice(config.getDouble("radius-price-start"), config.getDouble("radius-price-exponent-rate"), Double.valueOf(radiusLevel));
                    for (String s : baseLore) {
                        s = s.replaceAll("\\{currentValue}", String.valueOf(radiusLevel));
                        if(s.contains("{nextValue}")) {
                            if(radiusLevel >= config.getInt("max-radius")) {
                                s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                            } else {
                                s = s.replaceAll("\\{nextValue}", String.valueOf(radiusLevel + 1));
                            }
                        }
                        s = s.replaceAll("\\{price}", GeneralUtil.formatNumber(radiusUpgradePrice));
                        radiusLore.add(GeneralUtil.messageWithColorCode(s));
                    }
                    radiusMeta.setLore(radiusLore);
                    radiusUpgrade.setItemMeta(radiusMeta);
                    items[12] = radiusUpgrade;

                    // Make autosell upgrader
                    ItemStack autoSellUpgrade = new ItemStack(Material.DIAMOND_HOE, 1);
                    ItemMeta autoSellMeta = autoSellUpgrade.getItemMeta();
                    autoSellMeta.addEnchant(Enchantment.DURABILITY, 3, true);
                    autoSellMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    autoSellMeta.setDisplayName(GeneralUtil.messageWithColorCode(baseName.replaceAll("\\{type}", "AutoSell")));
                    List<String> autoSellLore =  new ArrayList<>();
                    for (String s : baseLore) {
                        s = s.replaceAll("\\{currentValue}", String.valueOf(autoSell));
                        if(s.contains("{nextValue}")) {
                            if(autoSell == true) {
                                s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                            } else {
                                s = s.replaceAll("\\{nextValue}", "true");
                            }
                        }
                        s = s.replaceAll("\\{price}", GeneralUtil.formatNumber(config.getDouble("autosell-price")));
                        autoSellLore.add(GeneralUtil.messageWithColorCode(s));
                    }
                    autoSellMeta.setLore(autoSellLore);
                    autoSellUpgrade.setItemMeta(autoSellMeta);
                    items[14] = autoSellUpgrade;

                    // Make sell multiplier upgrader
                    ItemStack sellMultUpgrade = new ItemStack(Material.GOLD_HOE, 1);
                    ItemMeta sellMultMeta = sellMultUpgrade.getItemMeta();
                    sellMultMeta.addEnchant(Enchantment.DURABILITY, 3, true);
                    sellMultMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    sellMultMeta.setDisplayName(GeneralUtil.messageWithColorCode(baseName.replaceAll("\\{type}", "Sell Multiplier")));
                    Double sellMultIncr = config.getDouble("sell-multiplier-increment");
                    Double sellMultUpgradePrice = calculatePrice(config.getDouble("sell-multiplier-price-start"), config.getDouble("sell-multiplier-price-exponent-rate"), (sellMultiplier - 1.0)/sellMultIncr);
                    List<String> sellMultLore =  new ArrayList<>();
                    for (String s : baseLore) {
                        s = s.replaceAll("\\{currentValue}", String.valueOf(sellMultiplier));
                        if(s.contains("{nextValue}")) {
                            if(sellMultiplier >= config.getDouble("max-sell-multiplier")) {
                                s = s.replaceAll("\\{nextValue}", "Max Value Reached");
                            } else {
                                s = s.replaceAll("\\{nextValue}", String.valueOf(GeneralUtil.roundToHundredths(sellMultiplier + sellMultIncr)));
                            }
                        }
                        s = s.replaceAll("\\{price}", GeneralUtil.formatNumber(sellMultUpgradePrice));
                        sellMultLore.add(GeneralUtil.messageWithColorCode(s));
                    }
                    sellMultMeta.setLore(sellMultLore);
                    sellMultUpgrade.setItemMeta(sellMultMeta);
                    items[16] = sellMultUpgrade;

                    // Make currency show-er
                    ItemStack currencyShow = new ItemStack(Material.SUGAR_CANE, 1);
                    ItemMeta currencyShowMeta = currencyShow.getItemMeta();
                    currencyShowMeta.addEnchant(Enchantment.DURABILITY, 3, true);
                    currencyShowMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                    Double currencyValue = GeneralUtil.getNumber(this.plugin.getSQLite().getBalance(p.getUniqueId()));

                    String baseCurrencyName = config.getString("currency-item-name");
                    String currencyName = config.getString("name-of-harvesterhoe-currency");

                    List<String> baseCurrencyLore = config.getStringList("currency-item-lore");
                    List<String> currencyShowLore =  new ArrayList<>();
                    for (String s : baseCurrencyLore) {
                        s = s.replaceAll("\\{currentValue}", String.valueOf(GeneralUtil.roundToHundredths(currencyValue)));
                        s = s.replaceAll("\\{currency}", currencyName);
                        currencyShowLore.add(GeneralUtil.messageWithColorCode(s));
                    }
                    currencyShowMeta.setDisplayName(GeneralUtil.messageWithColorCode(baseCurrencyName.replaceAll("\\{currency}", currencyName)));
                    currencyShowMeta.setLore(currencyShowLore);
                    currencyShow.setItemMeta(currencyShowMeta);
                    items[18] = currencyShow;
                    
                    inv.setContents(items);
                    p.openInventory(inv);
                } else {
                    sender.sendMessage(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("no-hoe-message")));
                }
            } else {
                sender.sendMessage(GeneralUtil.messageWithColorCode("&c&l(!) &cOnly players can run this command!"));
            }
        }
        return true;
    }

    public static Double calculatePrice(Double startPrice, Double exponentRate, Double currentLevel) {
        return GeneralUtil.roundToHundred(startPrice * Math.pow(exponentRate + 1.0, currentLevel));
    }

}
