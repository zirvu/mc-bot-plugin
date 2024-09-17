package com.zirvumcai.project;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataHandler {

    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public DataHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        loadDataFile();
    }

    // Load the data.yml file
    private void loadDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // Get the stored Villager UUID from the file
    public UUID getStoredVillagerUUID() {
        String uuidString = dataConfig.getString("npc_uuid");
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        }
        return null;
    }

    // Save the Villager's UUID to the file
    public void saveVillagerUUID(UUID uuid) {
        dataConfig.set("npc_uuid", uuid.toString());
        saveDataFile();
    }

    // Clear the stored Villager UUID
    public void clearVillagerUUID() {
        dataConfig.set("npc_uuid", null);
        saveDataFile();
    }

    // Save changes to the data.yml file
    private void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
