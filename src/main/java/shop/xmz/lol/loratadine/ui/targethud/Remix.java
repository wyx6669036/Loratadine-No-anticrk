package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import javax.annotation.Nullable;
import java.awt.*;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

public class Remix {
    private static long lastUpdateTime = System.currentTimeMillis();
    private static LivingEntity lastTarget;
    private static float easingHealth = 0f;

    public static void drawTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int x, int y) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;
        float healthPresent = target != null ? health / target.getMaxHealth() : 0;

        // 更新缓动血量动画
        updateEasingHealth(health);

        // 盔甲
        int armor = target != null ? target.getArmorValue() : 0;
        float armorPresent = target != null ? (float) armor / 20 : 0;

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        int healthWidth = target != null ? (int) Math.max(130, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : 130;
        int armorWidth = target != null ? (int) Math.max(95.5F, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : (int) 95.5F;
        float presentWidth_health = Math.min(healthPresent, 1) * healthWidth;
        float presentWidth_armor = Math.min(armorPresent, 1) * armorWidth;

        //各种颜色
        Color bgColor = new Color(30, 30, 30);

        Color healthBgColor = new Color(80, 0, 0);
        Color healthColor = new Color(0, 165, 0);

        Color armorBgColor = new Color(0, 0, 80);
        Color armorColor = new Color(0, 0, 165);


        Color rectBgColor = new Color(70, 70, 70);
        Color rectOutlineColor = new Color(0, 0, 0);

        Color textColor = new Color(200, 200, 200);

        // 动画
        poseStack.pushPose();
        poseStack.translate(((float) x + 70) * (1 - animation.getOutput()), ((float) y + 25) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制边框
        RenderUtils.drawRoundedRect(poseStack, (float) x, (float) y, 140, 50,0, bgColor);

        // 绘制头像
        try {
            if (target != null) {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, (float) x + 5, (float) y + 5, 32, 32, player);
            } else {
                RenderUtils.drawRoundedRect(poseStack, (float) x + 5, (float) y + 5, 32, 32, 0, rectBgColor);
            }
        } catch (Exception e) {
            RenderUtils.drawRoundedRect(poseStack, (float) x + 5, (float) y + 5, 32, 32, 0, rectBgColor);
        }

        // 绘制血条
        RenderUtils.drawRoundedRect(poseStack, (float) x + 5, (float) y + 40, 130, 5,0, healthBgColor);
        RenderUtils.drawRoundedRect(poseStack, (float) x + 5, (float) y + 40, presentWidth_health, 5,0, healthColor);

        // 绘制盔甲条
        RenderUtils.drawRoundedRect(poseStack, (float) x + 39, (float) y + 35.5F, 95.5F, 1,0, armorBgColor);
        RenderUtils.drawRoundedRect(poseStack, (float) x + 39, (float) y + 35.5F, presentWidth_armor, 1,0, armorColor);

        // 绘制盔甲框
        RenderUtils.drawRoundedRect(poseStack, (float) x + 39, (float) y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, (float) x + 40, (float) y + 17, 16, 16,0, rectBgColor);

        RenderUtils.drawRoundedRect(poseStack, (float) x + 39 + 20, (float) y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, (float) x + 40 + 20, (float) y + 17, 16, 16,0, rectBgColor);

        RenderUtils.drawRoundedRect(poseStack, (float) x + 39 + 40, (float) y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, (float) x + 40 + 40, (float) y + 17, 16, 16,0, rectBgColor);

        RenderUtils.drawRoundedRect(poseStack, (float) x + 39 + 60, (float) y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, (float) x + 40 + 60, (float) y + 17, 16, 16,0, rectBgColor);

        if (target != null) {
            RenderUtils.renderItemIcon(poseStack, (float) x + 40, (float) y + 18, target.getItemBySlot(EquipmentSlot.HEAD));
            RenderUtils.renderItemIcon(poseStack, (float) x + 60, (float) y + 18, target.getItemBySlot(EquipmentSlot.CHEST));
            RenderUtils.renderItemIcon(poseStack, (float) x + 80, (float) y + 18, target.getItemBySlot(EquipmentSlot.LEGS));
            RenderUtils.renderItemIcon(poseStack, (float) x + 100, (float) y + 18, target.getItemBySlot(EquipmentSlot.FEET));
        }

        // 绘制名字
        fontManager.zw22.drawString(poseStack, name, (float) x + 40, (float) y + 4, textColor.getRGB(),true);

        // 绘制Ping数值
        if (target instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) target;
            PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
            if (info != null) {
                int ping = info.getLatency();

                // 绘制Ping数值（带颜色）
                String pingText = ping + "ms";
                int color = textColor.getRGB();
                fontManager.zw12.drawString(poseStack, pingText, (float) x + 135 - fontManager.zw12.getStringWidth(pingText), (float) y + 28, color,true);
                fontManager.icon18.drawString(poseStack, "y", (float) x + 128, (float) y + 22, getPingColor(ping),true);
            }
        }
    }

    private static int getPingColor(int ping) {
        if (ping < 100) return new Color(0, 165, 0).getRGB();  // 亮绿
        if (ping < 200) return new Color(165, 165, 0).getRGB();  // 黄色
        if (ping < 300) return new Color(165, 80, 0).getRGB();  // 橙色
        if (ping < 400) return new Color(165, 0, 0).getRGB();  // 红色
        return new Color(80, 0, 0).getRGB();                  // 深红
    }

    /**
     * 更新缓动动画血量值
     */
    private static void updateEasingHealth(float targetHealth) {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float changeAmount = Math.abs(easingHealth - targetHealth);
        float baseSpeed = 0.02f; // 基础速度
        float speed = baseSpeed * deltaTime; // 根据时间差缩放速度

        if (changeAmount > 5) {
            speed *= 2.0f;
        } else if (changeAmount > 2) {
            speed *= 1.5f;
        }

        if (Math.abs(easingHealth - targetHealth) < 0.1) {
            easingHealth = targetHealth;
        } else if (easingHealth > targetHealth) {
            easingHealth -= Math.min(speed * 1.2f, easingHealth - targetHealth);
        } else {
            easingHealth += Math.min(speed, targetHealth - easingHealth);
        }
    }
}
