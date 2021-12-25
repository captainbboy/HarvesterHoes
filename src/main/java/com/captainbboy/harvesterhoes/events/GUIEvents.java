package com.captainbboy.harvesterhoes.events;

import com.captainbboy.harvesterhoes.GeneralUtil;
import com.captainbboy.harvesterhoes.HarvesterHoes;
import com.captainbboy.harvesterhoes.commands.UpgradeCommand;
import de.tr7zw.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GUIEvents implements Listener {

    HarvesterHoes plugin;

    public GUIEvents(HarvesterHoes plg) {
        this.plugin = plg;
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().getTitle().equals(GeneralUtil.messageWithColorCode("&5&lHarvester Hoe Upgrades"))) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();

        ItemStack hoe = p.getItemInHand();
        FileConfiguration config = this.plugin.getConfig();
        NBTItem nbti = new NBTItem(hoe);
        Economy eco = this.plugin.eco;

        if (nbti.hasKey("isHarvHoe") && nbti.getBoolean("isHarvHoe")) {
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
            if (item.getType() == Material.STAINED_GLASS_PANE)
                return;
            if (item.getType() == Material.GOLD_PICKAXE) {
                // Clicked Haste Upgrade
                if(hasteLevel >= config.getInt("max-haste")) {
                    p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Haste")));
                } else {
                    Double price = UpgradeCommand.calculatePrice(config.getDouble("haste-price-start"), config.getDouble("haste-price-exponent-rate"), Double.valueOf(hasteLevel));
                    if(handlePurchase(p, "Haste", price)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel + 1, radiusLevel, autoSell, sellMultiplier, autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setInteger("harvHoeHasteLevel", hasteLevel + 1);
                        p.setItemInHand(nbti2.getItem());
                    };
                }
            } else if (item.getType() == Material.DIAMOND_PICKAXE) {
                // Clicked Radius Upgrade
                if(radiusLevel >= config.getInt("max-radius")) {
                    p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Radius")));
                } else {
                    Double price = UpgradeCommand.calculatePrice(config.getDouble("radius-price-start"), config.getDouble("radius-price-exponent-rate"), Double.valueOf(radiusLevel));
                    if(handlePurchase(p, "Radius", price)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel, radiusLevel + 1, autoSell, sellMultiplier, autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setInteger("harvHoeRadiusLevel", radiusLevel + 1);
                        p.setItemInHand(nbti2.getItem());
                    };
                }
            } else if (item.getType() == Material.DIAMOND_HOE) {
                // Clicked AutoSell Upgrade
                if(autoSell == true) {
                    p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "AutoSell")));
                } else {
                    Double price = config.getDouble("auto-sell-price");
                    if(handlePurchase(p, "AutoSell", price)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel, radiusLevel, true, sellMultiplier, autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setBoolean("harvHoeAutoSell", true);
                        p.setItemInHand(nbti2.getItem());
                    };
                }
            } else if (item.getType() == Material.GOLD_HOE) {
                // Clicked Sell Multiplier Upgrade
                if(sellMultiplier >= config.getDouble("max-sell-multiplier")) {
                    p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("stat-already-maxed-message").replaceAll("\\{type}", "Sell Multiplier")));
                } else {
                    Double sellMultIncr = config.getDouble("sell-multiplper-increment");
                    Double price = UpgradeCommand.calculatePrice(config.getDouble("sell-multiplier-price-start"), config.getDouble("sell-multiplier-price-exponent-rate"), GeneralUtil.roundToHundredths((sellMultiplier - 1.0)/sellMultIncr));
                    if(handlePurchase(p, "Radius", price)) {
                        ItemStack newHoe = handleItem(hoe, hasteLevel, radiusLevel, autoSell, GeneralUtil.roundToHundredths(sellMultiplier + config.getDouble("sell-multiplper-increment")), autoSellEnabled);
                        NBTItem nbti2 = new NBTItem(newHoe);
                        nbti2.setDouble("harvHoeSellMultiplier", GeneralUtil.roundToHundredths(sellMultiplier + config.getDouble("sell-multiplper-increment")));
                        p.setItemInHand(nbti2.getItem());
                    };
                }
            }
        } else {
            p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("no-hoe-message")));
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().getTitle().equals(GeneralUtil.messageWithColorCode("&5&lHarvester Hoe Upgrades"))) {
            e.setCancelled(true);
        }
    }

    private boolean handlePurchase(Player p, String type, Double price) {
        FileConfiguration config = this.plugin.getConfig();
        Economy eco = this.plugin.eco;

        if(eco.getBalance(p) < price) {
            p.sendMessage(GeneralUtil.messageWithColorCode(config.getString("too-poor-message")));
            return false;
        } else {
            eco.withdrawPlayer(p, price);
            String message = GeneralUtil.messageWithColorCode(config.getString("successful-upgrade-purchase-message"));
            message = message.replaceAll("\\{type}", type);
            message = message.replaceAll("\\{price}", String.valueOf(price));
            p.sendMessage(message);
            p.closeInventory();
            return true;
        }
    }

    private ItemStack handleItem(ItemStack hoe, Integer hasteLevel, Integer radiusLevel, Boolean autoSell, Double sellMultiplier, Boolean autoSellEnabled) {
        ItemMeta meta = hoe.getItemMeta();
        meta.setDisplayName(GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("item-name")));
        List<String> lores = new ArrayList();
        Iterator var9 = this.plugin.getConfig().getStringList("item-lore").iterator();

        while(var9.hasNext()) {
            String s = (String)var9.next();
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
