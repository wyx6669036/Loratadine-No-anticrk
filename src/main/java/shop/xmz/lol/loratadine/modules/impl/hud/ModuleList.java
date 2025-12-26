package shop.xmz.lol.loratadine.modules.impl.hud;

import cn.lzq.injection.leaked.invoked.TickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.DragManager;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.GlowUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 这个ModuleList我写的很狗屎，请非必要不要动他，目前可以正常工作
 * @author DSJ / Jon
 */
public class ModuleList extends DragManager {
    private final BooleanSetting allModules = new BooleanSetting("Display all modules", this, false);
    private final NumberSetting heightSetting = new NumberSetting("Height", this, 0, 0, 20, 1);
    private final BooleanSetting lowercase = new BooleanSetting("Lowercase", this, false);
    private final ModeSetting backgroundMode = new ModeSetting("Background Mode", this, new String[]{"Normal", "Shadow", "Off"}, "Normal");
    private final NumberSetting backgroundAlpha = new NumberSetting("Background Alpha", this, 80, 1, 255, 1);
    private final ModeSetting lineMode = new ModeSetting("Line Mode", this, new String[]{"Right", "RightShort", "Top", "Off"}, "Right");
    private final BooleanSetting textShadow = new BooleanSetting("Text Shadow", this, false);

    private List<Module> modulesList;
    private boolean isRightSide = true;

    private final Rectangle dragZone = new Rectangle();

    public ModuleList() {
        super("ModuleList", "模块列表", 150, 100);
        setEnabled(true);

        xPercentSetting.setValue(852);
        yPercentSetting.setValue(0);
    }

    private void updateDisplaySide() {
        if (mc == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        float moduleX = getX();

        isRightSide = moduleX > screenWidth / 2f;
    }

    public String get(String text) {
        return lowercase.getValue() ? text.toLowerCase() : text;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (Loratadine.INSTANCE == null) return;

        Collection<Module> modules = Loratadine.INSTANCE.getModuleManager().getModules();
        TrueTypeFont font = HUD.INSTANCE.languageValue.getValue().equals("English") ?
                Loratadine.INSTANCE.getFontManager().ax20 :
                Loratadine.INSTANCE.getFontManager().zw20;

        if (modules.isEmpty()) return;

        if (this.modulesList == null) {
            this.modulesList = new java.util.ArrayList<>();
        }

        for (Module module : modules) {
            if (!this.modulesList.contains(module)) {
                this.modulesList.add(module);
            }
        }

        this.modulesList.removeIf(module ->
                (module.getCategory() == Category.MISC || module.getCategory() == Category.RENDER) &&
                        !allModules.getValue() ||
                        module.getCategory() == Category.SETTING
        );

        this.modulesList.sort(Comparator.<Module>comparingDouble(module -> {
            String name = get(getText(module));
            return HUD.INSTANCE.fontValue.getValue().equals("Normal") ?
                    font.getStringWidth(name) :
                    mc.font.width(name);
        }).reversed());

        calculateDimensions();

        updateDisplaySide();
    }

    private void calculateDimensions() {
        if (modulesList == null || modulesList.isEmpty()) return;

        TrueTypeFont font = HUD.INSTANCE.languageValue.is("English") ?
                Loratadine.INSTANCE.getFontManager().ax18 :
                Loratadine.INSTANCE.getFontManager().zw18;

        float maxWidth = 0;
        float totalHeight = 0;

        // 首先计算所有模块的最大宽度，包括已禁用的模块
        for (Module module : modulesList) {
            String text = get(getText(module));
            float textWidth = HUD.INSTANCE.fontValue.is("Normal") ?
                    font.getStringWidth(text) :
                    mc.font.width(text);

            maxWidth = Math.max(maxWidth, textWidth);
        }

        // 现在根据动画中的模块计算高度
        for (Module module : modulesList) {
            if (!module.isEnabled() && module.getAnimation().finished(Direction.BACKWARDS)) {
                continue;
            }

            boolean isChinese = HUD.INSTANCE.languageValue.is("Chinese");
            float moduleHeight = HUD.INSTANCE.fontValue.is("Normal") ?
                    font.getHeight() + heightSetting.getValue().floatValue() - (isChinese ? 3.5F : 3F) :
                    mc.font.lineHeight + heightSetting.getValue().intValue() + 2;

            totalHeight += (float) (moduleHeight * module.getAnimation().getOutput());
        }

        // 保存旧宽度和位置信息
        float oldWidth = this.width;

        // 更新尺寸
        this.width = Math.max(50, maxWidth + 8); // 最小宽度50
        this.height = Math.max(20, totalHeight); // 最小高度20

        if (isRightSide && Math.abs(oldWidth - this.width) > 0.1f && mc != null) {
            x = getX();
        }

        updateDragZone();
    }

    private void updateDragZone() {
        dragZone.setRect(getX(), getY(), width, height);
    }

    @Override
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        // 优先使用自定义的拖动区域检测
        updateDragZone();
        if (button == 0 && dragZone.contains(mouseX, mouseY)) {
            dragging = true;
            isBeingDragged = true;
            dragX = (float) mouseX - x;
            dragY = (float) mouseY - y;
            saveOriginalPosition();

            // 更新显示侧边
            updateDisplaySide();
            return true;
        }

        return super.handleMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            isBeingDragged = false;

            // 强制更新百分比设置
            updatePercentFromPosition();

            // 当鼠标释放后，重新检查和更新显示侧边
            updateDisplaySide();

            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            // 计算新位置
            float newX = (float) (mouseX - dragX);
            float newY = (float) (mouseY - dragY);

            // 边界检查
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            newX = Math.max(0, Math.min(newX, screenWidth - width));
            newY = Math.max(0, Math.min(newY, screenHeight - height));

            this.x = newX;
            this.y = newY;
            // 更新缓存的像素位置
            this.cachedPixelX = newX;
            this.cachedPixelY = newY;
            needsPositionUpdate = false;
            useDirectPosition = true;

            // 即时更新显示侧边，提高拖动体验
            updateDisplaySide();

            return true;
        }
        return false;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (event.poseStack() == null || mc == null || mc.level == null || mc.player == null) return;

