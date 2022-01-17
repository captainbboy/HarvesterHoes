package com.captainbboy.harvesterhoes.events;

import com.captainbboy.harvesterhoes.GeneralUtil;
import com.captainbboy.harvesterhoes.HarvesterHoes;
import com.captainbboy.harvesterhoes.SQLite.SQLite;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class PlayerBlockEvent implements Listener {

    HarvesterHoes plugin;

    public PlayerBlockEvent(HarvesterHoes plg) {
        plugin = plg;
    }

    private ArrayList<Location> newBlocks = new ArrayList<>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!e.isCancelled()) {
            Block b = e.getBlock();
            if(b.getType() == Material.SUGAR_CANE_BLOCK) {
                newBlocks.add(b.getLocation());
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        newBlocks.remove(b.getLocation());
                    }
                }, 20L * 10L);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        if (!e.isCancelled()) {
            Player p = e.getPlayer();
            if (p.getGameMode() == GameMode.SURVIVAL) {

                if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR)
                    return;

                NBTItem nbti = new NBTItem(p.getItemInHand());

                if (nbti.hasKey("isHarvHoe") && nbti.getBoolean("isHarvHoe") && e.getBlock().getType().equals(Material.SUGAR_CANE_BLOCK) && this.plugin.wGuard().canBuild(p, p.getLocation())) {
                    if(nbti.hasKey("harvHoeHasteLevel")) {
                        Integer level = nbti.getInteger("harvHoeHasteLevel");
                        if(level > 0) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, (level - 1)));
                        }
                    }

                    Integer radius = 1;
                    if(nbti.hasKey("harvHoeRadiusLevel")) {
                        radius = nbti.getInteger("harvHoeRadiusLevel");
                    }

                    BlockFace blockFace = calculateBlockFace(p, e.getBlock());

                    e.setCancelled(true);
                    List<Block> blocks = getNearbySugarCane(e.getBlock().getLocation(), radius - 1, blockFace);

                    Integer amountOfCaneToGive = 0;

                    for(Block block : blocks) {
                        if(newBlocks.contains(block.getLocation())) {
                            p.sendMessage(GeneralUtil.messageWithColorCode(plugin.getConfig().getString("recently-placed-message")));
                            continue;
                        }
                        Location currLoc;
                        for(
                                currLoc = block.getLocation();
                                currLoc.getBlock().getType() == Material.SUGAR_CANE_BLOCK;
                                currLoc = new Location(currLoc.getWorld(), (double)currLoc.getBlockX(), (double)(currLoc.getBlockY() + 1), (double)currLoc.getBlockZ())
                        ){}

                        for(
                                currLoc = new Location(currLoc.getWorld(),(double)currLoc.getBlockX(), (double)(currLoc.getBlockY() - 1), (double)currLoc.getBlockZ());
                                currLoc.getBlockY() >= block.getY();
                                currLoc = new Location(currLoc.getWorld(), (double)currLoc.getBlockX(), (double)(currLoc.getBlockY() - 1), (double)currLoc.getBlockZ())
                        ) {
                            currLoc.getBlock().setType(Material.AIR);
                            amountOfCaneToGive += 1;
                        }
                    }

                    if(nbti.hasKey("harvHoeAutoSell") && nbti.getBoolean("harvHoeAutoSell") && nbti.hasKey("harvHoeAutoSellEnabled") && nbti.getBoolean("harvHoeAutoSellEnabled")) {
                        Integer priceOfSugarCane = this.plugin.getConfig().getInt("price-of-sugarcane");
                        Double multiplier = 1.0;
                        if(nbti.hasKey("harvHoeSellMultiplier")) {
                            multiplier = nbti.getDouble("harvHoeSellMultiplier");
                        }
                        if(priceOfSugarCane == 0) {
                            for (int i = 0; i < amountOfCaneToGive; i++) {
                                GeneralUtil.givePlayerItem(e.getPlayer(), Material.SUGAR_CANE);
                            }
                        } else {
                            this.plugin.eco.depositPlayer(p, amountOfCaneToGive * priceOfSugarCane * multiplier);
                            this.plugin.getPlayerHandler().addToEarned(p.getUniqueId(), amountOfCaneToGive * priceOfSugarCane * multiplier);
                        }
                    } else {
                        for (int i = 0; i < amountOfCaneToGive; i++) {
                            GeneralUtil.givePlayerItem(e.getPlayer(), Material.SUGAR_CANE);
                        }
                    }

                    Double currencyMultiplier = 1.0;

                    SQLite db = this.plugin.getSQLite();
                    GeneralUtil.updateBalance(db, p.getUniqueId(), amountOfCaneToGive * currencyMultiplier);
                }

            }
        }
    }

    public List<Block> getNearbySugarCane(Location location, int radius, BlockFace blockFace) {
        List<Block> blocks = new ArrayList<>();
        int y = location.getBlockY();
        if(blockFace == BlockFace.NORTH || blockFace == BlockFace.SOUTH) {
            int z = location.getBlockZ();
            for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
                Block block = location.getWorld().getBlockAt(x, y, z);
                if(block.getType() == Material.SUGAR_CANE_BLOCK) {
                    blocks.add(block);
                }
            }
        } else if(blockFace == BlockFace.EAST || blockFace == BlockFace.WEST) {
            int x = location.getBlockX();
            for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                Block block = location.getWorld().getBlockAt(x, y, z);
                if(block.getType() == Material.SUGAR_CANE_BLOCK) {
                    blocks.add(block);
                }
            }
        } else {
            for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    Block block = location.getWorld().getBlockAt(x, y, z);
                    if(block.getType() == Material.SUGAR_CANE_BLOCK) {
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    public BlockFace calculateBlockFace(Player p, Block targetBlock) {
        List<Block> lastTwoTargetBlocks = p.getLastTwoTargetBlocks((HashSet<Byte>) null, 32);
        if (lastTwoTargetBlocks.size() != 2) return null;
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

}
