
# ZirvuBotPlugin

ZirvuBotPlugin is a Minecraft plugin that adds a custom guardian NPC (a baby zombie) to follow and protect players. The guardian can attack hostile mobs like zombies, skeletons, creepers, and more. Additionally, the guardian can be given commands to follow, stay in place, or chat using predefined messages loaded from an XML file.

## Features

- Summon a baby zombie guardian with full diamond armor and a diamond sword.
- The guardian follows and protects the player from hostile mobs.
- Custom chat messages loaded from an XML file.
- Hold and release commands to control the guardian's movement.
- The guardian has fire resistance and will not burn in sunlight.

## Installation

1. **Download** the `ZirvuBotPlugin.jar` file.
2. Place the `ZirvuBotPlugin.jar` file in the `plugins` folder of your Minecraft server.
3. Restart or reload your server using the following command:
   ```
   /reload
   ```
4. The plugin should now be active.

## Configuration

Upon the first load, the plugin will automatically generate the required directories and an `helping.xml` file containing default chat messages for the guardian.

- The file structure looks like this:
  ```
  plugins/ZirvuBotPlugin/chat/guardian/helping.xml
  ```

The `helping.xml` contains predefined chat messages that the guardian can randomly use when summoned. You can edit or add new messages in the XML file.

## Commands

- **`help`**: Summons the guardian if it's not already summoned, or teleports it to the player if it is already summoned. The guardian will follow and protect the player.
  
- **`hold`**: Stops the guardian from moving or following the player. It will remain in its current position.

- **`release`**: Allows the guardian to move again and resume following or attacking hostile mobs.

- **`rest`**: Removes the guardian if it is currently summoned.

## Example Commands

1. **Summon Guardian**:
   ```
   help
   ```
   The guardian will appear and follow you.

2. **Hold Guardian in Place**:
   ```
   hold
   ```
   The guardian will stop moving and stay in place.

3. **Release Guardian**:
   ```
   release
   ```
   The guardian will resume following or attacking nearby mobs.

4. **Dismiss Guardian**:
   ```
   rest
   ```
   The guardian will be removed from the world.

## Custom Chat Messages

The guardian will use random chat messages when summoned. The messages are stored in the `helping.xml` file, which can be found in:

```
plugins/ZirvuBotPlugin/chat/guardian/helping.xml
```

To add custom messages, open the `helping.xml` file and add more `<message>` tags like this:

```xml
<messages>
  <message>Help? Oh, sure, as long as you don't need emotional support!</message>
  <message>I charge by the minute, you know that, right?</message>
  <message>Sure, I'll help. But what's in it for me?</message>
</messages>
```

## Troubleshooting

1. **Guardian is attacking the player**: 
   Ensure that the plugin is correctly cancelling any hostile actions toward the player using the `EntityTargetEvent`.

2. **Guardian not following or attacking mobs**: 
   Make sure that the plugin is checking for nearby hostile mobs and that the guardian's AI is properly handling pathfinding.

## License

This plugin is provided as-is and is intended for use in Minecraft servers.