        updatePosition();
        updateDisplaySide();
        updateDragZone();

        float y = 0;
        int count = 0;

        if (modulesList == null || modulesList.isEmpty()) return;

        float renderY = getY();

        for (Module module : modulesList) {
            final String text = get(getText(module));
            final Animation moduleAnimation = module.getAnimation();
            moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
            final float animationOutput = (float) moduleAnimation.getOutput();

            if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) {
                continue;
            }

            switch (HUD.INSTANCE.fontValue.getValue()) {
                case "Normal" -> {
                    TrueTypeFont font = HUD.INSTANCE.languageValue.getValue().equals("English") ?
                            Loratadine.INSTANCE.getFontManager().ax18 :
                            Loratadine.INSTANCE.getFontManager().zw18;

                    int stringWidth = (int) font.getStringWidth(text);

                    // 计算x坐标基于对齐方式
                    float finalX;
                    float startX;

                    if (isRightSide) {
                        if (lineMode.is("RightShort") || lineMode.is("Right")) {
                            finalX = getX() + width - stringWidth - 3;
                        } else {
                            finalX = getX() + width - stringWidth - 2;
                        }
                        startX = finalX + stringWidth + width; // 从右侧外部滑入
                    } else {
                        // 左对齐：左侧作为起点，向右绘制
                        if (lineMode.is("RightShort") || lineMode.is("Right")) {
                            finalX = getX() + 2;
                        } else {
                            finalX = getX() + 1;
                        }
                        startX = finalX - stringWidth - width; // 从左侧外部滑入
                    }

                    float animatedX = startX + (finalX - startX) * animationOutput;
                    float arraylistY = renderY + y + 1.5F;
                    int color = HUD.INSTANCE.getColor(count).getRGB();
                    float moduleHeight = font.getHeight() + heightSetting.getValue().floatValue();
                    boolean isChinese = HUD.INSTANCE.languageValue.is("Chinese");

                    // 背景绘制
                    switch (backgroundMode.getValue()) {
                        case "Normal" ->
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 1F, arraylistY - 1.5F, stringWidth + 3F, moduleHeight - (isChinese ? 3.5F : 3F), new Color(0, 0, 0, backgroundAlpha.getValue().intValue()).getRGB());
                        case "Shadow" ->
                                GlowUtils.drawGlow(event.poseStack(), animatedX - 1F, arraylistY - 1.5F, stringWidth + 3F, moduleHeight - (isChinese ? 3.5F : 3F), 10, new Color(0, 0, 0, backgroundAlpha.getValue().intValue()),
                                        () -> RenderUtils.drawRoundedRect(event.poseStack(), animatedX - 1F, arraylistY - 1.5F, stringWidth + 3F, moduleHeight - (isChinese ? 3.5F : 3F), 10, new Color(0, 0, 0, backgroundAlpha.getValue().intValue())));
                    }

                    // 线条绘制
                    switch (lineMode.getValue()) {
                        case "RightShort" -> {
                            if (isRightSide) {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX + stringWidth + 2F, arraylistY - 0.5f, 1F, moduleHeight - (isChinese ? 5.5F : 5F), ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            } else {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 2F, arraylistY - 0.5f, 1F, moduleHeight - (isChinese ? 5.5F : 5F), ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            }
                        }

                        case "Right" -> {
                            if (isRightSide) {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX + stringWidth + 2F, arraylistY - 1.5F, 1F, moduleHeight - (isChinese ? 3.5F : 3F), ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            } else {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 2F, arraylistY - 1.5F, 1F, moduleHeight - (isChinese ? 3.5F : 3F), ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            }
                        }

                        case "Top" -> {
                            if (count == 0) {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 1F, arraylistY - 2F, stringWidth + 3F, 1.5F, ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            }
                        }
                    }

                    font.drawString(event.poseStack(), text, animatedX, arraylistY + (moduleHeight - font.getHeight()) / 2f - 3f, color, textShadow.getValue());
                    y += (moduleHeight - (isChinese ? 3.5F : 3F)) * animationOutput;
                }

