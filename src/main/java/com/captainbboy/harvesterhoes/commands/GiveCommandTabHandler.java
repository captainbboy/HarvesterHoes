package com.captainbboy.harvesterhoes.commands;

import com.captainbboy.harvesterhoes.HarvesterHoes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GiveCommandTabHandler implements TabCompleter {

    HarvesterHoes plugin;

    public GiveCommandTabHandler (HarvesterHoes plg) {
        plugin = plg;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        //create new array
        final List<String> completions = new ArrayList<>();
        List<String> options = new ArrayList<>();

        if(args.length == 1) {
            options.add("give");
        }

        if(args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                options.add(p.getName());
            }
        }

        if(args.length == 3) {
            options.add("0");
        }

        if(args.length == 4) {
            options.add("1");
        }

        if(args.length == 5) {
            options.add("true");
            options.add("false");
        }

        StringUtil.copyPartialMatches(args[0], options, completions);
        Collections.sort(completions);

        return completions;
    }

}