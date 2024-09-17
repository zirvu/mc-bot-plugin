package com.zirvumcai.project;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class ZirvuBotPlugin extends JavaPlugin implements Listener {

    private Villager followerNPC;  // Reference to the villager that follows the player
    private UUID followerUUID;     // Unique identifier for the villager

    @Override
    public void onEnable() {
        getLogger().info("ZirvuBotPlugin has been enabled!");
        // Register the event listener
        getServer().getPluginManager().registerEvents(this, this);  // Ensure this line exists
    }

    @Override
    public void onDisable() {
        getLogger().info("ZirvuBotPlugin has been disabled!");
    }

    // Event handler for player chat ("help" message triggers the NPC summon)
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        getLogger().info(player.getName() + " sent a chat message: " + message);  // Debugging message

        if (player.getName().equals("zirvu1351") && message.equalsIgnoreCase("help")) {
            getLogger().info("Player zirvu1351 sent 'help' in chat.");  // Debugging message

            // Schedule the entity spawning on the main server thread
            Bukkit.getScheduler().runTask(this, () -> {
                // Check if the villager is dead or null, summon a new one if needed
                Villager existingNPC = findExistingFollower();
                if (existingNPC == null || !existingNPC.isValid() || existingNPC.isDead()) {
                    // No valid follower found or villager is dead, summon a new one
                    getLogger().info("No existing NPC found or NPC is dead. Summoning a new NPC.");
                    summonVillager(player);
                } else {
                    // Teleport the existing villager to the player
                    getLogger().info("NPC already exists and is alive. Teleporting to player.");
                    existingNPC.teleport(player.getLocation());
                }
            });
        }
    }

    // Method to find the existing NPC in the world using the stored UUID
    private Villager findExistingFollower() {
        if (followerUUID != null) {
            for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                if (entity instanceof Villager && entity.getUniqueId().equals(followerUUID)) {
                    return (Villager) entity;  // Return the existing follower NPC
                }
            }
        }
        return null;  // No NPC found
    }

    // Method to summon a new Villager and give it a sword
    public void summonVillager(Player player) {
        // Spawn a new Villager (as a placeholder for a human NPC)
        followerNPC = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        followerUUID = followerNPC.getUniqueId();  // Store the NPC's unique ID

        getLogger().info("New NPC summoned for player " + player.getName());  // Debugging message

        // Set the NPC invulnerable to prevent it from dying
        followerNPC.setInvulnerable(true);
        followerNPC.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2048);  // High health value
        followerNPC.setHealth(2048);

        // Give the villager a sword to attack mobs
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        followerNPC.getEquipment().setItemInMainHand(sword);

        // Start the task to either attack nearby mobs or follow the player
        startTaskToFollowOrAttack(followerNPC, player);
    }

    // Method to check for nearby mobs and either attack them or follow the player
    public void startTaskToFollowOrAttack(Villager npc, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player != null && npc != null && !npc.isDead() && player.isOnline()) {
                    // First, check for nearby hostile mobs
                    List<Entity> nearbyEntities = npc.getNearbyEntities(10, 5, 10);  // Scan 10 blocks around the NPC
                    boolean foundHostileMob = false;

                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Mob && !(entity instanceof IronGolem)) {  // Check if the entity is a hostile mob
                            Mob mob = (Mob) entity;
                            if (mob.getTarget() == null) {
                                // Move NPC toward the hostile mob
                                foundHostileMob = true;
                                attackHostileMob(npc, mob);
                                break;
                            }
                        }
                    }

                    // If no hostile mobs are found, follow the player
                    if (!foundHostileMob) {
                        Location playerLocation = player.getLocation();
                        Location npcLocation = npc.getLocation();

                        // Calculate the distance between the NPC and the player
                        double distance = npcLocation.distance(playerLocation);

                        // Move the NPC toward the player if it's more than 3 blocks away
                        if (distance > 3.0) {
                            // Calculate the direction vector towards the player
                            Vector direction = playerLocation.toVector().subtract(npcLocation.toVector()).normalize();

                            // Multiply the vector by a small value to move the NPC smoothly toward the player
                            npc.setVelocity(direction.multiply(0.2));  // Adjust this value to control movement speed
                        } else {
                            // Stop the NPC when close to the player
                            npc.setVelocity(new Vector(0, 0, 0));
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L); // Run every tick (20 times per second) for smooth movement and decision-making
    }

    // Method to make the NPC attack the hostile mob
    public void attackHostileMob(Villager npc, Mob mob) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npc != null && !npc.isDead() && mob != null && !mob.isDead()) {
                    Location npcLocation = npc.getLocation();
                    Location mobLocation = mob.getLocation();

                    // Calculate the distance between the NPC and the mob
                    double distance = npcLocation.distance(mobLocation);

                    // Move the NPC toward the mob if it's more than 1 block away
                    if (distance > 1.5) {
                        Vector direction = mobLocation.toVector().subtract(npcLocation.toVector()).normalize();
                        npc.setVelocity(direction.multiply(0.3));  // Make the NPC move towards the mob
                    } else {
                        // If close enough, simulate an attack
                        mob.damage(5, npc);  // Damage the mob with a 5 damage value
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);  // Run every tick for smooth attacking
    }

    // Prevent the Iron Golem from attacking the Villager NPC
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Villager && event.getEntity() instanceof IronGolem) {
            event.setCancelled(true);  // Cancel targeting if it's an Iron Golem and a Villager
        }
    }

    // Event to handle the death of the villager NPC
    @EventHandler
    public void onVillagerDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Villager && event.getEntity().getUniqueId().equals(followerUUID)) {
            getLogger().info("The NPC villager has died.");  // Log when the NPC dies
            followerNPC = null;  // Reset the follower reference
            followerUUID = null;  // Reset the NPC UUID
        }
    }
}
