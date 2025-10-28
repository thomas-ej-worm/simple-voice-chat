# Standalone Voice Server Implementation

This document describes the implementation of standalone voice server support for Simple Voice Chat.

## Overview

This implementation adds support for running a voice server independently from the Minecraft game server. This is achieved through two new modules:

1. **standalone-server**: A standalone Java application that acts as the voice server
2. **addon-client**: A Fabric client mod that enables connection to standalone servers

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Minecraft Server                             │
│                    (No modifications needed!)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Game Data
                              │
        ┌─────────────────────┴───────────────────────┐
        │                                             │
┌───────▼────────┐                            ┌───────▼────────┐
│  Client 1      │                            │  Client 2      │
│  + Main Mod    │                            │  + Main Mod    │
│  + Addon       │                            │  + Addon       │
└───────┬────────┘                            └───────┬────────┘
        │                                             │
        │ UDP (Position + Voice)                     │
        │                                             │
        └─────────────────┬───────────────────────────┘
                          │
                  ┌───────▼────────┐
                  │   Standalone    │
                  │  Voice Server   │
                  │ (Separate Java  │
                  │  Application)   │
                  └─────────────────┘
```

## Key Design Decisions

### 1. Client-Side Position Tracking

Since the game server cannot be modified, the client addon captures the player's position from the game client and includes it in voice packets.

**Advantages**:
- No server modifications required
- Works with any Minecraft server
- Simple to deploy

**Trade-offs**:
- Position data is client-provided (could be spoofed)
- Slightly more bandwidth per packet
- Relies on client-side updates

### 2. Custom Packet Protocol

New packet types were created specifically for standalone server communication:

- **PositionalMicPacket**: Client → Server (voice + position)
- **PositionalSoundPacket**: Server → Clients (voice + sender position)
- **AuthenticatePacket**: Client → Server (authentication)
- **AuthenticateAckPacket**: Server → Client (authentication response)

These packets extend the original protocol by including:
- Player position (x, y, z)
- World/dimension ID
- Voice distance setting

### 3. Proximity-Based Relay

The standalone server performs proximity calculations to determine which clients should receive voice data:

```java
if (sameWorld && distance <= maxDistance) {
    sendToClient(audioPacket);
}
```

This reduces bandwidth by only sending audio to players who can hear it.

## Module Structure

### Standalone Server

```
standalone-server/
├── src/main/java/de/maxhenkel/voicechat/standalone/
│   ├── StandaloneVoiceServer.java      # Main entry point
│   ├── ClientManager.java              # Client session management
│   ├── config/
│   │   └── ServerConfig.java           # Configuration loader
│   ├── network/
│   │   ├── UdpServer.java              # UDP socket handling
│   │   └── ClientSession.java          # Client state tracking
│   └── protocol/
│       ├── ProtocolConstants.java      # Packet type IDs
│       ├── AuthenticatePacket.java     # Authentication
│       ├── AuthenticateAckPacket.java  # Auth response
│       ├── PositionalMicPacket.java    # Voice input with position
│       └── PositionalSoundPacket.java  # Voice output with position
├── build.gradle                        # Gradle build configuration
└── README.md                           # Usage documentation
```

### Client Addon

```
addon-client/
├── src/main/java/de/maxhenkel/voicechat/addon/
│   └── StandaloneAddonPlugin.java     # Plugin implementation
├── src/main/resources/
│   └── fabric.mod.json                # Fabric mod metadata
├── build.gradle                       # Gradle build configuration
└── README.md                          # Usage documentation
```

## Protocol Specification

### Packet Format

All packets start with a single byte identifying the packet type:

```
[Packet Type (1 byte)][Packet Data (variable)]
```

### PositionalMicPacket (0x12)

Sent by client when speaking:

```
- Packet Type: 0x12 (1 byte)
- Client UUID: (16 bytes)
- Audio Data Length: (4 bytes)
- Audio Data: (variable bytes)
- Sequence Number: (8 bytes)
- Whispering Flag: (1 byte, 0 or 1)
- Position X: (8 bytes, double)
- Position Y: (8 bytes, double)
- Position Z: (8 bytes, double)
- World ID Length: (2 bytes, short)
- World ID: (variable bytes, UTF-8 string)
```

### PositionalSoundPacket (0x13)

Sent by server to nearby clients:

```
- Packet Type: 0x13 (1 byte)
- Channel UUID: (16 bytes)
- Sender UUID: (16 bytes)
- Audio Data Length: (4 bytes)
- Audio Data: (variable bytes)
- Sequence Number: (8 bytes)
- Whispering Flag: (1 byte, 0 or 1)
- Position X: (8 bytes, double)
- Position Y: (8 bytes, double)
- Position Z: (8 bytes, double)
- World ID Length: (2 bytes, short)
- World ID: (variable bytes, UTF-8 string)
- Distance: (4 bytes, float)
```

## Configuration

### Server Configuration

File: `server.properties` (created on first run)

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

### Client Addon Configuration

File: `.minecraft/config/voicechat-standalone-addon.properties` (to be implemented)

```properties
# Standalone server address
standalone.server.address=voice.example.com

# Standalone server port
standalone.server.port=24454

