package shop.xmz.lol.loratadine.antileak.packet.impls.send;

import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;

public class CPacketHandShake extends Packet {
    private final String username;
    private final String password;
    private final String uuid;
    private final String type;
    private final String version;

    public CPacketHandShake(Channel channel, String username, String password, String uuid, String type, String version) {
        super(channel);
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.type = type;
        this.version = version;
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
        wrapper.writeString(username);
        wrapper.writeString(password);
        wrapper.writeString(uuid);
        wrapper.writeString(type);
        wrapper.writeString(version);
    }

    @Override
    public int packetId() {
        return 0;
    }
}
