package com.zirvumcai.project;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class DataHandler {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public DataHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    // Save the Villager UUID to the configuration file
    public void saveVillagerUUID(UUID uuid) {
        config.set("villager-uuid", uuid.toString());
        plugin.saveConfig();
    }

    // Get the stored Villager UUID
    public UUID getStoredVillagerUUID() {
        String uuidString = config.getString("villager-uuid");
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        }
        return null;
    }

    // Clear the Villager UUID from the configuration
    public void clearVillagerUUID() {
        config.set("villager-uuid", null);
        plugin.saveConfig();
    }
}
