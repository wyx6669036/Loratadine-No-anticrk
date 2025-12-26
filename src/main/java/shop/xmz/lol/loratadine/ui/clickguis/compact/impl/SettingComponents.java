package shop.xmz.lol.loratadine.ui.clickguis.compact.impl;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.Setting;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;
import java.util.HashMap;

/**
 * SettingComponents
 * @author DSJ_
 */
public class SettingComponents implements Wrapper {
    private final Module module;
    public float size;
    public Color actualColor;
    public float x, y, rectWidth;
    public Setting draggingNumber;
    public boolean typing;

    // 存储每个滑块的进度动画
    private final HashMap<NumberSetting, Float> sliderPositions = new HashMap<>();

    // 添加滑块动画所需的额外变量
    private final HashMap<NumberSetting, Float> sliderTargetPositions = new HashMap<>(); // 目标位置
    private final HashMap<NumberSetting, Float> sliderLastClickTime = new HashMap<>(); // 上次点击时间
    private final HashMap<NumberSetting, float[]> sliderVelocity = new HashMap<>(); // 滑动速度

    // 动画调整参数
    private static final float SLIDER_ANIMATION_SPEED = 5.0f; // 基础动画速度
    private static final float SLIDER_SPRING_STRENGTH = 8.0f; // 弹簧强度
    private static final float SLIDER_DAMPING = 0.75f; // 阻尼系数

    // 存储每个模式设置的展开状态
    private final HashMap<ModeSetting, Boolean> modeExpanded = new HashMap<>();

    // 存储模式展开动画的进度 (0.0-1.0)
    private final HashMap<ModeSetting, Float> modeExpandAnimation = new HashMap<>();

    // 存储模式选项的悬停动画
    private final HashMap<String, HashMap<String, Float>> modeOptionHoverAnimation = new HashMap<>();

    private final HashMap<BooleanSetting, Float> booleanAnimations = new HashMap<>();
    private static final float BOOLEAN_ANIM_SPEED = 0.15f;

    // 设置项高度
    private static final float SETTING_HEIGHT = 16f;

    // 固定滑块位置和尺寸
    private static final float SLIDER_WIDTH = 50f;
    private static final float SLIDER_HEIGHT = 2f;
    private static final float VALUE_SPACING = 7f; // 值与屏幕右侧的间距

    // 布尔值和模式选择器背景
    private static final float BOOL_RECT_WIDTH = 10f;
    private static final float BOOL_RECT_HEIGHT = 10f;
    private static final float MODE_RECT_WIDTH = 70f;
    private static final float MODE_RECT_HEIGHT = 10f;

    // 模式选项高度
    private static final float MODE_OPTION_HEIGHT = 12f;

    // 数值背景 - 使用固定宽度
    private static final float NUMBER_RECT_WIDTH = 30f;
    private static final float NUMBER_RECT_HEIGHT = 10f;

    public SettingComponents(Module module) {
        this.module = module;
    }

    public void initGui() {
        // 初始化滑块位置
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                booleanAnimations.put((BooleanSetting)setting, ((BooleanSetting)setting).getValue() ? 1.0f : 0.0f);
            }

            if (setting instanceof NumberSetting) {
                NumberSetting numSetting = (NumberSetting) setting;
                float progress = (numSetting.getValue().floatValue() - numSetting.getMinValue().floatValue())
                        / (numSetting.getMaxValue().floatValue() - numSetting.getMinValue().floatValue());
                sliderPositions.put(numSetting, progress);
                sliderTargetPositions.put(numSetting, progress);
                sliderVelocity.put(numSetting, new float[]{0.0f});
            }

