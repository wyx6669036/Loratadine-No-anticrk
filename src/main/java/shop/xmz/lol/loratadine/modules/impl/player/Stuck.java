package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.MoveMathEvent;
import cn.lzq.injection.leaked.invoked.UpdateEvent;
import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.lwjgl.glfw.GLFW;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.ui.progressbar.ProgressbarManager;
import shop.xmz.lol.loratadine.utils.PacketUtils;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.EaseBackIn;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.util.concurrent.LinkedBlockingQueue;

public class Stuck extends Module {
    public final BooleanSetting onlyVoid = new BooleanSetting("Only Void",this,false);
    private final BooleanSetting addVelocityPacket = new BooleanSetting("Add VelocityPacket",this,false);

    private final LinkedBlockingQueue<Packet<ClientGamePacketListener>> packets = new LinkedBlockingQueue<>();
    private final Animation animation = new EaseBackIn(500, 1.0, 1.8f);
    public static final TimerUtils targetTimer = new TimerUtils();
    public static boolean shouldCancel = false;
    private boolean disableLogger;
    private int stuckTick;

    public Stuck() {
        super("Stuck", "卡空", Category.PLAYER, GLFW.GLFW_KEY_X);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) return;

        targetTimer.reset();

        KeyMapping.set(mc.options.keySprint.getKey(), false);
        mc.player.setSprinting(false);
    }

    @Override
    public void onDisable() {
        this.animation.setDirection(Direction.BACKWARDS);
        reset();
    }

    @EventTarget
    private void onMathEvent(MoveMathEvent event) {
        if (isPlayerValid() && !mc.player.onGround() && !mc.player.isSpectator()) {
            mc.player.setSprinting(false);
            if (stuckTick == 20) {
                event.setCancelled(false);
                reset();
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isPlayerValid()) return;

        if(mc.player.isSprinting()){
            mc.player.setSprinting(false);
        }

        if (!mc.player.isAlive() || mc.player.onGround() || (onlyVoid.getValue() && !AntiVoid.isInVoid())) {
            reset();
            this.setEnabled(false);
        }

        if (shouldCancel) {
            PacketUtils.sendPacketNoEvent(new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround()));
            WrapperUtils.setSkipTicks(1);
            shouldCancel = false;
        }

        float yaw = mc.player.getYRot() + (float) (Math.random() - 0.5);
        if (mc.player.tickCount % 2 == 0 && (KillAura.target == null || !Loratadine.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled())) {
            RotationUtils.setRotation(new Rotation(yaw, mc.player.getXRot()));
        }

        stuckTick++;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!isPlayerValid() || disableLogger || event.getSide() == Event.Side.POST || mc.player.onGround() || mc.player.isSpectator()) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundSetEntityMotionPacket wrapper && wrapper.getId() == mc.player.getId()) {
            event.setCancelled(true);
            if (addVelocityPacket.getValue()) {
                packets.add((Packet<ClientGamePacketListener>) packet);
            }
            shouldCancel = true;
        }
    }

    @EventTarget
    private void onWorld(WorldEvent event) {
        setEnabled(false);
    }

    private void reset() {
        if (!isPlayerValid()) return;

        try {
            disableLogger = true;
            while (!packets.isEmpty()) {
                packets.take().handle(mc.player.connection);
            }
            stuckTick = 0;
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            disableLogger = false;
        }
    }

    private boolean isPlayerValid() {
        return mc.player != null && mc.level != null;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

        int y = mc.getWindow().getGuiScaledHeight() / 2 + 40;

        if (stuckTick != 0) {
            targetTimer.reset();
            this.animation.setDirection(Direction.FORWARDS);
        }

        if(stuckTick > 15){
            this.animation.setDirection(Direction.BACKWARDS);
        }

        if (stuckTick != 0 && !animation.finished(Direction.BACKWARDS)) {
            event.poseStack().pushPose();
            switch (HUD.INSTANCE.count_Value.getValue()) {
                case "Loratadine" ->
                        ProgressbarManager.drawLoratadineCountInfo(event.poseStack(), "Stuck now...",stuckTick / 20F, y);
                case "Simple" ->
                        ProgressbarManager.drawSimpleCountInfo(event.poseStack(), "Stuck now...", stuckTick / 20F, stuckTick, y);
                case "Modern" ->
                        ProgressbarManager.drawModernCountInfo(event.poseStack(), animation,stuckTick / 20F, y);
                case "Basic" ->
                        ProgressbarManager.drawBasicCountInfo(event.poseStack(), stuckTick / 20F, y);
            }
            event.poseStack().popPose();
        }
    }
}