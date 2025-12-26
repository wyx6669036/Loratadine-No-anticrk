package shop.xmz.lol.loratadine.modules.impl.movement;

import cn.lzq.injection.leaked.invoked.MotionEvent;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;

public class Flight extends Module {
    private final NumberSetting horizontalSpeed = new NumberSetting("Horizontal Speed", this, 1.0, 0.1, 5.0, 0.1);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical Speed", this, 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting fakeDamage = new BooleanSetting("Fake Damage", this, true);


    public Flight() {
        super("Flight", "航班", Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        if (fakeDamage.getValue()) {
            mc.player.handleEntityEvent((byte) 2);
            mc.player.hurtTime = 10;
        }
    }

    @Override
    protected void onDisable() {
        MoveUtils.stopMove();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null) return;

        mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0, mc.player.getDeltaMovement().z);

        int hSpeed = horizontalSpeed.getValue().intValue();
        int vSpeed = verticalSpeed.getValue().intValue();

        if (mc.options.keyJump.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, vSpeed, mc.player.getDeltaMovement().z);
        } else if (mc.options.keyShift.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -vSpeed, mc.player.getDeltaMovement().z);
        }

        if (mc.player.zza != 0 || mc.player.xxa != 0) {
            float forward = mc.player.zza;
            float strafe = mc.player.xxa;
            float yaw = mc.player.getYRot();

            double sin = Math.sin(Math.toRadians(yaw));
            double cos = Math.cos(Math.toRadians(yaw));

            double motionX = (strafe * cos - forward * sin) * hSpeed;
            double motionZ = (forward * cos + strafe * sin) * hSpeed;

            mc.player.setDeltaMovement(motionX, mc.player.getDeltaMovement().y, motionZ);
        }
        mc.player.fallDistance = 0;
    }
}