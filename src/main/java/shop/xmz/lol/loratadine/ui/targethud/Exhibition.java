package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import javax.annotation.Nullable;
import java.awt.*;

public class Exhibition {
    private static LivingEntity lastTarget;

    public static void drawTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int x, int y) {

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 各种颜色
        Color darkest = new Color(0, 0, 0);
        Color lineColor = new Color(104, 104, 104);
        Color dark = new Color(70, 70, 70);

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + 70) * (1 - animation.getOutput()), (y + 25) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制边框
        RenderUtils.drawRectangle(poseStack, x, y, 140, 50, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 0.5F, y + 0.5F, 139, 49, lineColor.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 1.5F, y + 1.5F, 137, 47, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 2, y + 2, 136, 46, dark.getRGB());

        // 绘制名字
        String targetName = target != null ? target.getName().getString() : "Player";
        WrapperUtils.draw(poseStack, targetName, x + 40, y + 6, Color.WHITE.getRGB());

        // 获取计分板血量
        int healthScore = target != null ? (int) target.getHealth() : 0;

        // 绘制详细文本
        String name = "HP: " + healthScore + " | Dist: " + (target != null ? Math.round(RotationUtils.getDistanceToEntity(target)) : 0);
        poseStack.pushPose();
        poseStack.scale(0.7F, 0.7F, 0.7F);
        WrapperUtils.draw(poseStack, name, (x + 40F) * (1F / 0.7F), (y + 17F) * (1F / 0.7F), Color.WHITE.getRGB());
        poseStack.popPose();

        // 绘制血条
        double health = Math.min(healthScore, target != null ? target.getMaxHealth() : 20);
        int healthColor = target != null ? getColor(target).getRGB() : new Color(120, 0, 0).getRGB();

        // 绘制整个血条
        float x2 = x + 40F;
        RenderUtils.drawRectangle(poseStack, x2, y + 25, (float) ((100 - 9) * (health / (target != null ? target.getMaxHealth() : 20))), 6, healthColor);
        RenderUtils.drawRectangle(poseStack, x2, y + 25, 91, 1, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x2, y + 30, 91, 1, darkest.getRGB());

        // 绘制血条中间的线
        for (int i = 0; i < 10; i++) {
            RenderUtils.drawRectangle(poseStack, x2 + 10 * i, y + 25, 1, 6, darkest.getRGB());
        }

        // 绘制手持物品
        if (target != null) {
            RenderUtils.renderItemIcon(poseStack, x2, y + 31, target.getMainHandItem());
            RenderUtils.renderItemIcon(poseStack, x2 + 15, y + 31, target.getItemBySlot(EquipmentSlot.HEAD));
            RenderUtils.renderItemIcon(poseStack, x2 + 30, y + 31, target.getItemBySlot(EquipmentSlot.CHEST));
            RenderUtils.renderItemIcon(poseStack, x2 + 45, y + 31, target.getItemBySlot(EquipmentSlot.LEGS));
            RenderUtils.renderItemIcon(poseStack, x2 + 60, y + 31, target.getItemBySlot(EquipmentSlot.FEET));
        }

        // 绘制模型
        if (target != null) {
            poseStack.pushPose();
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.translate((x + 20) * (1 / 0.4), (y + 44) * (1 / 0.4), 40f * (1 / 0.4));
            RenderUtils.drawModel(poseStack, target.getYRot(), target.getXRot(), target);
            poseStack.popPose();
        }

        // 结束绘制
        poseStack.popPose();
    }

    //最好的血条颜色
    private static Color getColor(LivingEntity target) {
        Color healthColor = new Color(0, 165, 0);
        if (target.getHealth() < target.getMaxHealth() / 1.5)
            healthColor = new Color(200, 200, 0);
        if (target.getHealth() < target.getMaxHealth() / 2.5)
            healthColor = new Color(200, 155, 0);
        if (target.getHealth() < target.getMaxHealth() / 4)
            healthColor = new Color(120, 0, 0);
        return healthColor;
    }
}
