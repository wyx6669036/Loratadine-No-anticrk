package shop.xmz.lol.loratadine.modules.impl.misc;


import cn.lzq.injection.leaked.invoked.UpdateEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HealthBypass extends Module {

    public static final Map<String, AtomicInteger> HEALTHS = new HashMap<>();

    public HealthBypass() {
        super("HealthBypass", "真实血量",Category.MISC);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        for (AbstractClientPlayer player : mc.level.players()) {
            if (player != mc.player && HEALTHS.containsKey(player.getName().getString())) {
                player.setHealth(Math.max(1, HEALTHS.get(player.getName().getString()).get()));
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof ClientboundSetScorePacket packet) {
            if (mc.level != null && ("belowHealth".equals(packet.getObjectiveName()) || "health".equals(packet.getObjectiveName()))) {

                if (!packet.getOwner().equals(mc.player.getGameProfile().getName())) {
                    if (!HEALTHS.containsKey(packet.getOwner())) {
                        AtomicInteger atomic = new AtomicInteger();
                        HEALTHS.put(packet.getOwner(), atomic);
                    }
                    HEALTHS.get(packet.getOwner()).set(packet.getScore());
                }
            }
        }
    }
}
