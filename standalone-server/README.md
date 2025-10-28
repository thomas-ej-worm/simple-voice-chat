# Simple Voice Chat - Standalone Server

This is a standalone voice server that can run independently of a Minecraft server. It allows you to decouple the voice chat functionality from the game server.

## Features

- **Standalone Operation**: Runs as a separate Java application, no Minecraft server required
- **Positional Audio**: Supports 3D positional voice chat based on player locations
- **Multi-World Support**: Handles players in different worlds/dimensions
- **Proximity-Based Relay**: Only sends audio to players within hearing distance
- **Configurable**: Customizable port, distance, and performance settings
- **Low Latency**: Direct UDP communication for minimal voice lag

## Requirements

- Java 21 or higher
- Open UDP port (default: 24454)

## Installation

1. Build the project:
   ```bash
   ./gradlew standalone-server:shadowJar
   ```

2. The standalone server JAR will be in:
   ```
   standalone-server/build/libs/standalone-server-<version>-standalone.jar
   ```

## Usage

1. Run the server:
   ```bash
   java -jar standalone-server-<version>-standalone.jar
   ```

2. On first run, a `server.properties` configuration file will be created.

3. Configure the server by editing `server.properties`:
   ```properties
   # Bind address (0.0.0.0 = all interfaces)
   bind.address=0.0.0.0

   # UDP port for voice chat
   voice.port=24454

   # Maximum transmission unit size
   mtu.size=1024

   # Keep-alive interval in milliseconds
   keep.alive.interval=1000

   # Voice hearing distance in blocks
   voice.distance=48.0

   # Maximum concurrent clients
   max.clients=100
   ```

4. Restart the server to apply configuration changes.

## Protocol

The standalone server uses a custom UDP protocol for positional voice chat:

### Packet Types

- **AUTHENTICATE (0x10)**: Client authentication
- **AUTHENTICATE_ACK (0x11)**: Server authentication response
- **POSITIONAL_MIC (0x12)**: Client voice data with position
- **POSITIONAL_SOUND (0x13)**: Server relayed voice with position
- **KEEP_ALIVE (0x14)**: Connection keep-alive

### Positional Mic Packet Format

Clients send voice data along with their current position:

- Client UUID (16 bytes)
- Audio data length (4 bytes)
- Audio data (variable)
- Sequence number (8 bytes)
- Whispering flag (1 byte)
- Position X, Y, Z (24 bytes)
- World ID length (2 bytes)
- World ID string (variable)

### Positional Sound Packet Format

Server broadcasts voice to nearby players with sender position:

- Channel UUID (16 bytes)
- Sender UUID (16 bytes)
- Audio data length (4 bytes)
- Audio data (variable)
- Sequence number (8 bytes)
- Whispering flag (1 byte)
- Position X, Y, Z (24 bytes)
- World ID length (2 bytes)
- World ID string (variable)
- Distance (4 bytes)

## Client Configuration

Clients must use the **Simple Voice Chat Standalone Addon** mod to connect to this server. The addon:

1. Overrides the voice server address to point to the standalone server
2. Captures player position from the game
3. Sends position data with voice packets
4. Performs client-side proximity calculations

## Architecture

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────┐
│   Client 1  │         │  Standalone      │         │   Client 2  │
│  (w/ Addon) │◄───────►│  Voice Server    │◄───────►│  (w/ Addon) │
└─────────────┘   UDP   └──────────────────┘   UDP   └─────────────┘
      │                          │                          │
      └─────────┐      ┌─────────┴─────────┐      ┌─────────┘
                │      │                   │      │
             Position  Position         Position
             Tracking  Relay            Tracking
```

## Performance Considerations

- **Voice Distance**: Lower values reduce network traffic
- **MTU Size**: Larger values reduce packet count but may cause fragmentation
- **Keep-Alive Interval**: Affects timeout detection responsiveness
- **Max Clients**: Limit concurrent connections for resource management

## Limitations

- No encryption (plaintext audio transmission)
- No group chat support (only proximity voice)
- No authentication (accepts all clients)
- No player validation (relies on client-provided data)

## Future Enhancements

- Shared secret authentication with game server
- Group voice chat support
- Category-based audio channels
- Encryption for secure transmission
- Multi-server support (one voice server, multiple game servers)
- Administrative controls (kick, mute, ban)

## Troubleshooting

### Server won't start

- Check if port 24454 is already in use
- Verify Java 21+ is installed: `java -version`
- Check firewall rules for UDP port 24454

### Clients can't connect

- Verify server is running and accessible
- Check firewall allows UDP traffic on configured port
- Ensure clients have the addon mod installed
- Verify addon is configured with correct server address

### Voice quality issues

- Increase MTU size in configuration
- Reduce voice distance if bandwidth-limited
- Check network latency between clients and server

## License

GPL-3.0 - Same as Simple Voice Chat main project
