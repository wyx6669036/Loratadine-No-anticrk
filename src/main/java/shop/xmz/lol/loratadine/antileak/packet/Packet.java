package shop.xmz.lol.loratadine.antileak.packet;

import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;

public abstract class Packet {
    public final Channel channel;

    public Packet(final Channel channel) {
        this.channel = channel;
    }

    public abstract void read(PacketWrapper wrapper) throws Throwable;

    public abstract void write(PacketWrapper wrapper) throws Throwable;

    public abstract int packetId();
}
