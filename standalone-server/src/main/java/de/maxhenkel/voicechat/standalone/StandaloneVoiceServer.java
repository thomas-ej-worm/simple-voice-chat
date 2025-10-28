package de.maxhenkel.voicechat.standalone;

import de.maxhenkel.voicechat.standalone.config.ServerConfig;
import de.maxhenkel.voicechat.standalone.network.UdpServer;

import java.io.IOException;

/**
 * Standalone voice server that can run independently of a Minecraft server.
 * This server relays positional voice chat data between clients.
 */
public class StandaloneVoiceServer {

    private final ServerConfig config;
    private final UdpServer udpServer;
    private final ClientManager clientManager;

    public StandaloneVoiceServer(ServerConfig config) {
        this.config = config;
        this.clientManager = new ClientManager(config);
        this.udpServer = new UdpServer(config, clientManager);
    }

    public void start() throws IOException {
        System.out.println("Starting Simple Voice Chat Standalone Server v" + getVersion());
        System.out.println("Binding to " + config.getBindAddress() + ":" + config.getVoicePort());

        udpServer.start();

        System.out.println("Server started successfully!");
        System.out.println("Clients can connect to: " + config.getBindAddress() + ":" + config.getVoicePort());

        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        // Wait for shutdown
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        System.out.println("Shutting down server...");
        udpServer.stop();
        System.out.println("Server stopped.");
    }

    private String getVersion() {
        return "1.0.0-SNAPSHOT";
    }

    public static void main(String[] args) {
        System.out.println("=== Simple Voice Chat Standalone Server ===");

        // Load configuration
        ServerConfig config = ServerConfig.loadOrCreate();

        StandaloneVoiceServer server = new StandaloneVoiceServer(config);

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
