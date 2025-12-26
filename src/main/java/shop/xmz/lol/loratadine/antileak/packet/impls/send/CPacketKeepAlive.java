package shop.xmz.lol.loratadine.antileak.packet.impls.send;

import shop.xmz.lol.loratadine.antileak.Fucker;
import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.utils.CryptUtil;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class CPacketKeepAlive extends Packet {
    public CPacketKeepAlive(Channel channel) {
        super(channel);
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
        wrapper.writeBytes(CryptUtil.Sign.sign((String.valueOf(Fucker.userData.index.hashCode() ^ 3 & 31) + Fucker.userData.uuid.hashCode()).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public int packetId() {
        return -1;
    }
}
