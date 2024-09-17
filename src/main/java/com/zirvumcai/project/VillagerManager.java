package com.zirvumcai.project;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
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
        Bukkit.getScheduler().runTask(plugin, () -> {
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

    // Summon a new Villager NPC with a diamond sword and disable sleeping
    private void summonVillager(Player player) {
        // Remove any existing villager before summoning a new one
        // removeVillagerIfExists();

        Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        villager.setCustomName("Guardian");  // Give it a name
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2048);
        villager.setHealth(2048);

        // Equip the villager with a diamond sword and fire aspect
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2);  // Fire Aspect level 2
        villager.getEquipment().setItemInMainHand(sword);
        villager.getEquipment().setItemInMainHandDropChance(0); // Prevent dropping the sword

        // Disable villager's ability to sleep and prevent picking up items
        villager.setAI(true);
        villager.setCanPickupItems(false);

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
                    // Check the distance between the protector and the player
                    double distanceToPlayer = npc.getLocation().distance(player.getLocation());

                    // If the protector is too far from the player (e.g., 15 blocks away), teleport to the player
                    if (distanceToPlayer > 15.0) {
                        npc.teleport(player.getLocation());
                        return;
                    }

                    // Scan for nearby hostile mobs based on the player's location
                    List<Entity> nearbyEntities = player.getNearbyEntities(10, 5, 10);  // Scan 10 blocks around the player
                    boolean foundHostileMob = false;

                    for (Entity entity : nearbyEntities) {
                        if (isHostileMob(entity)) {  // Attack only hostile mobs
                            foundHostileMob = true;
                            attackHostileMob(npc, (Mob) entity);  // Attack hostile mob
                            break;
                        }
                    }

                    // If no hostile mobs are found, follow the player
                    if (!foundHostileMob) {
                        followPlayer(npc, player);  // Follow player if no mobs to attack
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    // Check if the entity is a hostile mob (Zombies, Skeletons, Creepers, etc.)
    private boolean isHostileMob(Entity entity) {
        return entity.getType() == EntityType.ZOMBIE || 
               entity.getType() == EntityType.SKELETON ||
               entity.getType() == EntityType.CREEPER ||
               entity.getType() == EntityType.SPIDER ||
               entity.getType() == EntityType.ENDERMAN ||  
               entity.getType() == EntityType.PILLAGER || 
               entity.getType() == EntityType.HUSK || 
               entity.getType() == EntityType.STRAY ||
               entity.getType() == EntityType.WITCH;
    }

    // Make the NPC attack a hostile mob
    private void attackHostileMob(Villager npc, Mob mob) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npc != null && !npc.isDead() && mob != null && !mob.isDead()) {
                    Location npcLocation = npc.getLocation();
                    Location mobLocation = mob.getLocation();

                    double distance = npcLocation.distance(mobLocation);

                    // Move the NPC toward the mob if it's more than 1 block away
                    if (distance > 1.5) {
                        Vector direction = mobLocation.toVector().subtract(npcLocation.toVector()).normalize();
                        npc.setVelocity(direction.multiply(0.3));  // Move toward the mob
                    } else {
                        mob.damage(5, npc);  // Damage the mob with a 5 damage value
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);  // Run every tick for smooth attacking
    }

    // Make the NPC follow the player
    private void followPlayer(Villager npc, Player player) {
        Location playerLocation = player.getLocation();
        Location npcLocation = npc.getLocation();

        double distance = npcLocation.distance(playerLocation);

        // Move the NPC toward the player if it's more than 3 blocks away
        if (distance > 3.0) {
            Vector direction = playerLocation.toVector().subtract(npcLocation.toVector()).normalize();
            npc.setVelocity(direction.multiply(0.2));  // Adjust movement speed
        } else {
            npc.setVelocity(new Vector(0, 0, 0));  // Stop the NPC when close to the player
        }
    }

    // Remove the Villager if it exists
    public void removeVillagerIfExists() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            UUID storedUUID = dataHandler.getStoredVillagerUUID();
            Villager villager = findVillagerByUUID(storedUUID);

            if (villager != null && !villager.isDead()) {
                plugin.getLogger().info("Removing existing NPC.");
                villager.remove();
                dataHandler.clearVillagerUUID();  // Clear the saved UUID after removal
            }
        });
    }

    // Teleport the Villager to the player
    public void teleportVillagerToPlayer(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            UUID storedUUID = dataHandler.getStoredVillagerUUID();
            Villager villager = findVillagerByUUID(storedUUID);

            if (villager != null && !villager.isDead()) {
                plugin.getLogger().info("Teleporting Villager to player.");
                villager.teleport(player.getLocation());
            } else {
                plugin.getLogger().warning("No Villager found to teleport.");
            }
        });
    }

}
