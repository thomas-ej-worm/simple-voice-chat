package de.maxhenkel.voicechat.standalone.network;

import de.maxhenkel.voicechat.standalone.ClientManager;
import de.maxhenkel.voicechat.standalone.config.ServerConfig;
import de.maxhenkel.voicechat.standalone.protocol.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * UDP server for voice chat communication.
 */
public class UdpServer {

    private final ServerConfig config;
    private final ClientManager clientManager;
    private DatagramSocket socket;
    private ExecutorService executor;
    private volatile boolean running;

    public UdpServer(ServerConfig config, ClientManager clientManager) {
        this.config = config;
        this.clientManager = clientManager;
    }

    public void start() throws IOException {
        InetSocketAddress bindAddress = new InetSocketAddress(config.getBindAddress(), config.getVoicePort());
        socket = new DatagramSocket(bindAddress);
        socket.setReceiveBufferSize(1024 * 1024); // 1MB buffer
        socket.setSendBufferSize(1024 * 1024);

        executor = Executors.newCachedThreadPool();
        running = true;

        // Start receiver thread
        executor.submit(this::receiveLoop);

        // Start timeout checker
        executor.submit(this::timeoutChecker);
    }

    private void receiveLoop() {
        byte[] buffer = new byte[config.getMtuSize()];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Process packet in separate thread to avoid blocking receiver
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
                SocketAddress sender = packet.getSocketAddress();

                executor.submit(() -> handlePacket(data, sender));

            } catch (IOException e) {
                if (running) {
                    System.err.println("Error receiving packet: " + e.getMessage());
                }
            }
        }
    }

    private void handlePacket(byte[] data, SocketAddress sender) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);

            // Read packet type (first byte)
            byte packetType = buffer.get();

            switch (packetType) {
                case ProtocolConstants.PACKET_TYPE_AUTHENTICATE:
                    handleAuthenticatePacket(buffer, sender);
                    break;
                case ProtocolConstants.PACKET_TYPE_POSITIONAL_MIC:
                    handlePositionalMicPacket(buffer, sender);
                    break;
                case ProtocolConstants.PACKET_TYPE_KEEP_ALIVE:
                    handleKeepAlivePacket(buffer, sender);
                    break;
                default:
                    System.err.println("Unknown packet type: " + packetType);
            }
        } catch (Exception e) {
            System.err.println("Error handling packet from " + sender + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAuthenticatePacket(ByteBuffer buffer, SocketAddress sender) {
        AuthenticatePacket packet = AuthenticatePacket.decode(buffer);
        System.out.println("Authentication request from " + packet.getClientId());

        // For now, accept all clients (in production, verify secret)
        clientManager.registerClient(packet.getClientId(), sender, packet.getSecret());

        // Send acknowledgment
        AuthenticateAckPacket ack = new AuthenticateAckPacket(true);
        sendPacket(ack.encode(), sender);
    }

    private void handlePositionalMicPacket(ByteBuffer buffer, SocketAddress sender) {
        PositionalMicPacket micPacket = PositionalMicPacket.decode(buffer);

        // Update client position
        clientManager.updateClientPosition(
                micPacket.getClientId(),
                micPacket.getX(),
                micPacket.getY(),
                micPacket.getZ(),
                micPacket.getWorldId()
        );

        // Create sound packet and broadcast
        PositionalSoundPacket soundPacket = new PositionalSoundPacket(
                UUID.randomUUID(), // channelId
                micPacket.getClientId(),
                micPacket.getData(),
                micPacket.getSequenceNumber(),
                micPacket.isWhispering(),
                micPacket.getX(),
                micPacket.getY(),
                micPacket.getZ(),
                micPacket.getWorldId(),
                config.getVoiceDistance()
        );

        // Broadcast to nearby clients
        broadcastToNearbyClients(soundPacket);
    }

    private void handleKeepAlivePacket(ByteBuffer buffer, SocketAddress sender) {
        ClientSession session = clientManager.getClientByAddress(sender);
        if (session != null) {
            session.updateKeepAlive();
        }
    }

    private void broadcastToNearbyClients(PositionalSoundPacket packet) {
        ClientSession sender = clientManager.getClient(packet.getSenderId());
        if (sender == null) {
            return;
        }

        float maxDistance = config.getVoiceDistance();
        byte[] encodedPacket = packet.encode();

        for (ClientSession client : clientManager.getAllClients()) {
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
                sendPacket(encodedPacket, client.getAddress());
            }
        }
    }

    private double calculateDistance(ClientSession a, ClientSession b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void sendPacket(byte[] data, SocketAddress destination) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, destination);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error sending packet to " + destination + ": " + e.getMessage());
        }
    }

    private void timeoutChecker() {
        while (running) {
            try {
                Thread.sleep(config.getKeepAliveInterval());
                clientManager.checkTimeouts();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}
