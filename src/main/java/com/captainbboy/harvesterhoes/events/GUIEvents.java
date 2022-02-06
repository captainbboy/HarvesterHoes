package com.captainbboy.harvesterhoes.events;

import com.captainbboy.harvesterhoes.GeneralUtil;
import com.captainbboy.harvesterhoes.HarvesterHoes;
import com.captainbboy.harvesterhoes.SQLite.SQLite;
import com.captainbboy.harvesterhoes.commands.UpgradeCommand;
import com.captainbboy.mobswords.MobSwords;
import com.captainbboy.mobswords.events.PlayerClickEvent;
import de.tr7zw.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GUIEvents implements Listener {

    private final HarvesterHoes plugin;

    public GUIEvents(HarvesterHoes plg) {
        this.plugin = plg;
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory().getTitle().equals(GeneralUtil.messageWithColorCode("&5&lHarvester Hoe Upgrades"))) {
            e.setCancelled(true);

            final ItemStack clickedItem = e.getCurrentItem();

            // verify current item is not null
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            final Player p = (Player) e.getWhoClicked();

            ItemStack hoe = p.getItemInHand();
            FileConfiguration config = this.plugin.getConfig();
            NBTItem nbti = new NBTItem(hoe);
            Economy eco = this.plugin.eco;

            if (!nbti.hasKey("isHarvHoe") || !nbti.getBoolean("isHarvHoe")) {
                p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("no-hoe-message")));
                return;
            }

            Integer hasteLevel = nbti.getInteger("harvHoeHasteLevel");
            Integer radiusLevel = nbti.getInteger("harvHoeRadiusLevel");
            if (radiusLevel == 0)
                radiusLevel = 1;
            Boolean autoSell = nbti.getBoolean("harvHoeAutoSell");
            Boolean autoSellEnabled = nbti.getBoolean("harvHoeAutoSellEnabled");
            Double sellMultiplier = nbti.getDouble("harvHoeSellMultiplier");
            if (sellMultiplier == 0.0)
                sellMultiplier = 1.0;

            ItemStack item = e.getCurrentItem();
            switch (item.getType()) {
                case STAINED_GLASS_PANE:
                    break;
                case GOLD_PICKAXE:
                    // Clicked Haste Upgrade
                    if (hasteLevel >= config.getInt("max-haste")) {
                        p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Haste")));
                        break;
                    }
                    Double hastePrice = UpgradeCommand.calculatePrice(config.getDouble("haste-price-start"), config.getDouble("haste-price-exponent-rate"), Double.valueOf(hasteLevel));
                    if (handlePurchase(p, "Haste", hastePrice)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel + 1, radiusLevel, autoSell, sellMultiplier, autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setInteger("harvHoeHasteLevel", hasteLevel + 1);
                        p.setItemInHand(nbti2.getItem());
                        plugin.getUpgradeCmd().showGUI(p.getItemInHand(), p);
                    }
                    break;
                case DIAMOND_PICKAXE:
                    // Clicked Radius Upgrade
                    if (radiusLevel >= config.getInt("max-radius")) {
                        p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Radius")));
                        break;
                    }
                    Double radiusPrice = UpgradeCommand.calculatePrice(config.getDouble("radius-price-start"), config.getDouble("radius-price-exponent-rate"), Double.valueOf(radiusLevel));
                    if (handlePurchase(p, "Radius", radiusPrice)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel, radiusLevel + 1, autoSell, sellMultiplier, autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setInteger("harvHoeRadiusLevel", radiusLevel + 1);
                        p.setItemInHand(nbti2.getItem());
                        plugin.getUpgradeCmd().showGUI(p.getItemInHand(), p);
                    }
                    break;
                case DIAMOND_HOE:
                    // Clicked AutoSell Upgrade
                    if (autoSell == true) {
                        p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "AutoSell")));
                        break;
                    }
                    Double autoSellPrice = config.getDouble("autosell-price");
                    if (handlePurchase(p, "AutoSell", autoSellPrice)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel, radiusLevel, true, sellMultiplier, autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setBoolean("harvHoeAutoSell", true);
                        p.setItemInHand(nbti2.getItem());
                        plugin.getUpgradeCmd().showGUI(p.getItemInHand(), p);
                    }
                    break;
                case GOLD_HOE:
                    // Clicked Sell Multiplier Upgrade
                    if (sellMultiplier >= config.getDouble("max-sell-multiplier")) {
                        p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Sell Multiplier")));
                        break;
                    }
                    Double sellMultIncr = config.getDouble("sell-multiplier-increment");
                    Double sellMultPrice = UpgradeCommand.calculatePrice(config.getDouble("sell-multiplier-price-start"), config.getDouble("sell-multiplier-price-exponent-rate"), GeneralUtil.roundToHundredths((sellMultiplier - 1.0) / sellMultIncr));
                    if (handlePurchase(p, "Sell Multiplier", sellMultPrice)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel, radiusLevel, autoSell, GeneralUtil.roundToHundredths(sellMultiplier + config.getDouble("sell-multiplier-increment")), autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setDouble("harvHoeSellMultiplier", GeneralUtil.roundToHundredths(sellMultiplier + config.getDouble("sell-multiplier-increment")));
                        p.setItemInHand(nbti2.getItem());
                        plugin.getUpgradeCmd().showGUI(p.getItemInHand(), p);
                    }
                    break;
            }
        } else if (e.getInventory().getTitle().equals(GeneralUtil.messageWithColorCode("&6&lUpgrade Menu"))) {
            e.setCancelled(true);

            final ItemStack clickedItem = e.getCurrentItem();

            // verify current item is not null
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            PluginManager pluginManager = this.plugin.getServer().getPluginManager();
            if(pluginManager.isPluginEnabled("MobSwords")) {
                if (pluginManager.getPlugin("MobSwords") instanceof MobSwords) {
                    MobSwords mobSwords = (MobSwords) pluginManager.getPlugin("MobSwords");
                    FileConfiguration config = mobSwords.getConfig();

                    Material material;
                    try {
                        material = Material.getMaterial(config.getString("mob-sword-item-type"));
                    } catch (Exception e2) {
                        material = Material.DIAMOND_SWORD;
                    }

                    if (clickedItem.getType() == Material.DIAMOND_HOE) {
                        if (e.getWhoClicked() instanceof Player) {
                            e.getWhoClicked().closeInventory();
                            if(e.getWhoClicked().getItemInHand() == null || e.getWhoClicked().getItemInHand().getType() == Material.AIR) {
                                e.getWhoClicked().sendMessage(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("no-hoe-message")));
                                return;
                            }
                            plugin.getUpgradeCmd().showGUI(e.getWhoClicked().getItemInHand(), (Player) e.getWhoClicked());
                        }
                    } else if (clickedItem.getType() == material) {
                        if (e.getWhoClicked() instanceof Player) {
                            e.getWhoClicked().closeInventory();
                            if(e.getWhoClicked().getItemInHand() == null || e.getWhoClicked().getItemInHand().getType() == Material.AIR) {
                                e.getWhoClicked().sendMessage(GeneralUtil.messageWithColorCode(config.getString("no-sword-message")));
                                return;
                            }
                            NBTItem nbtItem = new NBTItem(e.getWhoClicked().getItemInHand());
                            if(nbtItem.hasKey("isMobSword") && nbtItem.getBoolean("isMobSword")) {
                                PlayerClickEvent.promptUpgradeMenu(mobSwords, (Player) e.getWhoClicked(), nbtItem);
                            } else {
                                e.getWhoClicked().sendMessage(GeneralUtil.messageWithColorCode(config.getString("no-sword-message")));
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().getTitle().equals(GeneralUtil.messageWithColorCode("&5&lHarvester Hoe Upgrades")) || e.getInventory().getTitle().equals(GeneralUtil.messageWithColorCode("&6&lUpgrade Menu"))) {
            e.setCancelled(true);
        }
    }

    private boolean handlePurchase(Player p, String type, Double price) {
        FileConfiguration config = this.plugin.getConfig();
        SQLite db = this.plugin.getSQLite();

        Double bal = GeneralUtil.getNumber(db.getBalance(p.getUniqueId()));
        if(bal < price) {
            p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("too-poor-message")));
            return false;
        }
        db.setBalance(p.getUniqueId(), bal - price);
        String message = GeneralUtil.messageWithColorCode(config.getString("successful-upgrade-purchase-message"));
        message = message.replaceAll("\\{type}", type);
        message = message.replaceAll("\\{price}", GeneralUtil.formatNumber(price));
        p.sendMessage(message);
        return true;
    }

    private ItemStack handleItem(ItemStack hoe, Integer hasteLevel, Integer radiusLevel, Boolean autoSell, Double sellMultiplier, Boolean autoSellEnabled) {
        ItemMeta meta = hoe.getItemMeta();
        meta.setDisplayName(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("item-name")));
        List<String> lores = new ArrayList();

        for(String s : this.plugin.getConfig().getStringList("item-lore")) {
            s = s.replaceAll("\\{hasteValue}", String.valueOf(hasteLevel));
            s = s.replaceAll("\\{radiusValue}", String.valueOf(radiusLevel));
            s = s.replaceAll("\\{autosellValue}", String.valueOf(autoSell));
            if(s.contains("{autosellEnabled}")) {
                if(autoSellEnabled)
                    s = s.replaceAll("\\{autosellEnabled}", "enabled");
                else
                    s = s.replaceAll("\\{autosellEnabled}", "disabled");
            }
            s = s.replaceAll("\\{sellMultiplierValue}", String.valueOf(sellMultiplier));
            lores.add(GeneralUtil.messageWithColorCode(s));
        }
        meta.setLore(lores);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        meta.spigot().setUnbreakable(true);
        hoe.setItemMeta(meta);

        return hoe;
    }

}
