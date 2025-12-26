package shop.xmz.lol.loratadine.antileak.packet.impls.receive;

import shop.xmz.lol.loratadine.antileak.Fucker;
import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.user.UserData;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;

public class SPacketPlayer extends Packet {
    public SPacketPlayer(Channel channel) {
        super(channel);
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
        final int state = wrapper.readInt();
        final String name1 = wrapper.readString();

        switch (state) {
            case 0:
                final UserData user = wrapper.readUserOthers();
                Fucker.userNames.put(name1, user);

                break;

            case 1:
                final String name2 = wrapper.readString();
                final UserData data = Fucker.userNames.get(name1);
                if (data != null) {
                    Fucker.userNames.remove(name1);
                    Fucker.userNames.put(name2, data);
                }

                break;

            case 2:
                Fucker.userNames.remove(name1);

                break;
        }
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public int packetId() {
        return 2;
    }
}
