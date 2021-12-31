package com.captainbboy.harvesterhoes.SQLite;

import com.captainbboy.harvesterhoes.HarvesterHoes;

import java.util.logging.Level;

public class Error {
    public static void execute(HarvesterHoes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(HarvesterHoes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}