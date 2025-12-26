package shop.xmz.lol.loratadine.modules.impl.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.DragManager;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.ui.clickguis.compact.CompactClickGUI;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.DropdownClickGUI;
import shop.xmz.lol.loratadine.ui.targethud.*;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.EaseBackIn;

/**
 * @author Jon_awa / DSJ_ / DreamDev
 * @since 13/2/2025
 */
public class TargetHUD extends DragManager {
    private final ModeSetting mode = new ModeSetting("TargetMode", this, new String[]{"Loratadine","Chill","LSD", "Modern", "Simple", "Exhibition","Modern Remix"}, "Loratadine");
    private final Animation animation = new EaseBackIn(500, 1.0, 1.8f);
    public static final TimerUtils targetTimer = new TimerUtils();

    // 在构造函数中设置合适的默认尺寸
    public TargetHUD() {
        super("TargetHUD", "目标信息", 100, 40);

        // 设置默认位置在屏幕中间偏右
        xPercentSetting.setValue(70);
        yPercentSetting.setValue(30);
    }

    @Override
    public void onEnable() {
        targetTimer.reset();
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

        // 更新位置和尺寸 - 确保在任何渲染之前
        updatePosition();

        // 获取目标实体
        LivingEntity target = KillAura.target;

        // 更新动画方向
        if (target != null || mc.screen instanceof CompactClickGUI || mc.screen instanceof DropdownClickGUI) {
            targetTimer.reset();
            this.animation.setDirection(Direction.FORWARDS);
        } else {
            this.animation.setDirection(Direction.BACKWARDS);
        }

        // 先入栈保存状态
        PoseStack poseStack = event.poseStack();

        // Simple模式单独处理
        if (mode.getValue().equals("Simple")) {
            Simple.drawTargetInfo(poseStack, target, (int) getX(), (int) getY());
        }

        // 其他模式正常处理
        if (!animation.finished(Direction.BACKWARDS) || mc.screen instanceof CompactClickGUI || mc.screen instanceof DropdownClickGUI) {
            LivingEntity finalTarget = null;

            if (target != null) {
                finalTarget = target;
            } else if (mc.screen instanceof CompactClickGUI || mc.screen instanceof DropdownClickGUI) {
                finalTarget = mc.player;
            }

            poseStack.pushPose(); // 入栈

            // 根据模式渲染
            switch (mode.getValue()) {
                case "Loratadine" ->
                        Loratadine.drawTargetInfo(poseStack, finalTarget, animation, (int) getX(), (int) getY());
                case "Modern" ->
                        Modern.drawTargetInfo(poseStack, finalTarget, animation, (int) getX(), (int) getY());
                case "Exhibition" ->
                        Exhibition.drawTargetInfo(poseStack, finalTarget, animation, (int) getX(), (int) getY());
                case "Chill" ->
                        Chill.drawTargetInfo(poseStack, finalTarget, animation, (int) getX(), (int) getY());
                case "LSD" ->
                        LSD.drawTargetInfo(poseStack, finalTarget, animation, (int) getX(), (int) getY());
                case "Modern Remix" ->
                        Remix.drawTargetInfo(poseStack, finalTarget, animation, (int) getX(), (int) getY());
            }

            poseStack.popPose(); // 出栈
        }

        // 始终渲染拖动效果和高亮
        renderDragEffects(poseStack);
        updateHighlightAnimation();
    }

    @Override
    public boolean shouldRenderDragHighlight() {
        return dragging;
    }
}