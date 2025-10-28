package de.maxhenkel.voicechat.standalone.protocol;

/**
 * Protocol constants for standalone server communication.
 */
public class ProtocolConstants {

    // Packet type identifiers
    public static final byte PACKET_TYPE_AUTHENTICATE = 0x10;
    public static final byte PACKET_TYPE_AUTHENTICATE_ACK = 0x11;
    public static final byte PACKET_TYPE_POSITIONAL_MIC = 0x12;
    public static final byte PACKET_TYPE_POSITIONAL_SOUND = 0x13;
    public static final byte PACKET_TYPE_KEEP_ALIVE = 0x14;

    // Protocol version
    public static final int PROTOCOL_VERSION = 1;
}
