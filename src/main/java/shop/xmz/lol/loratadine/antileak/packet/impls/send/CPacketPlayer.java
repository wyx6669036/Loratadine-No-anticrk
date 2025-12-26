package shop.xmz.lol.loratadine.antileak.packet.impls.send;

import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class CPacketPlayer extends Packet implements Wrapper {
    public CPacketPlayer(Channel channel) {
        super(channel);
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
        if (mc.player != null) {
            wrapper.writeString(mc.player.getScoreboardName());
        }
    }

    @Override
    public int packetId() {
        return 2;
    }
}
