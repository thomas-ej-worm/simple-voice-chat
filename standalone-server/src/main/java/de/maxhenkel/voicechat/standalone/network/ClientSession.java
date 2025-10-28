package de.maxhenkel.voicechat.standalone.network;

import java.net.SocketAddress;
import java.util.UUID;

/**
 * Represents a connected client session.
 */
public class ClientSession {

    private final UUID clientId;
    private final SocketAddress address;
    private final String secret;

    private double x, y, z;
    private String worldId;
    private long lastKeepAlive;

    public ClientSession(UUID clientId, SocketAddress address, String secret) {
        this.clientId = clientId;
        this.address = address;
        this.secret = secret;
        this.worldId = "overworld";
        this.lastKeepAlive = System.currentTimeMillis();
    }

    public void updatePosition(double x, double y, double z, String worldId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
    }

    public void updateKeepAlive() {
        this.lastKeepAlive = System.currentTimeMillis();
    }

    public UUID getClientId() {
        return clientId;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public String getSecret() {
        return secret;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorldId() {
        return worldId;
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }
}
