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

    // Save the Zombie UUID to the configuration file
    public void saveZombieUUID(UUID uuid) {
        config.set("zombie-uuid", uuid.toString());
        plugin.saveConfig();
    }

    // Get the stored Zombie UUID
    public UUID getStoredZombieUUID() {
        String uuidString = config.getString("zombie-uuid");
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        }
        return null;
    }

    // Clear the Zombie UUID from the configuration
    public void clearZombieUUID() {
        config.set("zombie-uuid", null);
        plugin.saveConfig();
    }
}
