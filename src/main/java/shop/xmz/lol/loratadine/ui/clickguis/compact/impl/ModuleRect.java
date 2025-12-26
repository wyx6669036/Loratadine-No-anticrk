package shop.xmz.lol.loratadine.ui.clickguis.compact.impl;

import net.minecraft.client.gui.GuiGraphics;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;

public class ModuleRect implements Wrapper {
    public Module module;
    public float x, y, width, height;
    public float rectWidth, rectHeight;
    private final SettingComponents settingComponents;
    public boolean typing;

    // 启用/禁用动画
    private float enableAnimation = 0f;
    private static final float ANIMATION_DURATION = 150f; // 毫秒

    // 单次弹跳动画参数
    private float bounceAnimation = 0f;
    private boolean isAnimating = false;
    private long animationStartTime = 0;
    private static final long BOUNCE_DURATION = 300; // 弹跳动画持续时间（毫秒）
    private boolean lastEnabledState = false;

    public ModuleRect(Module module) {
        this.module = module;
        this.settingComponents = new SettingComponents(module);
        this.rectHeight = 0;
        this.lastEnabledState = module.isEnabled();

        // 初始化开关状态
        if (module.isEnabled()) {
            enableAnimation = 1.0f;
        }
    }

    public void initGui() {
        settingComponents.initGui();
        enableAnimation = module.isEnabled() ? 1.0f : 0.0f;
        lastEnabledState = module.isEnabled();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        typing = false;

        // 检测模块状态是否变化
        boolean currentEnabled = module.isEnabled();
        if (currentEnabled != lastEnabledState) {
            // 记录状态变化，触发动画
            lastEnabledState = currentEnabled;
            animationStartTime = System.currentTimeMillis();
            isAnimating = true;
        }

        // 更新启用/禁用基础动画
        updateEnableAnimation(partialTicks, currentEnabled);

        // 更新单次弹跳动画
        updateSingleBounceAnimation();

        // 模块背景
        RenderUtils.drawRectangle(guiGraphics.pose(), x, y, rectWidth, 20, new Color(100, 100, 100).getRGB());

        // 获取字体
        boolean isEnglish = ((HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))
                .languageValue.getValue().equals("English");
        TrueTypeFont titleFont = isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity20 :
                Loratadine.INSTANCE.getFontManager().zw20;

        // 模块名称
        String moduleName = isEnglish ? module.getName() : module.getCnName();
        titleFont.drawString(guiGraphics.pose(), moduleName, x + 5, y + 10 - titleFont.getHeight() / 2f, -1);

        // 绘制启用指示器
        Color accentColor = HUD.INSTANCE.getColor(0);
        Color disabledColor = new Color(64, 68, 75);
        Color darkBgColor = new Color(45, 45, 45);

        // 计算弹跳缩放和位置
        float toggleSize = 10f;
        float bouncedSize = toggleSize * (1f + bounceAnimation * 0.5f);

        // 计算偏移量，保持开关居中
        float offsetX = (bouncedSize - toggleSize) / 2;
        float offsetY = (bouncedSize - toggleSize) / 2;

        // 计算开关位置
        float toggleX = x + rectWidth - 15 - offsetX;
        float toggleY = y + 5 - offsetY;

        // 绘制外框 - 使用启用动画混合颜色
        Color borderColor = ColorUtils.interpolateColorC(disabledColor, accentColor, enableAnimation);
        RenderUtils.drawRectangle(guiGraphics.pose(),
                toggleX,
                toggleY,
                bouncedSize,
                bouncedSize,
                borderColor.getRGB());

        // 绘制内框
        RenderUtils.drawRectangle(guiGraphics.pose(),
                toggleX + 1,
                toggleY + 1,
                bouncedSize - 2,
                bouncedSize - 2,
                darkBgColor.getRGB());

        // 如果启用，绘制填充
        if (enableAnimation > 0.0f) {
            int alpha = (int)(255 * enableAnimation);
            Color fillColor = new Color(
                    accentColor.getRed(),
                    accentColor.getGreen(),
                    accentColor.getBlue(),
                    alpha);

            RenderUtils.drawRectangle(guiGraphics.pose(),
                    toggleX + 2,
                    toggleY + 2,
                    bouncedSize - 4,
                    bouncedSize - 4,
                    fillColor.getRGB());
        }

        // 渲染模块设置面板
        float settingsHeight = 0;
        if (module.getSettings().size() > 0) {
            settingComponents.actualColor = accentColor;
            settingsHeight = settingComponents.calculateHeight();

            if (settingsHeight > 0) {
                RenderUtils.drawRectangle(guiGraphics.pose(), x, y + 20, rectWidth, settingsHeight, new Color(35, 35, 35).getRGB());

                settingComponents.x = x;
                settingComponents.y = y + 20;
                settingComponents.rectWidth = rectWidth;

                settingComponents.render(guiGraphics, mouseX, mouseY, partialTicks);

                typing = typing || settingComponents.typing;
            }
        }

        rectHeight = 20 + settingsHeight;
    }

    private void updateEnableAnimation(float partialTicks, boolean enabled) {
        float targetValue = enabled ? 1.0f : 0.0f;
        float animSpeed = partialTicks / (ANIMATION_DURATION / 1000f);

        if (enableAnimation < targetValue) {
            enableAnimation = Math.min(enableAnimation + animSpeed, 1.0f);
        } else if (enableAnimation > targetValue) {
            enableAnimation = Math.max(enableAnimation - animSpeed, 0.0f);
        }
    }

    /**
     * 更新单次弹跳动画 - 使用平滑的正弦曲线
     */
    private void updateSingleBounceAnimation() {
        if (!isAnimating) {
            bounceAnimation = 0f;
            return;
        }

        // 计算动画已经运行的时间
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - animationStartTime;

        if (elapsedTime >= BOUNCE_DURATION) {
            // 动画结束
            bounceAnimation = 0f;
            isAnimating = false;
            return;
        }

        // 计算动画进度 (0.0 - 1.0)
        float progress = (float) elapsedTime / BOUNCE_DURATION;

        // 使用简单的正弦曲线制作单次弹跳效果
        // 这里使用sin函数的前半部分（0-π）得到一个0→1→0的曲线
        float bounce = (float) Math.sin(Math.PI * progress);

        // 应用曲线
        bounceAnimation = bounce;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        // 检查是否点击了模块标题
        if (isHovering(x, y, rectWidth, 20, mouseX, mouseY) && button == 0) {
            // 切换模块状态
            module.toggle();
            return;
        }

        // 处理设置面板点击
        if (mouseY > y + 20 && mouseY < y + rectHeight) {
            settingComponents.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        settingComponents.mouseReleased(mouseX, mouseY, button);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        settingComponents.keyPressed(keyCode, scanCode, modifiers);
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        settingComponents.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}