package shop.xmz.lol.loratadine.antileak.packet.impls.send;

import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;

public class CPacketChat extends Packet {
    private final String message;

    public CPacketChat(Channel channel, String message) {
        super(channel);
        this.message = message;
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
        wrapper.writeString(message);
    }

    @Override
    public int packetId() {
        return 3;
    }
}
