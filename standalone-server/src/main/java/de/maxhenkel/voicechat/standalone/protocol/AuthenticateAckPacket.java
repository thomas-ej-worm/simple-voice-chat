package de.maxhenkel.voicechat.standalone.protocol;

import java.nio.ByteBuffer;

/**
 * Authentication acknowledgment packet sent by server to client.
 */
public class AuthenticateAckPacket {

    private final boolean success;

    public AuthenticateAckPacket(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public byte[] encode() {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(ProtocolConstants.PACKET_TYPE_AUTHENTICATE_ACK);
        buffer.put((byte) (success ? 1 : 0));
        return buffer.array();
    }

    public static AuthenticateAckPacket decode(ByteBuffer buffer) {
        byte successByte = buffer.get();
        return new AuthenticateAckPacket(successByte == 1);
    }
}
