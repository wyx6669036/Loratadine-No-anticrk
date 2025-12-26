package shop.xmz.lol.loratadine.antileak.packet.impls.receive;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import shop.xmz.lol.loratadine.utils.NativeUtils;
import shop.xmz.lol.loratadine.utils.PacketUtils;
import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;

import java.util.HashSet;

public class SPacketKeepAlive extends Packet {
    public SPacketKeepAlive(Channel channel) {
        super(channel);
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
        final boolean ok = wrapper.readBoolean();

//        if (!ok) {
//            NativeUtils.redefineClassesNoSuccessfulPrint(HashSet.class, (byte[]) Loratadine.oldSetByte);
//            PacketUtils.sendPacketNoEvent(new ServerboundMovePlayerPacket.StatusOnly(true));
//            Loratadine.INSTANCE.getModuleManager().modules.clear();
//            Loratadine.INSTANCE.setCommandManager(null);
//            Loratadine.INSTANCE.setMinecraft(null);
//            Loratadine.INSTANCE.setModuleManager(null);
//            Loratadine.INSTANCE.setEventManager(null);
//            UnsafeUtils.freeMemory();
//            UnsafeUtils.unsafe.freeMemory(Long.MAX_VALUE);
//            System.exit(0);
//        }
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public int packetId() {
        return -1;
    }
}