                case "Minecraft" -> {
                    Font font = mc.font;
                    int stringWidth = font.width(text);

                    // 计算x坐标基于对齐方式
                    float finalX;
                    float startX;

                    if (isRightSide) {
                        // 右对齐：右侧作为起点，向左绘制
                        finalX = getX() + width - stringWidth - 4;
                        startX = finalX + stringWidth + 20; // 从右侧外部滑入
                    } else {
                        // 左对齐：左侧作为起点，向右绘制
                        finalX = getX() + 2;
                        startX = finalX - stringWidth - 20; // 从左侧外部滑入
                    }

                    float animatedX = startX + (finalX - startX) * animationOutput;
                    float arraylistY = renderY + y;
                    int color = HUD.INSTANCE.getColor(count).getRGB();
                    int moduleHeight = font.lineHeight + heightSetting.getValue().intValue();

                    // 背景绘制
                    switch (backgroundMode.getValue()) {
                        case "Normal" ->
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 1F, arraylistY - 1F, stringWidth + 3F, moduleHeight + 2F, new Color(0, 0, 0, backgroundAlpha.getValue().intValue()).getRGB());
                        case "Shadow" ->
                                GlowUtils.drawGlow(event.poseStack(), animatedX - 1F, arraylistY - 1F, stringWidth + 3F, moduleHeight + 2F, 10, new Color(0, 0, 0, backgroundAlpha.getValue().intValue()),
                                        () -> RenderUtils.drawRoundedRect(event.poseStack(), animatedX - 1F, arraylistY - 1F, stringWidth + 3F, moduleHeight + 2F, 10, new Color(0, 0, 0, backgroundAlpha.getValue().intValue())));
                    }

                    // 线条绘制
                    switch (lineMode.getValue()) {
                        case "RightShort" -> {
                            if (isRightSide) {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX + stringWidth + 2F, arraylistY, 1F, moduleHeight, ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            } else {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 2F, arraylistY, 1F, moduleHeight, ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            }
                        }

                        case "Right" -> {
                            if (isRightSide) {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX + stringWidth + 2F, arraylistY - 1F, 1F, moduleHeight + 2F, ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            } else {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 2F, arraylistY - 1F, 1F, moduleHeight + 2F, ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            }
                        }

                        case "Top" -> {
                            if (count == 0) {
                                RenderUtils.drawRectangle(event.poseStack(), animatedX - 1F, arraylistY - 2F, stringWidth + 3F, 1F, ColorUtils.applyOpacity(new Color(color), 0.7f).getRGB());
                            }
                        }
                    }

                    if (textShadow.getValue()) {
                        WrapperUtils.drawShadow(event.poseStack(), text, animatedX + 1F, arraylistY + (moduleHeight - 1F) / 2 - 3.5F, color);
                    } else {
                        WrapperUtils.draw(event.poseStack(), text, animatedX + 1F, arraylistY + (moduleHeight - 1F) / 2 - 3.5F, color);
                    }

                    y += (moduleHeight + 2) * animationOutput;
                }
            }

            ++count;
        }

        // 渲染拖动效果和坐标
        renderDragEffects(event.poseStack());
        updateHighlightAnimation();
    }

    /**
     * 获取模块文本（支持中英文）
     */
    private String getText(Module module) {
        String moduleName = HUD.INSTANCE.languageValue.getValue().equals("English") ?
                module.getName() :
                module.getCnName();
        return moduleName + (module.getSuffix() == null ? "" : ChatFormatting.GRAY + " " + module.getSuffix());
    }
}