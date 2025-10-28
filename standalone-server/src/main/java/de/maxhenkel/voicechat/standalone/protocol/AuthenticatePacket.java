package de.maxhenkel.voicechat.standalone.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Authentication packet sent by client to join the server.
 */
public class AuthenticatePacket {

    private final UUID clientId;
    private final String secret;

    public AuthenticatePacket(UUID clientId, String secret) {
        this.clientId = clientId;
        this.secret = secret;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }

    public byte[] encode() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(1 + 16 + 2 + secretBytes.length);

        buffer.put(ProtocolConstants.PACKET_TYPE_AUTHENTICATE);
        buffer.putLong(clientId.getMostSignificantBits());
        buffer.putLong(clientId.getLeastSignificantBits());
        buffer.putShort((short) secretBytes.length);
        buffer.put(secretBytes);

        return buffer.array();
    }

    public static AuthenticatePacket decode(ByteBuffer buffer) {
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        UUID clientId = new UUID(mostSigBits, leastSigBits);

        short secretLength = buffer.getShort();
        byte[] secretBytes = new byte[secretLength];
        buffer.get(secretBytes);
        String secret = new String(secretBytes, StandardCharsets.UTF_8);

        return new AuthenticatePacket(clientId, secret);
    }
}