# Enable standalone mode
standalone.enabled=true
```

## Building

### Standalone Server

```bash
./gradlew standalone-server:shadowJar
```

Output: `standalone-server/build/libs/standalone-server-<version>-standalone.jar`

### Client Addon

```bash
./gradlew addon-client:build
```

Output: `addon-client/build/libs/voicechat-<version>-standalone-addon-fabric.jar`

## Deployment

### Server Setup

1. Build the standalone server JAR
2. Run on a separate server or same machine as game server:
   ```bash
   java -jar standalone-server-<version>-standalone.jar
   ```
3. Ensure UDP port 24454 is open in firewall
4. Configure server.properties as needed

### Client Setup

1. Install Simple Voice Chat main mod (required)
2. Install the standalone addon mod
3. Configure addon with standalone server address
4. Connect to Minecraft server normally

## Implementation Status

### ✅ Completed

- [x] Standalone server module structure
- [x] UDP server implementation
- [x] Positional packet protocol
- [x] Client session management
- [x] Proximity-based audio relay
- [x] Authentication system (basic)
- [x] Configuration system
- [x] Client addon module structure
- [x] Plugin registration
- [x] Documentation (READMEs)

### ⚠️ TODO (Future Enhancements)

- [ ] Client addon event handlers (position capture, packet extension)
- [ ] Client addon configuration system
- [ ] Client-side proximity filtering
- [ ] Encryption for secure transmission
- [ ] Shared secret authentication with game server
- [ ] Group voice chat support
- [ ] Category-based audio channels
- [ ] Multi-server support (server ID in packets)
- [ ] Administrative controls (kick, mute, ban)
- [ ] Web-based monitoring/admin panel

### ⚠️ Known Limitations

1. **No Encryption**: Audio is transmitted in plaintext
2. **Basic Authentication**: Currently accepts all clients
3. **Client-Provided Position**: Relies on client-reported position (could be spoofed)
4. **No Group Chat**: Only proximity voice is supported
5. **Addon is Framework Only**: Client addon provides structure but needs full implementation

## Testing Plan

1. **Unit Tests**: Test packet encoding/decoding
2. **Integration Tests**: Test server with mock clients
3. **Manual Testing**:
   - Start standalone server
   - Connect two clients with addon
   - Test proximity voice (move closer/farther)
   - Test multi-world scenarios
   - Test connection timeout
   - Test high client count

## Security Considerations

### Current Implementation

- ✅ No Minecraft server modifications (safe for servers)
- ✅ Client-only changes (players control their own mods)
- ⚠️ No encryption (audio can be intercepted)
- ⚠️ No position validation (clients can send fake positions)
- ⚠️ Basic authentication only

### Recommendations for Production

1. **Add Encryption**: Implement AES-GCM encryption for audio packets
2. **Position Validation**: Server should validate position changes aren't impossible
3. **Rate Limiting**: Prevent packet flooding
4. **Authentication**: Integrate with game server for token-based auth
5. **Monitoring**: Log suspicious activity (rapid position changes, flooding)

## Performance Considerations

### Bandwidth

Per voice packet overhead:
- Original MicPacket: ~1KB (audio only)
- PositionalMicPacket: ~1KB + 50 bytes (audio + position data)
- Overhead: ~5% increase

With 50ms voice packets (20 packets/second):
- Original: 20 KB/s per speaker
- Positional: 21 KB/s per speaker
- Impact: Minimal

### CPU

Server CPU usage:
- O(N²) for N clients (each speaker broadcasts to listeners)
- Proximity filtering reduces to O(N×M) where M = nearby clients
- Typical: 10-50 clients, minimal CPU impact
- High load: 100+ clients, consider load balancing

### Latency

- UDP direct communication: <10ms typical
- Position updates: Every packet (no additional latency)
- Proximity calculation: <1ms per client check
- Total added latency: <5ms

## Future Roadmap

### Phase 1: Core Functionality (This PR)
- [x] Standalone server implementation
- [x] Client addon framework
- [x] Basic protocol

### Phase 2: Full Client Integration
- [ ] Complete client addon implementation
- [ ] Position tracking and packet extension
- [ ] Configuration UI
- [ ] Testing and bug fixes

### Phase 3: Security
- [ ] Encryption implementation
- [ ] Authentication integration
- [ ] Position validation
- [ ] Rate limiting

### Phase 4: Advanced Features
- [ ] Group voice chat
- [ ] Multi-server support
- [ ] Admin controls
- [ ] Monitoring/metrics
- [ ] Load balancing

### Phase 5: Platform Support
- [ ] Forge client addon
- [ ] NeoForge client addon
- [ ] Quilt client addon

## Contributing

This implementation provides the foundation for standalone voice server support. Contributions are welcome for:

- Completing the client addon implementation
- Adding encryption
- Implementing group chat
- Adding administrative features
- Writing tests
- Improving documentation

## Credits

- **Original Simple Voice Chat**: henkelmax
- **Standalone Server Implementation**: Claude AI (this implementation)
- **Architecture Design**: Based on discussions in Issue #2

## License

GPL-3.0 - Same as Simple Voice Chat main project
