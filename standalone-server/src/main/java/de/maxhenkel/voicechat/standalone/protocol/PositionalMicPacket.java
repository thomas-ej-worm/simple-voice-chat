package de.maxhenkel.voicechat.standalone.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Microphone packet with position data sent by client.
 */
public class PositionalMicPacket {

    private final UUID clientId;
    private final byte[] data;
    private final long sequenceNumber;
    private final boolean whispering;
    private final double x, y, z;
    private final String worldId;

    public PositionalMicPacket(UUID clientId, byte[] data, long sequenceNumber, boolean whispering,
                               double x, double y, double z, String worldId) {
        this.clientId = clientId;
        this.data = data;
        this.sequenceNumber = sequenceNumber;
        this.whispering = whispering;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public byte[] getData() {
        return data;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean isWhispering() {
        return whispering;
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

    public byte[] encode() {
        byte[] worldIdBytes = worldId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(1 + 16 + 4 + data.length + 8 + 1 + 24 + 2 + worldIdBytes.length);

        buffer.put(ProtocolConstants.PACKET_TYPE_POSITIONAL_MIC);
        buffer.putLong(clientId.getMostSignificantBits());
        buffer.putLong(clientId.getLeastSignificantBits());
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.putLong(sequenceNumber);
        buffer.put((byte) (whispering ? 1 : 0));
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putShort((short) worldIdBytes.length);
        buffer.put(worldIdBytes);

        return buffer.array();
    }

    public static PositionalMicPacket decode(ByteBuffer buffer) {
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        UUID clientId = new UUID(mostSigBits, leastSigBits);

        int dataLength = buffer.getInt();
        byte[] data = new byte[dataLength];
        buffer.get(data);

        long sequenceNumber = buffer.getLong();
        boolean whispering = buffer.get() == 1;

        double x = buffer.getDouble();
        double y = buffer.getDouble();
        double z = buffer.getDouble();

        short worldIdLength = buffer.getShort();
        byte[] worldIdBytes = new byte[worldIdLength];
        buffer.get(worldIdBytes);
        String worldId = new String(worldIdBytes, StandardCharsets.UTF_8);

        return new PositionalMicPacket(clientId, data, sequenceNumber, whispering, x, y, z, worldId);
    }
}
