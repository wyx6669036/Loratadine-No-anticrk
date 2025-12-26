package shop.xmz.lol.loratadine.utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.antileak.Fucker;
import shop.xmz.lol.loratadine.antileak.client.Client;
import shop.xmz.lol.loratadine.antileak.packet.impls.send.CPacketPlayer;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.HashSet;

public class FuckerUtils implements Wrapper {
    private static boolean loaded = false;
    private static final TimerUtils TIMER_UTILS = new TimerUtils();

    public static void a() {
        if (mc.level == null || mc.player == null) return;

        loaded();

//        if (TIMER_UTILS.delay(50000L)) {
//            if (Loratadine.INSTANCE.getModuleManager() != null
//                    && Client.channel == null
//                    || !Client.channel.isActive()
//                    || !Fucker.login) {
//                new Thread(() -> {
//                    NativeUtils.redefineClassesNoSuccessfulPrint(HashSet.class, (byte[]) Loratadine.oldSetByte);
//                    PacketUtils.sendPacketNoEvent(new ServerboundMovePlayerPacket.StatusOnly(true));
//                    Loratadine.INSTANCE.getModuleManager().modules.clear();
//                    Loratadine.INSTANCE.setCommandManager(null);
//                    Loratadine.INSTANCE.setMinecraft(null);
//                    Loratadine.INSTANCE.setModuleManager(null);
//                    Loratadine.INSTANCE.setEventManager(null);
//                    UnsafeUtils.freeMemory();
//                    UnsafeUtils.unsafe.freeMemory(Long.MAX_VALUE);
//                    System.exit(0);
//                }).start();
//            }
//            TIMER_UTILS.reset();
//        }
    }

    private static void loaded() {
        if (!loaded) {
            if (mc.player == null || mc.level == null) return;

            loaded = true;

            if (!Loratadine.isDllInject) Loratadine.INSTANCE.loadClientResource();

            if (Client.channel != null) {
                try {
                    PacketWrapper.release(new CPacketPlayer(Client.channel));
                } catch (Throwable ignored) {}
            }
        }
    }

    public static boolean onPacket(Object packet, Event.Side side) {
        if (mc.level == null || mc.player == null || packet == null) return false;

        if (packet instanceof Packet<?> wrapper) {
            if (PacketUtils.handleSendPacket((Packet<ServerGamePacketListener>) packet)) return true;

            final PacketEvent event = new PacketEvent(side, wrapper);

            Loratadine.INSTANCE.getEventManager().call(event);

            return event.isCancelled();
        }
        return false;
    }
}
