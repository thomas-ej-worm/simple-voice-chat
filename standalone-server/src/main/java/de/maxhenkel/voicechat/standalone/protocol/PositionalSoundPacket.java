package de.maxhenkel.voicechat.standalone.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Sound packet with position data sent by server to clients.
 */
public class PositionalSoundPacket {

    private final UUID channelId;
    private final UUID senderId;
    private final byte[] data;
    private final long sequenceNumber;
    private final boolean whispering;
    private final double x, y, z;
    private final String worldId;
    private final float distance;

    public PositionalSoundPacket(UUID channelId, UUID senderId, byte[] data, long sequenceNumber,
                                 boolean whispering, double x, double y, double z, String worldId, float distance) {
        this.channelId = channelId;
        this.senderId = senderId;
        this.data = data;
        this.sequenceNumber = sequenceNumber;
        this.whispering = whispering;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
        this.distance = distance;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public UUID getSenderId() {
        return senderId;
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

    public float getDistance() {
        return distance;
    }

    public byte[] encode() {
        byte[] worldIdBytes = worldId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(1 + 32 + 4 + data.length + 8 + 1 + 24 + 2 + worldIdBytes.length + 4);

        buffer.put(ProtocolConstants.PACKET_TYPE_POSITIONAL_SOUND);
        buffer.putLong(channelId.getMostSignificantBits());
        buffer.putLong(channelId.getLeastSignificantBits());
        buffer.putLong(senderId.getMostSignificantBits());
        buffer.putLong(senderId.getLeastSignificantBits());
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.putLong(sequenceNumber);
        buffer.put((byte) (whispering ? 1 : 0));
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putShort((short) worldIdBytes.length);
        buffer.put(worldIdBytes);
        buffer.putFloat(distance);

        return buffer.array();
    }

    public static PositionalSoundPacket decode(ByteBuffer buffer) {
        long channelMsb = buffer.getLong();
        long channelLsb = buffer.getLong();
        UUID channelId = new UUID(channelMsb, channelLsb);

        long senderMsb = buffer.getLong();
        long senderLsb = buffer.getLong();
        UUID senderId = new UUID(senderMsb, senderLsb);

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

        float distance = buffer.getFloat();

        return new PositionalSoundPacket(channelId, senderId, data, sequenceNumber, whispering, x, y, z, worldId, distance);
    }
}
