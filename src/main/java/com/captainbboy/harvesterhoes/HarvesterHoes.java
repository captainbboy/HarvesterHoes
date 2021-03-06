package com.captainbboy.harvesterhoes;

import com.captainbboy.harvesterhoes.SQLite.SQLite;
import com.captainbboy.harvesterhoes.commands.MainCommand;
import com.captainbboy.harvesterhoes.commands.MainCommandTabHandler;
import com.captainbboy.harvesterhoes.commands.UpgradeCommand;
import com.captainbboy.harvesterhoes.events.GUIEvents;
import com.captainbboy.harvesterhoes.events.PlayerBlockEvent;
import com.captainbboy.harvesterhoes.events.PlayerItemEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public final class HarvesterHoes extends JavaPlugin {

    private WorldGuardPlugin worldGuard;
    private PlayerHandler playerHandler = new PlayerHandler();
    private PlayerItemEvent interactEvent = new PlayerItemEvent(this);
    private SQLite sqLite;
    private UpgradeCommand upgradeCmd = new UpgradeCommand(this);
    public String currVersion = "1.4.1";
    public Economy eco;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Check Dependencies
        if(getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[HarvesterHoes]" + ChatColor.RED + " You must have WorldGuard installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if(getServer().getPluginManager().getPlugin("NBTAPI") == null) {
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[HarvesterHoes]" + ChatColor.RED + " You must have NBTAPI installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[HarvesterHoes]" + ChatColor.RED + " You must have Vault and an Economy Plugin installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Default config
        this.saveDefaultConfig();

        // Database
        sqLite = new SQLite(this);
        sqLite.load();

        // Events
        getServer().getPluginManager().registerEvents(new PlayerBlockEvent(this), this);
        getServer().getPluginManager().registerEvents(new GUIEvents(this), this);
        getServer().getPluginManager().registerEvents(interactEvent, this);

        // Commands
        getCommand("harvesterhoe").setExecutor(new MainCommand(this));
        getCommand("harvesterhoe").setTabCompleter(new MainCommandTabHandler(this));
        getCommand("upgrade").setExecutor(upgradeCmd);

        getServer().getConsoleSender().sendMessage(GeneralUtil.messageWithColorCode("&d&l(!) &bHarvesterHoe Plugin Has Been &aEnabled!"));

        startMinuteMessages();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getConsoleSender().sendMessage(GeneralUtil.messageWithColorCode("&d&l(!) &bHarvesterHoe Plugin Has Been &cDisabled!"));
    }

    public WorldGuardPlugin wGuard() {
        if(worldGuard == null) {
            Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");
            if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
                this.getPluginLoader().disablePlugin(this);
            } else {
                worldGuard = (WorldGuardPlugin) plugin;
            }
        }

        return worldGuard;
    }

    public void startMinuteMessages() {

        Map<UUID, Double> map = this.playerHandler.getAmountEarned();
        for(UUID uuid : map.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if(p != null) {
                String message = this.getConfig().getString("money-in-last-minute-message");
                message = message.replaceAll("\\{amount}", GeneralUtil.formatNumber(String.valueOf(map.get(uuid))));
                p.sendMessage(GeneralUtil.messageWithColorCode(message));
            }
        }
        this.playerHandler.clearAmountEarned();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                startMinuteMessages();
            }
        }, 60 * 20L);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if(economy != null)
            eco = economy.getProvider();

        return (eco != null);
    }

    public SQLite getSQLite() {
        return this.sqLite;
    }

    public PlayerHandler getPlayerHandler() {
        return this.playerHandler;
    }

    public PlayerItemEvent getInteractEvent() {
        return this.interactEvent;
    }

    public UpgradeCommand getUpgradeCmd() {
        return this.upgradeCmd;
    }
}
