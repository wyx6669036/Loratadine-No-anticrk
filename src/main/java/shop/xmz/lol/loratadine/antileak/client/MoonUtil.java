package shop.xmz.lol.loratadine.antileak.client;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.antileak.Fucker;
import shop.xmz.lol.loratadine.utils.NativeUtils;
import shop.xmz.lol.loratadine.utils.PacketUtils;
import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;

import java.util.HashSet;

public class MoonUtil extends Thread {
    @Override
    public void run() {
//        while (true) {
//            try {
//                Thread.sleep(50000L);
//                if (Loratadine.INSTANCE.getModuleManager() != null && Client.channel == null || !Client.channel.isActive() || !Fucker.login) {
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
//                }
//            } catch (Throwable ignored) {}
//        }
    }
}