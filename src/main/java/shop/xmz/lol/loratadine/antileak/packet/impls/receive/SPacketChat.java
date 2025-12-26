package shop.xmz.lol.loratadine.antileak.packet.impls.receive;

import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.user.UserData;
import shop.xmz.lol.loratadine.antileak.user.UserType;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;
import shop.xmz.lol.loratadine.utils.ClientUtils;

public class SPacketChat extends Packet {
    public SPacketChat(Channel channel) {
        super(channel);
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
        final UserData sender = wrapper.readUserOthers();
        final String message = wrapper.readString();

        ClientUtils.displayIRC(UserType.getFromName(sender.userLimits.tag).getDisplayName() + "§r " + sender.playerName + " (" + sender.username + ") : " + message);
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public int packetId() {
        return 4;
    }
}
