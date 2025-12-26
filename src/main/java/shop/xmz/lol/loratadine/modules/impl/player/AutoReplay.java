package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.MotionEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;
import shop.xmz.lol.loratadine.utils.TimerUtils;


public class AutoReplay extends Module {
    public static int rounds = 0;
    private final TimerUtils timer = new TimerUtils();
    private boolean shouldReplay = false;
    private long triggerTime = 0;

    public NumberSetting delay = new NumberSetting("Delay", this,1000, 0, 5000, 100);

    public AutoReplay() {
        super("AutoReplay", "自动游戏", Category.PLAYER);
    }

    @EventTarget
    public void onChatReceived(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundSystemChatPacket wrapper) {

            if (!isEnabled() || wrapper.content().getString().isEmpty()) return;

            String message = wrapper.content().getString();
            String playerName = mc.player.getGameProfile().getName();

            if (message.startsWith("<" + playerName + ">")) return;

            if (message.contains("游戏结束")) {
                rounds++;
                shouldReplay = true;
                triggerTime = System.currentTimeMillis() + (long) delay.getValue();
                NotificationManager.add(NotificationType.INFO, "AutoReplay", "准备自动开始下一局！");
            }
        }
    }

    @EventTarget
    public void onUpdate(MotionEvent event) {
        if (!isEnabled() || !shouldReplay) return;

        if (System.currentTimeMillis() >= triggerTime) {
            shouldReplay = false;

            ItemStack stack = mc.player.getInventory().getItem(4);
            if (stack.getItem() != Items.EMERALD) {
                NotificationManager.add(NotificationType.INFO, "AutoReplay","操你妈！");
                return;
            }

            int originalSlot = mc.player.getInventory().selected;

            try {
                mc.player.getInventory().selected = 4;
                //PacketUtils.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));
                timer.reset();
            } finally {
                mc.player.getInventory().selected = originalSlot;
            }
        }
    }
}