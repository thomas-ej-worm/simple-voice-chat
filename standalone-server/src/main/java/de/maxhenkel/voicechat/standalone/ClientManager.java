package de.maxhenkel.voicechat.standalone;

import de.maxhenkel.voicechat.standalone.config.ServerConfig;
import de.maxhenkel.voicechat.standalone.network.ClientSession;
import de.maxhenkel.voicechat.standalone.protocol.PositionalSoundPacket;

import java.net.SocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages connected clients and handles packet routing.
 */
public class ClientManager {

    private final ServerConfig config;
    private final Map<UUID, ClientSession> clients = new ConcurrentHashMap<>();
    private final Map<SocketAddress, UUID> addressToClientId = new ConcurrentHashMap<>();

    public ClientManager(ServerConfig config) {
        this.config = config;
    }

    public void registerClient(UUID clientId, SocketAddress address, String secret) {
        ClientSession session = new ClientSession(clientId, address, secret);
        clients.put(clientId, session);
        addressToClientId.put(address, clientId);
        System.out.println("Client registered: " + clientId + " from " + address);
    }

    public ClientSession getClient(UUID clientId) {
        return clients.get(clientId);
    }

    public ClientSession getClientByAddress(SocketAddress address) {
        UUID clientId = addressToClientId.get(address);
        return clientId != null ? clients.get(clientId) : null;
    }

    public void unregisterClient(UUID clientId) {
        ClientSession session = clients.remove(clientId);
        if (session != null) {
            addressToClientId.remove(session.getAddress());
            System.out.println("Client unregistered: " + clientId);
        }
    }

    public void updateClientPosition(UUID clientId, double x, double y, double z, String worldId) {
        ClientSession session = clients.get(clientId);
        if (session != null) {
            session.updatePosition(x, y, z, worldId);
        }
    }

    public void broadcastSound(PositionalSoundPacket packet) {
        ClientSession sender = clients.get(packet.getSenderId());
        if (sender == null) {
            return;
        }

        float maxDistance = config.getVoiceDistance();

        // Broadcast to all clients in the same world within range
        for (ClientSession client : clients.values()) {
            // Don't send back to sender
            if (client.getClientId().equals(packet.getSenderId())) {
                continue;
            }

            // Check if in same world
            if (!client.getWorldId().equals(sender.getWorldId())) {
                continue;
            }

            // Check distance
            double distance = calculateDistance(sender, client);
            if (distance <= maxDistance) {
                // TODO: Send packet to client
                // This will be implemented when we add the UDP packet sending
            }
        }
    }

    private double calculateDistance(ClientSession a, ClientSession b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public int getClientCount() {
        return clients.size();
    }

    public void checkTimeouts() {
        long now = System.currentTimeMillis();
        long timeout = config.getKeepAliveInterval() * 3; // 3x keepalive interval

        clients.values().removeIf(session -> {
            if (now - session.getLastKeepAlive() > timeout) {
                System.out.println("Client timed out: " + session.getClientId());
                addressToClientId.remove(session.getAddress());
                return true;
            }
            return false;
        });
    }

    public Iterable<ClientSession> getAllClients() {
        return clients.values();
    }
}
