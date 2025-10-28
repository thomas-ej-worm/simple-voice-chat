package de.maxhenkel.voicechat.standalone.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration for the standalone voice server.
 */
public class ServerConfig {

    private static final String CONFIG_FILE = "server.properties";

    private final String bindAddress;
    private final int voicePort;
    private final int mtuSize;
    private final int keepAliveInterval;
    private final float voiceDistance;
    private final int maxClients;

    public ServerConfig(String bindAddress, int voicePort, int mtuSize, int keepAliveInterval, float voiceDistance, int maxClients) {
        this.bindAddress = bindAddress;
        this.voicePort = voicePort;
        this.mtuSize = mtuSize;
        this.keepAliveInterval = keepAliveInterval;
        this.voiceDistance = voiceDistance;
        this.maxClients = maxClients;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public int getVoicePort() {
        return voicePort;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public float getVoiceDistance() {
        return voiceDistance;
    }

    public int getMaxClients() {
        return maxClients;
    }

    public static ServerConfig loadOrCreate() {
        Path configPath = Paths.get(CONFIG_FILE);

        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                return load(input);
            } catch (IOException e) {
                System.err.println("Failed to load configuration: " + e.getMessage());
                System.out.println("Using default configuration");
                return createDefault();
            }
        } else {
            ServerConfig config = createDefault();
            config.save(configPath);
            return config;
        }
    }

    private static ServerConfig load(InputStream input) throws IOException {
        Properties props = new Properties();
        props.load(input);

        String bindAddress = props.getProperty("bind.address", "0.0.0.0");
        int voicePort = Integer.parseInt(props.getProperty("voice.port", "24454"));
        int mtuSize = Integer.parseInt(props.getProperty("mtu.size", "1024"));
        int keepAliveInterval = Integer.parseInt(props.getProperty("keep.alive.interval", "1000"));
        float voiceDistance = Float.parseFloat(props.getProperty("voice.distance", "48.0"));
        int maxClients = Integer.parseInt(props.getProperty("max.clients", "100"));

        return new ServerConfig(bindAddress, voicePort, mtuSize, keepAliveInterval, voiceDistance, maxClients);
    }

    private static ServerConfig createDefault() {
        return new ServerConfig("0.0.0.0", 24454, 1024, 1000, 48.0f, 100);
    }

    private void save(Path configPath) {
        Properties props = new Properties();
        props.setProperty("bind.address", bindAddress);
        props.setProperty("voice.port", String.valueOf(voicePort));
        props.setProperty("mtu.size", String.valueOf(mtuSize));
        props.setProperty("keep.alive.interval", String.valueOf(keepAliveInterval));
        props.setProperty("voice.distance", String.valueOf(voiceDistance));
        props.setProperty("max.clients", String.valueOf(maxClients));

        try (OutputStream output = Files.newOutputStream(configPath)) {
            props.store(output, "Standalone Voice Server Configuration");
            System.out.println("Created default configuration file: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }
}
