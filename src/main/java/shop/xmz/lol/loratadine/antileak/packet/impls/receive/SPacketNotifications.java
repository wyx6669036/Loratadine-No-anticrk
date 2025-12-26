package shop.xmz.lol.loratadine.antileak.packet.impls.receive;

import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;
import shop.xmz.lol.loratadine.utils.ClientUtils;

public class SPacketNotifications extends Packet {
    public SPacketNotifications(Channel channel) {
        super(channel);
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
        final int state = wrapper.readInt();
        final String text = wrapper.readString();

        switch (state) {
            case 0: // 用户上下线
                ClientUtils.displayIRC("§7通知§r -> " + text);
                break;

            case 1: // 通知
                ClientUtils.displayIRC("§7通知§r -> " + text);
                break;

            case 2: // 警告
                ClientUtils.displayIRC("§4警告§r -> " + text);
                break;

            case 3: // 成功
                ClientUtils.displayIRC("§a成功§r -> " + text);
                break;

            case 4: // 失败
                ClientUtils.displayIRC("§c失败§r -> " + text);
                break;

            case 5: // 错误
                ClientUtils.displayIRC("§c错误§r -> " + text);
                break;
        }
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public int packetId() {
        return 1;
    }
}
