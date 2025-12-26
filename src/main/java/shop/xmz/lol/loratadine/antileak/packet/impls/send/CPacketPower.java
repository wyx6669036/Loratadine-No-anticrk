package shop.xmz.lol.loratadine.antileak.packet.impls.send;

import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;

public class CPacketPower extends Packet {
    private final int state;
    private final String username;
    private final long time;

    public CPacketPower(Channel channel, int state, String username, long time) {
        super(channel);
        this.state = state;
        this.username = username;
        this.time = time;
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
        wrapper.writeInt(state);
        wrapper.writeString(username);
        if (state == 1)
            wrapper.writeLong(time);
    }

    @Override
    public int packetId() {
        return 4;
    }
}
