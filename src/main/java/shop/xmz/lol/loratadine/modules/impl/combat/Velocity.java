package shop.xmz.lol.loratadine.modules.impl.combat;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import cn.lzq.injection.leaked.invoked.MotionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.misc.AntiBot;
import shop.xmz.lol.loratadine.modules.impl.player.Stuck;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;
import shop.xmz.lol.loratadine.utils.player.RayCastUtil;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.util.LinkedList;
import java.util.Queue;

import static shop.xmz.lol.loratadine.modules.impl.combat.KillAura.target;

public class Velocity extends Module {
    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"Vanilla", "Grim Attack","WatchDog", "Grim Full" ,"Jump Reset","Auto Stuck"}, "Vanilla");

    private final BooleanSetting attacked_FlightObject = (BooleanSetting) new BooleanSetting("Attacked FlightObject", this, false)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final NumberSetting targetMotion = (NumberSetting) new NumberSetting("Target Motion", this, 0.1, 0.01, 1, 0.001)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final NumberSetting counter = (NumberSetting) new NumberSetting("Counter", this, 1, 1, 10, 1)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final BooleanSetting rayCast = (BooleanSetting) new BooleanSetting("Ray Cast", this, true)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final BooleanSetting sprintOnly = (BooleanSetting) new BooleanSetting("Sprint Only", this, true)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final NumberSetting range = (NumberSetting) new NumberSetting("Range", this, 3, 2, 8, 0.1)
            .setVisibility(() -> mode_Value.is("Grim Attack") || mode_Value.is("Auto Stuck"));
    private final BooleanSetting c07 = (BooleanSetting) new BooleanSetting("Grim Full Send C07",this,false)
            .setVisibility(() -> mode_Value.is("Grim Full"));

    private final BooleanSetting onlyMove = new BooleanSetting("OnlyMove",this,false);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround",this,false);

    private final BooleanSetting flagCheckValue = new BooleanSetting("FlagCheck",this,false);
    public final NumberSetting flagTickValue = new NumberSetting("FlagTicks",this,6, 0, 30, 1);
    public final BooleanSetting debugMessageValue = new BooleanSetting("FlagDebugMessage",this,false);
    int flags;

    private final Queue<Packet<?>> packets = new LinkedList<>();
    public boolean attackedFlightObject = false;
    private boolean slowdownTicks = false;
    public boolean velocityInput = false;
    public boolean attacked = false;
    boolean shouldSend = false;
    double reduceXZ;

    public Velocity() {
        super("Velocity", "反击退", Category.COMBAT);
        this.setEnabled(true);
    }

    public void reset() {
        velocityInput = false;
        attacked = false;
        attackedFlightObject = false;
        reduceXZ = 0;
        packets.clear();
    }

    @EventTarget
    public void onPlayerUpdate(MotionEvent event) {
        if (mc.player == null || !event.post) return;

        if (shouldSend) {
            WrapperUtils.setSkipTicks(WrapperUtils.getSkipTicks() + 1);

            if(c07.getValue())
                mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                        BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                        Direction.getNearest(mc.player.getLookAngle().x, mc.player.getLookAngle().y, mc.player.getLookAngle().z).getOpposite()));
            mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround()));

            shouldSend = false;
        }
    }

    @EventTarget
    public void onUpdate(LivingUpdateEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (flagCheckValue.getValue()) {
            if (flags > 0) flags--;
        }

        if (mc.player.isDeadOrDying() || mc.player.getHealth() <= 0) return;

        switch (mode_Value.getValue()) {
            case "Grim Attack" -> {
                while (!packets.isEmpty()) {
                    mc.player.connection.send(packets.poll());
                }

                Vec3 deltaMovement = mc.player.getDeltaMovement();

                if (slowdownTicks) {
                    WrapperUtils.setSkipTicks(1);
                    slowdownTicks = false;
                }

                if (velocityInput) {
                    if (attacked) {
                        mc.player.setDeltaMovement(deltaMovement.x * reduceXZ, deltaMovement.y, deltaMovement.z * reduceXZ);
                        attacked = false;
                    }
                    if (attackedFlightObject) {
                        mc.player.setDeltaMovement(deltaMovement.x * reduceXZ, deltaMovement.y, deltaMovement.z * reduceXZ);
                        attackedFlightObject = false;
                    }
                    if (mc.player.hurtTime == 0) reset();
                }
            }

            case "Auto Stuck" -> {
                if (target != null
                        && target != mc.player
                        && RotationUtils.getDistanceToEntity(target) <= range.getValue().floatValue()
                        && mc.player.onGround() && mc.player.hurtTime > 0) {
                    Stuck stuck = (Stuck) Loratadine.INSTANCE.getModuleManager().getModule(Stuck.class); stuck.setEnabled(true);
                }

                if (target != null && target != mc.player && RotationUtils.getDistanceToEntity(target) >= range.getValue().floatValue()) {
                    Stuck stuck = (Stuck) Loratadine.INSTANCE.getModuleManager().getModule(Stuck.class); stuck.setEnabled(false);
                }
            }

            case "Jump Reset" -> {
                if (mc.player.onGround() && mc.player.hurtTime > 0) {
                    mc.player.setSprinting(false);
                    mc.player.input.jumping = true;
                }
            }
        }

        this.setSuffix(mode_Value.getValue());
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.level == null || mc.player == null) return;

        Packet<?> packet = event.getPacket();

        if ((onlyGround.getValue() && !mc.player.onGround()) || (onlyMove.getValue() && !MoveUtils.isMoving()) || flags != 0) {
            return;
        }

        if (packet instanceof ClientboundSetEntityMotionPacket packetEntityVelocity) {

            if (packetEntityVelocity.getId() != mc.player.getId()) return;

            switch (mode_Value.getValue()) {
                case "Vanilla" ->
                        event.setCancelled(true);

                case "WatchDog" -> {
                    event.setCancelled(true);
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, packetEntityVelocity.getYa() / 8000.0D, mc.player.getDeltaMovement().z);
                }

                case "Grim Full" -> {
                    event.setCancelled(true);
                    shouldSend = true;
                }

                case "Grim Attack" -> {
                    //Entity
                    if (target != null && target != mc.player && !mc.player.onClimbable()/* && RotationUtils.getDistanceToEntity(target) <= range.getValue().floatValue()*/) {
                        final HitResult hitResult = RayCastUtil.rayCast(new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast()), 3.0);
                        if (rayCast.getValue() && hitResult != null) {
                            if (hitResult.getType() != HitResult.Type.ENTITY || !target.equals(((EntityHitResult) hitResult).getEntity())) {
                                return;
                            }
                        }
                        boolean state = WrapperUtils.getWasSprinting();

                        if (!sprintOnly.getValue() || state) {

                            if (attacked) return;

                            velocityInput = true;

                            reduceXZ = 1;

                            if (!state) {
                                packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                                packets.offer(new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround()));
                                slowdownTicks = true;
                            }

                            final double motionX = packetEntityVelocity.getXa() / 8000.0;
                            final double motionZ = packetEntityVelocity.getZa() / 8000.0;
                            double velocityDistance = Math.sqrt(motionX * motionX + motionZ * motionZ);

                            int counter = 0;
                            while (velocityDistance * reduceXZ > targetMotion.getValue().floatValue() && counter <= this.counter.getValue().intValue()) {
                                packets.offer(ServerboundInteractPacket.createAttackPacket(target, mc.player.isShiftKeyDown()));
                                packets.offer(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                reduceXZ *= 0.6;
                                counter++;
                            }

                            if (!state) {
                                packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                            }

                            attacked = true;
                        }
                    }

                    //Projectile
                    if (attacked_FlightObject.getValue()) {
                        for (Entity entity : mc.level.entitiesForRendering()) {
                            if (entity != null
                                    && entity != mc.player
                                    && entity instanceof Projectile
                                    && !((AntiBot) Loratadine.INSTANCE.getModuleManager().getModule(AntiBot.class)).isServerBot(entity)
                                    && RotationUtils.getDistanceToEntity(entity) > 6.0) {
                                final HitResult hitResult = RayCastUtil.rayCast(new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast()), 3.0);
                                if (rayCast.getValue() && hitResult != null) {
                                    if (hitResult.getType() != HitResult.Type.ENTITY || !entity.equals(((EntityHitResult) hitResult).getEntity())) {
                                        return;
                                    }
                                }

                                if (attackedFlightObject) return;

                                if (entity.onGround()) continue;

                                velocityInput = true;

                                boolean state = WrapperUtils.getWasSprinting();

                                reduceXZ = 1;

                                if (!state) {
                                    packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                                    packets.offer(new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround()));
                                    slowdownTicks = true;
                                }

                                final double motionX = packetEntityVelocity.getXa() / 8000.0;
                                final double motionZ = packetEntityVelocity.getZa() / 8000.0;
                                double velocityDistance = Math.sqrt(motionX * motionX + motionZ * motionZ);

                                int counter = 0;
                                while (velocityDistance * reduceXZ > targetMotion.getValue().floatValue() && counter <= this.counter.getValue().intValue()) {
                                    packets.offer(ServerboundInteractPacket.createAttackPacket(entity, mc.player.isShiftKeyDown()));
                                    packets.offer(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                    reduceXZ *= 0.6;
                                    counter++;
                                }

                                if (!state) {
                                    packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                                }

                                attackedFlightObject = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (packet instanceof ClientboundPlayerPositionPacket && flagCheckValue.getValue()) {
            flags = flagTickValue.getValue().intValue();
            if (debugMessageValue.getValue()) ClientUtils.log("[Velocity]Debug Flags.");
        }
    }
}