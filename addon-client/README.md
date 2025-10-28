# Simple Voice Chat - Standalone Addon (Client)

This is a client-side addon for Simple Voice Chat that enables connection to standalone voice servers.

## Purpose

This addon allows players to use a standalone voice server instead of the voice server integrated with the Minecraft server. This provides:

- **Decoupled Architecture**: Voice server can run independently from game server
- **Better Scalability**: Voice server can be on different hardware
- **Reduced Game Server Load**: Voice processing doesn't impact game performance
- **Flexible Deployment**: One voice server can serve multiple game servers

## Features

- Captures player position from the game client
- Sends position data along with voice packets
- Connects to configured standalone server
- Maintains compatibility with original voice chat mod

## Requirements

- Minecraft 1.21.10
- Fabric Loader 0.17.2+
- Simple Voice Chat mod installed
- Java 21

## Installation

1. Install Simple Voice Chat mod (main mod)
2. Install this addon mod
3. Configure the standalone server address

## Configuration

On first run, the addon creates a configuration file:

**Location**: `.minecraft/config/voicechat-standalone-addon.properties`

```properties
# Standalone server address
standalone.server.address=voice.example.com

# Standalone server port
standalone.server.port=24454

# Enable standalone mode (true/false)
standalone.enabled=true
```

### Configuration Options

- `standalone.server.address`: Hostname or IP address of standalone server
- `standalone.server.port`: UDP port of standalone server (default: 24454)
- `standalone.enabled`: Toggle standalone mode on/off

## How It Works

1. **Position Capture**: Addon tracks player's position in real-time
2. **Packet Extension**: Voice packets are extended to include position data
3. **Server Override**: Connection is redirected to standalone server
4. **Proximity Calculation**: Addon handles client-side audio attenuation

## Architecture

```
┌────────────────────────────────────┐
│        Minecraft Client            │
│                                    │
│  ┌──────────────────────────────┐ │
│  │ Simple Voice Chat (Main Mod) │ │
│  └──────────┬───────────────────┘ │
│             │                      │
│  ┌──────────▼───────────────────┐ │
│  │  Standalone Addon (This Mod) │ │
│  │  - Position Tracking         │ │
│  │  - Packet Extension          │ │
│  │  - Server Override           │ │
│  └──────────┬───────────────────┘ │
└─────────────┼────────────────────┘
              │ UDP
              ▼
    ┌─────────────────────┐
    │  Standalone Server   │
    │  (Separate Process)  │
    └─────────────────────┘
```

## Usage

### For Players

1. Install both the main mod and this addon
2. Configure the standalone server address
3. Join your Minecraft server normally
4. Voice chat will automatically use the standalone server

### For Server Administrators

1. Set up a standalone voice server (see standalone-server module)
2. Provide players with the voice server address
3. No game server configuration needed!

## Current Implementation Status

**⚠️ Note**: This is a basic implementation framework. The current version includes:

- ✅ Module structure and build configuration
- ✅ Fabric mod registration
- ✅ Plugin registration with voice chat API
- ⚠️ Event handlers (TODO)
- ⚠️ Position tracking (TODO)
- ⚠️ Packet extension (TODO)
- ⚠️ Configuration system (TODO)

### What Needs to be Implemented

To make this addon fully functional, the following components need implementation:

1. **Configuration System**
   - Load/save standalone server address
   - Toggle standalone mode
   - UI integration (optional)

2. **Position Tracking**
   - Hook into client player position updates
   - Track current world/dimension
   - Cache position for packet sending

3. **Packet Extension**
   - Intercept outgoing microphone packets
   - Add position data to packets
   - Use custom packet format (PositionalMicPacket)

4. **Server Override**
   - Use VoiceHostEvent to override server address
   - Point client to standalone server

5. **Packet Handling**
   - Receive PositionalSoundPackets from server
   - Perform client-side proximity filtering
   - Apply distance-based attenuation

## Development

### Building

```bash
./gradlew addon-client:build
```

Output JAR: `addon-client/build/libs/voicechat-<version>-standalone-addon-fabric.jar`

### Testing

1. Build the standalone server
2. Run the standalone server locally
3. Install the addon in a Minecraft development environment
4. Configure addon to point to localhost:24454
5. Join a multiplayer server and test voice chat

## API Integration

This addon uses the Simple Voice Chat Plugin API:

```java
@Override
public void registerEvents(EventRegistration registration) {
    // Override voice server address
    registration.registerEvent(VoiceHostEvent.class, event -> {
        String standaloneAddress = config.getStandaloneServerAddress();
        event.setVoiceHost(standaloneAddress);
    });

    // Capture player position for voice packets
    registration.registerEvent(MicrophonePacketEvent.class, event -> {
        Vec3 position = getPlayerPosition();
        String worldId = getWorldId();
        // Extend packet with position data
    });
}
```

## Limitations

- Client addon currently provides structure only
- Full implementation requires deeper integration with voice chat internals
- No encryption (plaintext audio)
- Relies on client-provided position data (potential for spoofing)

## Compatibility

- **Fabric**: ✅ Supported (this version)
- **Forge**: ❌ Requires separate implementation
- **NeoForge**: ❌ Requires separate implementation
- **Quilt**: ❌ Not tested

## Future Enhancements

- Configuration GUI using Cloth Config
- Multi-server support (automatic server discovery)
- Fallback to integrated server if standalone unavailable
- Position validation and anti-cheat
- Encryption support
- Group voice chat

## Troubleshooting

### Addon not loading

- Check Simple Voice Chat main mod is installed
- Verify Fabric Loader version is 0.17.2+
- Check logs for initialization errors

### Can't connect to standalone server

- Verify standalone server is running
- Check configured address and port
- Ensure firewall allows UDP traffic
- Test server connectivity: `nc -u <host> <port>`

### Voice not working

- Verify main Simple Voice Chat mod works normally
- Check addon is enabled in configuration
- Verify standalone server shows client connection
- Check client and server logs for errors

## Contributing

This addon is part of the Simple Voice Chat project. Contributions welcome!

## License

GPL-3.0 - Same as Simple Voice Chat main project

## Credits

- **Simple Voice Chat**: henkelmax
- **Standalone Server Implementation**: Claude AI (this implementation)
