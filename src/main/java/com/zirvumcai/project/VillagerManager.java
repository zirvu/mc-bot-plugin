package com.zirvumcai.project;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class VillagerManager {

    private final JavaPlugin plugin;
    private final DataHandler dataHandler;

    public VillagerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataHandler = new DataHandler(plugin);
    }

    // Handle the "help" command and summon or teleport the NPC
    public void handleHelpCommand(Player player) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTask(plugin, () -> {
            UUID storedUUID = dataHandler.getStoredVillagerUUID();
            Villager existingVillager = findVillagerByUUID(storedUUID);

            if (existingVillager == null || !existingVillager.isValid() || existingVillager.isDead()) {
                plugin.getLogger().info("No valid NPC found. Summoning a new one.");
                summonVillager(player);  // Summon the villager on the main thread
            } else {
                plugin.getLogger().info("NPC found. Teleporting to player.");
                existingVillager.teleport(player.getLocation());  // Teleport on the main thread
            }
        });
    }

    // Summon a new Villager NPC
    private void summonVillager(Player player) {
        Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        villager.setInvulnerable(true);
        villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2048);
        villager.setHealth(2048);

        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        villager.getEquipment().setItemInMainHand(sword);

        // Save the NPC's UUID to the data file
        dataHandler.saveVillagerUUID(villager.getUniqueId());

        // Start the task to either attack mobs or follow the player
        startTaskToFollowOrAttack(villager, player);
    }

    // Find an existing Villager by UUID
    private Villager findVillagerByUUID(UUID uuid) {
        if (uuid == null) return null;
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            if (entity instanceof Villager && entity.getUniqueId().equals(uuid)) {
                return (Villager) entity;
            }
        }
        return null;
    }

    // Start the task to either attack nearby mobs or follow the player
    private void startTaskToFollowOrAttack(Villager npc, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player != null && npc != null && !npc.isDead() && player.isOnline()) {
                    // Follow or attack mobs logic (simplified for example)
                    Location playerLocation = player.getLocation();
                    Location npcLocation = npc.getLocation();
                    double distance = npcLocation.distance(playerLocation);

                    // Follow player logic
                    if (distance > 3.0) {
                        Vector direction = playerLocation.toVector().subtract(npcLocation.toVector()).normalize();
                        npc.setVelocity(direction.multiply(0.2));
                    } else {
                        npc.setVelocity(new Vector(0, 0, 0));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    // Remove the Villager if it exists
    public void removeVillagerIfExists() {
        UUID storedUUID = dataHandler.getStoredVillagerUUID();
        Villager villager = findVillagerByUUID(storedUUID);

        if (villager != null && !villager.isDead()) {
            plugin.getLogger().info("Removing existing NPC.");
            villager.remove();
            dataHandler.clearVillagerUUID();  // Clear the saved UUID after removal
        }
    }
}
