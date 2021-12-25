package com.captainbboy.harvesterhoes.events;

import com.captainbboy.harvesterhoes.GeneralUtil;
import com.captainbboy.harvesterhoes.HarvesterHoes;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerItemEvent implements Listener {

    HarvesterHoes plugin;

    public PlayerItemEvent(HarvesterHoes plg) {
        plugin = plg;
    }

    @EventHandler
    public void onPlayerItemInteractEvent(PlayerInteractEvent e) {
        if(e.getPlayer().isSneaking() && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            ItemStack item = e.getPlayer().getItemInHand();
            NBTItem nbti = new NBTItem(item);
            if (nbti.hasKey("isHarvHoe") && nbti.getBoolean("isHarvHoe")) {
                if(nbti.hasKey("harvHoeAutoSell") && nbti.getBoolean("harvHoeAutoSell")) {
                    Boolean oldValue = nbti.getBoolean("harvHoeAutoSellEnabled");
                    nbti.setBoolean("harvHoeAutoSellEnabled", !oldValue);
                    ItemStack item2 = nbti.getItem();
                    String message = GeneralUtil.messageWithColorCode(this.plugin.getConfig().getString("toggle-autosell-message"));
                    if(!oldValue) {
                        message = message.replaceAll("\\{value}", "enabled");
                    } else {
                        message = message.replaceAll("\\{value}", "disabled");
                    }
                    ItemMeta itemMeta = item2.getItemMeta();
                    List<String> lore = itemMeta.getLore();
                    List<String> newLore = new ArrayList<>();
                    for(String s : lore) {
                        if(s.contains("disabled")) {
                            if(!oldValue) {
                                s = s.replaceAll("disabled", "enabled");
                            }
                        } else if(s.contains("enabled")) {
                            if(oldValue) {
                                s = s.replaceAll("enabled", "disabled");
                            }
                        }
                        newLore.add(s);
                    }
                    itemMeta.setLore(newLore);
                    item2.setItemMeta(itemMeta);

                    e.getPlayer().setItemInHand(item2);
                    e.getPlayer().sendMessage(message);
                }
            }
        }
    }
}
