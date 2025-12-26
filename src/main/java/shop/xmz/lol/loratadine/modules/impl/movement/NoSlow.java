package shop.xmz.lol.loratadine.modules.impl.movement;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import cn.lzq.injection.leaked.invoked.SlowEvent;
import cn.lzq.injection.leaked.invoked.UpdateEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.ui.progressbar.ProgressbarManager;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.EaseBackIn;


public class NoSlow extends Module {
    public static NoSlow INSTANCE;

    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"Heypixel"}, "Heypixel");
    public final BooleanSetting hold = new BooleanSetting("Hold Slow", this, false);
    public final BooleanSetting debug = new BooleanSetting("Debug", this, false);

    private final Animation animation = new EaseBackIn(500, 1.0, 1.8f);
    public static final TimerUtils targetTimer = new TimerUtils();
    public boolean serverEat = false;
    public int serverEatTick = 0;
    Item eatFood = null;

    public NoSlow() {
        super("NoSlow", "无减速", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        targetTimer.reset();
    }

    @EventTarget
    public void onSlow(SlowEvent event) {
        if (mc.player == null) return;

        // 原有逻辑
        if (serverEat) {
            if (serverEatTick < 3) {
                event.state = true;
                KeyMapping.set(mc.options.keySprint.getKey(), false);
                mc.player.setSprinting(false);
                sendClickWindowPacketInBarrier();
            } else {
                event.state = false;
            }
        }
    }

    public void sendClickWindowPacketInBarrier() {
        if (mc.player == null) return;

        Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectOpenHashMap<>();
        modifiedStacks.put(36, new ItemStack(Items.BARRIER));

        mc.player.connection.send(new ServerboundContainerClickPacket(0, 0, 36, 0, ClickType.SWAP, new ItemStack(Blocks.BARRIER), modifiedStacks));
        mc.player.connection.send(new ServerboundContainerClosePacket(0));
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

        int y = mc.getWindow().getGuiScaledHeight() / 2 + 10;

        if (serverEatTick != 0) {
            targetTimer.reset();
            this.animation.setDirection(Direction.FORWARDS);
        }

        if (serverEatTick > 25) {
            this.animation.setDirection(Direction.BACKWARDS);
        }

        if (serverEat && !animation.finished(Direction.BACKWARDS)) {
            event.poseStack().pushPose();
            switch (HUD.INSTANCE.count_Value.getValue()) {
                case "Loratadine" ->
                        ProgressbarManager.drawLoratadineCountInfo(event.poseStack(), "Eat now...", serverEatTick / 35.0f, y);
                case "Simple" ->
                        ProgressbarManager.drawSimpleCountInfo(event.poseStack(), "Eat now...", serverEatTick / 35.0f, serverEatTick, y);
                case "Modern" ->
                        ProgressbarManager.drawModernCountInfo(event.poseStack(), animation, serverEatTick / 35.0f, y);
                case "Basic" ->
                        ProgressbarManager.drawBasicCountInfo(event.poseStack(), serverEatTick / 35.0f, y);
            }
            event.poseStack().popPose();
        }
    }

    @EventTarget
    public void onServerEatTick(LivingUpdateEvent event) {
        if (serverEat) {
            serverEatTick++;
            debugMessage("EatTick:" + serverEatTick);
        }

        if (serverEatTick == 35) {
            debugMessage("Eat:" + eatFood);
            reset();
        }
    }

    @EventTarget
    public void onUpdateEvent(UpdateEvent event) {
        if (mc.player == null) return;

        // 获取主手物品并检查是否为食物
        if ((mc.player.getMainHandItem().getItem().getFoodProperties() != null
                    || mc.player.getOffhandItem().getItem().getFoodProperties() != null) && hold.getValue() && serverEatTick < 3)
        {
            KeyMapping.set(mc.options.keySprint.getKey(), false);
            mc.player.setSprinting(false);
        }

    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.gameMode == null || mc.isLocalServer() || mc.player.isSpectator()) return;

        Packet<?> packet = event.getPacket();
            if (mc.player.getMainHandItem().getItem().getFoodProperties() != null || mc.player.getOffhandItem().getItem().getFoodProperties() != null) {
                if (packet instanceof ServerboundUseItemPacket) {
                    serverEat = true;
                    mc.gameMode.handleInventoryMouseClick(
                            0, // 背包窗口 ID (0 表示主背包)
                            0, // 随机选中的槽位编号
                            1, // 按键编号 (1 表示丢弃物品)
                            ClickType.THROW, // 丢弃操作
                            mc.player // 玩家实例
                    );
                    eatFood = mc.player.getItemInHand(((ServerboundUseItemPacket) packet).getHand()).getItem();
                }

                if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
                    if (actionPacket.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM
                            && (mc.player.getMainHandItem().getItem().getFoodProperties() != null || mc.player.getOffhandItem().getItem().getFoodProperties() != null)) {
                        event.setCancelled(true);
                    }
                }
            }
        this.setSuffix(mode_Value.getValue());
    }

    public void reset() {
        serverEatTick = 0;
        serverEat = false;
        eatFood = null;
    }

    public void debugMessage(String message) {
        if (debug.getValue()) ClientUtils.log(message);
    }
}