            // 初始化模式展开状态
            if (setting instanceof ModeSetting) {
                ModeSetting modeSetting = (ModeSetting) setting;
                // 默认所有模式都是折叠的
                if (!modeExpanded.containsKey(modeSetting)) {
                    modeExpanded.put(modeSetting, false);
                    modeExpandAnimation.put(modeSetting, 0.0f);
                }

                // 为每个模式的选项初始化悬停动画
                String modeId = modeSetting.getName();
                HashMap<String, Float> optionMap = modeOptionHoverAnimation.computeIfAbsent(modeId, k -> new HashMap<>());

                for (String mode : modeSetting.getValues()) {
                    // 初始化或保持现有的动画进度
                    if (!optionMap.containsKey(mode)) {
                        optionMap.put(mode, 0.0f);
                    }
                }
            }
        }
    }

    /**
     * 计算所有设置的总高度
     */
    public float calculateHeight() {
        float totalHeight = 0;

        for (Setting<?> setting : module.getSettings()) {
            if (!setting.shouldRender()) continue;  // 跳过不应该渲染的设置

            totalHeight += SETTING_HEIGHT; // 基础高度

            // 为展开的模式设置添加额外高度
            if (setting instanceof ModeSetting) {
                ModeSetting modeSetting = (ModeSetting) setting;
                Boolean expanded = modeExpanded.get(modeSetting);
                Float animation = modeExpandAnimation.get(modeSetting);

                if (expanded != null && animation != null && animation > 0) {
                    int options = modeSetting.getValues().length - 1; // 减去当前选中的
                    float expandedHeight = options * MODE_OPTION_HEIGHT * animation;
                    totalHeight += expandedHeight;
                }
            }
        }

        return totalHeight;
    }

    /**
     * 更新滑块的弹性动画
     */
    private float updateSliderAnimation(NumberSetting setting, float targetPosition, float partialTicks) {
        // 确保初始化
        if (!sliderPositions.containsKey(setting)) {
            sliderPositions.put(setting, targetPosition);
        }
        if (!sliderTargetPositions.containsKey(setting)) {
            sliderTargetPositions.put(setting, targetPosition);
        }
        if (!sliderVelocity.containsKey(setting)) {
            sliderVelocity.put(setting, new float[]{0.0f});
        }

        // 更新目标位置
        sliderTargetPositions.put(setting, targetPosition);

        // 获取当前位置和速度
        float currentPosition = sliderPositions.get(setting);
        float[] velocity = sliderVelocity.get(setting);

        // 如果正在拖动，直接使用目标位置
        if (draggingNumber == setting) {
            sliderPositions.put(setting, targetPosition);
            velocity[0] = 0.0f;
            return targetPosition;
        }

        // 弹簧物理模型计算
        float deltaTime = partialTicks * 0.05f; // 时间增量
        float distance = targetPosition - currentPosition; // 与目标的距离

        // 计算弹簧力
        float springForce = distance * SLIDER_SPRING_STRENGTH;

        // 应用弹簧力和阻尼
        velocity[0] += springForce * deltaTime;
        velocity[0] *= Math.pow(SLIDER_DAMPING, deltaTime * 60.0);

        // 更新位置
        float newPosition = currentPosition + velocity[0] * deltaTime * SLIDER_ANIMATION_SPEED;

        // 如果接近目标且速度很小，直接设为目标位置
        if (Math.abs(distance) < 0.001f && Math.abs(velocity[0]) < 0.001f) {
            newPosition = targetPosition;
            velocity[0] = 0.0f;
        }

        // 储存结果
        sliderPositions.put(setting, newPosition);

        return newPosition;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        typing = false;
        float yOffset = 0;

        // 获取字体
        boolean isEnglish = ((HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))
                .languageValue.getValue().equals("English");
        TrueTypeFont font = isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity16 :
                Loratadine.INSTANCE.getFontManager().zw16;
        TrueTypeFont smallFont = isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity14 :
                Loratadine.INSTANCE.getFontManager().zw14;

        // 颜色设置
        Color accentColor = actualColor != null ? actualColor : HUD.INSTANCE.getColor(0);
        Color disabledColor = new Color(64, 68, 75);
        Color darkBgColor = new Color(45, 45, 45);
        Color settingBgColor = new Color(80, 80, 80);

        for (Setting<?> setting : module.getSettings()) {
            if (!setting.shouldRender()) continue;

            float settingY = y + yOffset;
            float middleY = settingY + SETTING_HEIGHT / 2f;
            float settingHeight = SETTING_HEIGHT;
            float extraHeight = 0;

            // 如果是模式设置且有展开动画，计算额外高度
            if (setting instanceof ModeSetting) {
                ModeSetting modeSetting = (ModeSetting) setting;
                Boolean expanded = modeExpanded.get(modeSetting);
                Float expandAnim = modeExpandAnimation.get(modeSetting);

                if (expanded != null && expandAnim != null && expandAnim > 0) {
                    int optionsCount = 0;
                    for (String mode : modeSetting.getValues()) {
                        if (!mode.equals(modeSetting.getValue())) {
                            optionsCount++;
                        }
                    }
                    extraHeight = optionsCount * MODE_OPTION_HEIGHT * expandAnim;
                }
            }

            // 绘制设置背景 - 包含可能的展开部分
            RenderUtils.drawRectangle(guiGraphics.pose(), x, settingY, rectWidth, settingHeight + extraHeight,
                    settingBgColor.getRGB());

            // 绘制设置名称 - 使用设置名称而不是模块名称
            font.drawString(guiGraphics.pose(), setting.getName(), x + 5, middleY - font.getHeight() / 2f, -1);

            // 根据设置项类型渲染控件
            if (setting instanceof BooleanSetting) {
                renderBooleanSetting(guiGraphics, (BooleanSetting) setting, settingY, middleY, partialTicks, smallFont, accentColor, disabledColor, darkBgColor);
            } else if (setting instanceof NumberSetting) {
                renderNumberSetting(guiGraphics, (NumberSetting) setting, settingY, middleY, mouseX, partialTicks, smallFont, accentColor, disabledColor, darkBgColor);
            } else if (setting instanceof ModeSetting) {
                extraHeight = renderModeSetting(guiGraphics, (ModeSetting) setting, settingY, middleY, mouseX, mouseY, partialTicks, smallFont, accentColor, disabledColor, darkBgColor);
            }

            yOffset += SETTING_HEIGHT + extraHeight;
        }

        size = yOffset;
    }

    private void renderBooleanSetting(GuiGraphics guiGraphics, BooleanSetting setting, float settingY, float middleY,
                                      float partialTicks, TrueTypeFont font, Color accentColor, Color disabledColor, Color darkBgColor) {
        Float animationProgress = booleanAnimations.get(setting);
        if (animationProgress == null) {
            animationProgress = setting.getValue() ? 1.0f : 0.0f;
            booleanAnimations.put(setting, animationProgress);
        }

        float targetProgress = setting.getValue() ? 1.0f : 0.0f;
        if (Math.abs(animationProgress - targetProgress) > 0.001f) {
            if (animationProgress < targetProgress) {
                animationProgress = Math.min(targetProgress, animationProgress + BOOLEAN_ANIM_SPEED * partialTicks);
            } else {
                animationProgress = Math.max(targetProgress, animationProgress - BOOLEAN_ANIM_SPEED * partialTicks);
            }
            booleanAnimations.put(setting, animationProgress);
        }

        boolean enabled = setting.getValue();
        Color valueColor = enabled ? accentColor : Color.RED;

        Color borderColor = ColorUtils.interpolateColorC(disabledColor, valueColor, animationProgress);

        float boolRectX = x + rectWidth - (BOOL_RECT_WIDTH + VALUE_SPACING);
        float boolRectY = middleY - BOOL_RECT_HEIGHT/2;

        RenderUtils.drawRectangle(guiGraphics.pose(), boolRectX, boolRectY, BOOL_RECT_WIDTH, BOOL_RECT_HEIGHT,
                borderColor.getRGB());

        RenderUtils.drawRectangle(guiGraphics.pose(), boolRectX + 1, boolRectY + 1, BOOL_RECT_WIDTH - 2, BOOL_RECT_HEIGHT - 2,
                darkBgColor.getRGB());

        if (animationProgress > 0) {
            int alpha = (int)(255 * animationProgress);
            Color fillColor = new Color(valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), alpha);
            RenderUtils.drawRectangle(guiGraphics.pose(), boolRectX + 2, boolRectY + 2,
                    BOOL_RECT_WIDTH - 4, BOOL_RECT_HEIGHT - 4, fillColor.getRGB());
        }
    }

    private void renderNumberSetting(GuiGraphics guiGraphics, NumberSetting setting, float settingY, float middleY,
                                     int mouseX, float partialTicks, TrueTypeFont font, Color accentColor, Color disabledColor, Color darkBgColor) {
        // 当前值显示
        float currentValue = setting.getValue().floatValue();
        String valueText = currentValue == (int) currentValue ?
                String.valueOf((int) currentValue) :
                String.format("%.2f", currentValue);

        // 使用固定宽度矩形 - 不随数字变化
        float numberRectX = x + rectWidth - (NUMBER_RECT_WIDTH + VALUE_SPACING);
        float numberRectY = middleY - NUMBER_RECT_HEIGHT/2;

        // 绘制矩形背景
        RenderUtils.drawRectangle(guiGraphics.pose(), numberRectX, numberRectY, NUMBER_RECT_WIDTH, NUMBER_RECT_HEIGHT,
                disabledColor.getRGB());

        // 绘制值 - 在矩形中居中
        font.drawCenteredString(guiGraphics.pose(), valueText,
                numberRectX + NUMBER_RECT_WIDTH/2,
                middleY - font.getHeight() / 2f + 1, -1);

        // 固定滑块位置 - 放在数值框左侧
        final float sliderX = numberRectX - SLIDER_WIDTH - 5;
        final float sliderY = middleY - SLIDER_HEIGHT / 2f;

        // 计算目标滑块位置
        float targetProgress = (setting.getValue().floatValue() - setting.getMinValue().floatValue())
                / (setting.getMaxValue().floatValue() - setting.getMinValue().floatValue());

        // 使用增强的弹性动画更新滑块位置
        float currentPosition = updateSliderAnimation(setting, targetProgress, partialTicks);

        // 计算滑块填充宽度
        float sliderFillWidth = SLIDER_WIDTH * currentPosition;

        // 绘制滑块背景
        RenderUtils.drawRectangle(guiGraphics.pose(), sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT, disabledColor.getRGB());

        // 绘制滑块填充
        if (sliderFillWidth > 0) {
            RenderUtils.drawRectangle(guiGraphics.pose(), sliderX, sliderY, sliderFillWidth, SLIDER_HEIGHT, accentColor.getRGB());
        }

        // 绘制滑块指示器 - 固定高度和宽度
        final float indicatorWidth = 2;
        final float indicatorHeight = 6f;
        float indicatorX = sliderX + sliderFillWidth - (indicatorWidth / 2);
        float indicatorY = sliderY + (SLIDER_HEIGHT / 2f) - (indicatorHeight / 2f);

        RenderUtils.drawRectangle(guiGraphics.pose(), indicatorX, indicatorY, indicatorWidth, indicatorHeight, Color.WHITE.getRGB());

        // 如果正在拖动，更新设置值
        if (draggingNumber == setting) {
            float percent = Math.min(1, Math.max(0, (mouseX - sliderX) / SLIDER_WIDTH));

            // 更新目标位置，但让动画系统处理实际的平滑过渡
            sliderTargetPositions.put(setting, percent);
            sliderLastClickTime.put(setting, (float)System.currentTimeMillis() / 1000.0f);

            double newValue = (percent * (setting.getMaxValue().doubleValue() - setting.getMinValue().doubleValue())) + setting.getMinValue().doubleValue();

            // 使用优化过的setValue方法，它会处理step逻辑
            setting.setValue(newValue);
        }
    }

    private float renderModeSetting(GuiGraphics guiGraphics, ModeSetting setting, float settingY, float middleY,
                                    int mouseX, int mouseY, float partialTicks, TrueTypeFont font, Color accentColor, Color disabledColor, Color darkBgColor) {
        // 获取当前模式值
        String currentMode = setting.getValue();
        String[] modes = setting.getValues();
        String modeId = setting.getName(); // 用作唯一标识

        // 获取或创建此模式的悬停动画映射
        HashMap<String, Float> hoverAnimMap = modeOptionHoverAnimation.computeIfAbsent(modeId, k -> new HashMap<>());

        // 确保所有模式选项都有动画值
        for (String mode : modes) {
            if (!hoverAnimMap.containsKey(mode)) {
                hoverAnimMap.put(mode, 0.0f);
            }
        }

        // 获取展开状态
        Boolean expanded = modeExpanded.get(setting);
        if (expanded == null) {
            expanded = false;
            modeExpanded.put(setting, false);
        }

        // 获取展开动画状态
        Float expandAnim = modeExpandAnimation.get(setting);
        if (expandAnim == null) {
            expandAnim = 0.0f;
            modeExpandAnimation.put(setting, 0.0f);
        }

        // 更新展开动画
        float targetAnim = expanded ? 1.0f : 0.0f;
        if (Math.abs(expandAnim - targetAnim) > 0.001f) {
            expandAnim += (targetAnim - expandAnim) * 0.5f * partialTicks;
            modeExpandAnimation.put(setting, expandAnim);
        }

        // 绘制模式背景矩形
        float modeWidth = Math.max(MODE_RECT_WIDTH, font.getStringWidth(currentMode) + 10);
        float modeRectX = x + rectWidth - (modeWidth + VALUE_SPACING);
        float modeRectY = middleY - MODE_RECT_HEIGHT/2;

        // 绘制模式选择框背景
        RenderUtils.drawRectangle(guiGraphics.pose(), modeRectX, modeRectY, modeWidth, MODE_RECT_HEIGHT,
                disabledColor.getRGB());

        // 绘制当前值
        font.drawCenteredString(guiGraphics.pose(), currentMode,
                modeRectX + modeWidth/2,
                middleY - font.getHeight() / 2f + 0.5F, -1);

        // 绘制动画箭头
        drawAnimatedArrow(guiGraphics, modeRectX + modeWidth - 5, middleY, 4, expanded, expandAnim, -1);

        // 额外高度（用于展开选项）
        float extraHeight = 0;

        // 如果有展开动画，绘制选项
        if (expandAnim > 0) {
            float dropdownY = settingY + SETTING_HEIGHT;
            int optionsCount = 0;

            // 首先在整个展开区域绘制统一的背景，与主设置背景保持一致
            // 计算总展开高度
            int totalOptions = 0;
            for (String mode : modes) {
                if (!mode.equals(currentMode)) totalOptions++;
            }

            // 添加底部额外填充
            float paddingBottom = 2.0f;
            float totalExpandHeight = (totalOptions * MODE_OPTION_HEIGHT + paddingBottom) * expandAnim;

            if (totalExpandHeight > 0) {
                Color settingBgColor = new Color(80, 80, 80);
                RenderUtils.drawRectangle(guiGraphics.pose(), x, dropdownY, rectWidth, totalExpandHeight,
                        settingBgColor.getRGB());
            }

            // 然后为每个选项单独绘制背景和效果
            for (String mode : modes) {
                if (mode.equals(currentMode)) continue; // 跳过当前选中的模式

                float optionHeight = MODE_OPTION_HEIGHT * expandAnim;
                if (optionHeight <= 0) continue;

                // 计算选项Y位置，使用动画进度让文本滑动展开而不是重叠
                float optionY = dropdownY + (optionsCount * MODE_OPTION_HEIGHT * expandAnim);

                // 计算选项是否被hover
                boolean hovering = mouseX >= modeRectX && mouseX <= modeRectX + modeWidth &&
                        mouseY >= optionY && mouseY <= optionY + optionHeight;

                // 更新悬停动画 - 平滑过渡
                float hoverAnim = hoverAnimMap.getOrDefault(mode, 0.0f);
                float targetHoverAnim = hovering ? 1.0f : 0.0f;

                if (Math.abs(hoverAnim - targetHoverAnim) > 0.001f) {
                    // 使用与展开动画相似的平滑动画效果
                    hoverAnim += (targetHoverAnim - hoverAnim) * 0.3f * partialTicks;
                    hoverAnimMap.put(mode, hoverAnim);
                }

                // 绘制选项背景 - 使用动画混合颜色
                Color normalColor = new Color(50, 50, 50);
                Color hoverColor = ColorUtils.darker(accentColor, 0.8f);

                // 根据悬停动画进度混合颜色
                Color optionColor = ColorUtils.interpolateColorC(normalColor, hoverColor, hoverAnim);

                // 为每个选项绘制背景
                float optionPadding = 1.0f;
                RenderUtils.drawRectangle(guiGraphics.pose(),
                        modeRectX - optionPadding,
                        optionY,
                        modeWidth + (optionPadding * 2),
                        optionHeight,
                        ColorUtils.applyOpacity(optionColor.getRGB(), expandAnim));

                // 计算文本透明度 - 使文本随展开动画平滑显示
                int textColor = ColorUtils.applyOpacity(-1, expandAnim);

                // 绘制选项文本
                font.drawCenteredString(guiGraphics.pose(), mode,
                        modeRectX + modeWidth/2,
                        optionY + optionHeight/2 - (float) font.getHeight() / 2 + 0.5F,
                        textColor);

                optionsCount++;
                extraHeight += optionHeight;
            }

            // 添加底部额外填充到额外高度中
            extraHeight += paddingBottom * expandAnim;
        }

        return extraHeight;
    }

    private void drawAnimatedArrow(GuiGraphics guiGraphics, float x, float y, float size, boolean expanded, float animationProgress, int color) {
        float rotation = expanded ? (120 * animationProgress) : (180 - 180 * animationProgress);

        String arrowChar = "▼";
        TrueTypeFont font = Loratadine.INSTANCE.getFontManager().zw12;

        float charWidth = font.getStringWidth(arrowChar);
        float charHeight = font.getHeight();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotation((float) Math.toRadians(rotation)));
        float xOffset = -charWidth / 2;
        float yOffset = -charHeight / 2 + 1; // 可能需要微调这个值以获得精确的垂直居中
        font.drawString(guiGraphics.pose(), arrowChar, xOffset, yOffset, color);
        guiGraphics.pose().popPose();
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        float yOffset = 0;

        for (Setting<?> setting : module.getSettings()) {
            if (!setting.shouldRender()) continue;  // 跳过不应该渲染的设置

            float settingY = y + yOffset;
            float settingHeight = SETTING_HEIGHT;
            float middleY = settingY + SETTING_HEIGHT / 2f;

            // 计算额外高度（如果有展开的模式）
            float extraHeight = 0;
            if (setting instanceof ModeSetting) {
                ModeSetting modeSetting = (ModeSetting) setting;
                Boolean expanded = modeExpanded.get(modeSetting);
                Float expandAnim = modeExpandAnimation.get(modeSetting);

                if (expanded != null && expandAnim != null && expandAnim > 0) {
                    int optionsCount = 0;
                    for (String mode : modeSetting.getValues()) {
                        if (!mode.equals(modeSetting.getValue())) {
                            optionsCount++;
                        }
                    }
                    extraHeight = optionsCount * MODE_OPTION_HEIGHT * expandAnim;
                }
            }

            // 先处理基本设置行
            if (mouseY >= settingY && mouseY <= settingY + SETTING_HEIGHT && mouseX >= x && mouseX <= x + rectWidth) {
                if (setting instanceof BooleanSetting) {
                    BooleanSetting boolSetting = (BooleanSetting) setting;
                    // 计算布尔值矩形的位置
                    float boolRectX = x + rectWidth - (BOOL_RECT_WIDTH + VALUE_SPACING);
                    float boolRectY = middleY - BOOL_RECT_HEIGHT/2;

                    // 检查是否点击了布尔值矩形
                    if (mouseX >= boolRectX && mouseX <= boolRectX + BOOL_RECT_WIDTH &&
                            mouseY >= boolRectY && mouseY <= boolRectY + BOOL_RECT_HEIGHT) {
                        boolSetting.toggle();
                        return;
                    }
                } else if (setting instanceof NumberSetting) {
                    NumberSetting numSetting = (NumberSetting) setting;

                    // 固定位置计算 - 使用与渲染相同的固定位置
                    float numberRectX = x + rectWidth - (NUMBER_RECT_WIDTH + VALUE_SPACING);

                    // 计算滑块位置 - 放在数值框左侧
                    final float sliderX = numberRectX - SLIDER_WIDTH - 5;
                    final float sliderY = middleY - SLIDER_HEIGHT / 2f;

                    // 检查是否点击了滑块 - 扩大点击区域，使滑块更容易点击
                    if (mouseX >= sliderX - 2 && mouseX <= sliderX + SLIDER_WIDTH + 2 &&
                            mouseY >= sliderY - 6 && mouseY <= sliderY + SLIDER_HEIGHT + 6) {
                        draggingNumber = numSetting;

                        // 更新时间戳和速度
                        sliderLastClickTime.put(numSetting, (float)System.currentTimeMillis() / 1000.0f);
                        sliderVelocity.put(numSetting, new float[]{0.0f});

                        // 立即更新数值
                        float percent = (float)((mouseX - sliderX) / SLIDER_WIDTH);
                        percent = Math.min(1, Math.max(0, percent)); // 确保百分比在0-1范围内

                        // 更新目标位置
                        sliderTargetPositions.put(numSetting, percent);

                        double newValue = (percent * (numSetting.getMaxValue().doubleValue() - numSetting.getMinValue().doubleValue())) + numSetting.getMinValue().doubleValue();

                        // 使用优化过的setValue
                        numSetting.setValue(newValue);
                        return;
                    }
                } else if (setting instanceof ModeSetting) {
                    ModeSetting modeSetting = (ModeSetting) setting;

                    // 获取当前模式值
                    String currentMode = modeSetting.getValue();

                    boolean isEnglish = ((HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))
                            .languageValue.getValue().equals("English");

                    TrueTypeFont smallFont = isEnglish ?
                            Loratadine.INSTANCE.getFontManager().tenacity14 :
                            Loratadine.INSTANCE.getFontManager().zw14;

                    // 计算模式矩形位置
                    float modeWidth = Math.max(MODE_RECT_WIDTH, smallFont.getStringWidth(currentMode) + 10);
                    float modeRectX = x + rectWidth - (modeWidth + VALUE_SPACING);
                    float modeRectY = middleY - MODE_RECT_HEIGHT/2;

                    // 检查是否点击了模式矩形
                    if (mouseX >= modeRectX && mouseX <= modeRectX + modeWidth &&
                            mouseY >= modeRectY && mouseY <= modeRectY + MODE_RECT_HEIGHT) {

                        // 改为只有右键才能展开/折叠菜单
                        if (button == 1) { // 右键：展开/折叠
                            // 切换展开状态
                            modeExpanded.put(modeSetting, !modeExpanded.getOrDefault(modeSetting, false));
                        }

                        // 移除左键循环切换功能，只能通过展开菜单选择
                        return;
                    }
                }
            }

            // 如果是模式设置并且展开了，检查是否点击了下拉选项
            if (setting instanceof ModeSetting) {
                ModeSetting modeSetting = (ModeSetting) setting;

                // 检查是否处于展开状态
                Boolean expanded = modeExpanded.get(modeSetting);
                Float expandAnim = modeExpandAnimation.get(modeSetting);

                if (expanded != null && expanded && expandAnim != null && expandAnim > 0.5f) { // 只有展开动画超过一半时才能点击
                    String currentMode = modeSetting.getValue();
                    String[] modes = modeSetting.getValues();

                    boolean isEnglish = ((HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))
                            .languageValue.getValue().equals("English");

                    TrueTypeFont smallFont = isEnglish ?
                            Loratadine.INSTANCE.getFontManager().tenacity14 :
                            Loratadine.INSTANCE.getFontManager().zw14;

                    // 计算下拉区域的位置
                    float modeWidth = Math.max(MODE_RECT_WIDTH, smallFont.getStringWidth(currentMode) + 10);
                    float modeRectX = x + rectWidth - (modeWidth + VALUE_SPACING);
                    float dropdownY = settingY + SETTING_HEIGHT;

                    int optionIndex = 0;
                    for (String mode : modes) {
                        if (mode.equals(currentMode)) continue; // 跳过当前选中的模式

                        // 使用与渲染相同的计算方式，确保点击区域匹配
                        float optionHeight = MODE_OPTION_HEIGHT * expandAnim;
                        float optionY = dropdownY + (optionIndex * MODE_OPTION_HEIGHT * expandAnim);

                        // 检查是否点击了这个选项
                        if (mouseX >= modeRectX && mouseX <= modeRectX + modeWidth &&
                                mouseY >= optionY && mouseY <= optionY + optionHeight && button == 0) { // 左键点击选项

                            // 设置新的选中值
                            modeSetting.setValue(mode);

                            // 选择后自动折叠下拉框
                            modeExpanded.put(modeSetting, false);
                            return;
                        }

                        optionIndex++;
                    }
                }
            }

            // 更新Y偏移，为下一个设置准备
            yOffset += SETTING_HEIGHT + extraHeight;
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingNumber != null && button == 0) {
            // 添加释放时的弹跳效果 - 根据拖动持续时间计算初始速度
            NumberSetting setting = (NumberSetting) draggingNumber;
            float currentTime = (float)System.currentTimeMillis() / 1000.0f;
            float lastTime = sliderLastClickTime.getOrDefault(setting, currentTime);

            // 如果拖动时间很短，添加些弹性效果
            if (currentTime - lastTime < 0.3f) {
                float[] velocity = sliderVelocity.getOrDefault(setting, new float[]{0.0f});
                // 添加一个小的弹跳速度
                velocity[0] = 0.05f * (currentTime - lastTime > 0 ? 1 : -1);
                sliderVelocity.put(setting, velocity);
            }

            draggingNumber = null;
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        // 键盘事件处理
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        // 滚动事件处理
    }
}