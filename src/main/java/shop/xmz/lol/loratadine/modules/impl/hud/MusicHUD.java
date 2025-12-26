package shop.xmz.lol.loratadine.modules.impl.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.DragManager;
import shop.xmz.lol.loratadine.modules.impl.setting.Theme;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.lyrics.LyricsHandler;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.sound.SMTCUtil;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicHUD extends DragManager {
    public static MusicHUD INSTANCE;
    private final ModeSetting mode = new ModeSetting("Mode", this, new String[]{
            "Classic"
    }, "Classic");
    private final BooleanSetting ncmLyrics = new BooleanSetting("163NCM Lyrics", this, false);
    private final BooleanSetting showPlayingIcon = new BooleanSetting("Show Playing Icon", this, false);
    private final ModeSetting fontValue = new ModeSetting("Font", this, new String[]{"Minecraft", "Normal"}, "Normal");
    private final BooleanSetting shadow = new BooleanSetting("Shadow", this, true);
    public final BooleanSetting debug = new BooleanSetting("Debug", this, false);

    private String lastStyle = "";
    private String lastTitle = "";
    private String lastArtist = "";
    private boolean lastShadow = true;
    private boolean lastShowPlayingIcon = true;
    private String lastFontValue = "Normal";

    // 动画相关变量
    private float animatedWidth = 0;
    private float animatedHeight = 0;
    private float targetWidth = 0;
    private float targetHeight = 0;
    private long lastUpdateTime = 0;
    private static final float ANIMATION_DURATION = 200.0f; // 动画持续时间（毫秒）

    // 异步更新
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private static final long UPDATE_INTERVAL_MS = 3000; // 3秒更新一次

    // 颜色
    private static final Color WHITE = Color.WHITE;
    private static final Color LIGHT_GRAY = new Color(200, 200, 200);
    private static final Color CLASSIC_BG = new Color(23, 23, 23);

    public MusicHUD() {
        super("SmtcHUD", "音乐信息", 200, 45);
        INSTANCE = this;

        // 设置默认位置
        xPercentSetting.setValue(6);
        yPercentSetting.setValue(40);
    }

    @Override
    public void onEnable() {
        if (!SMTCUtil.isInitialized()) {
            SMTCUtil.initialize();
        }

        if (!SMTCUtil.hasMediaInfo()) {
            debugMessage("没有媒体信息");
        } else {
            debugMessage("捕捉到媒体信息");
        }
        startAsyncUpdates();
    }

    public void debugMessage(String message) {
        if (debug.getValue()) ClientUtils.log(message);
    }

    public void startAsyncUpdates() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!isEnabled()) return;

            if (isUpdating.compareAndSet(false, true)) {
                try {
                    SMTCUtil.updateMediaInfo();
                } finally {
                    isUpdating.set(false);
                }
            }
        }, 0, UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    // 检查设置是否有变化
    private boolean checkSettingsChanged() {
        boolean shadowChanged = shadow.getValue() != lastShadow;
        boolean showPlayingIconChanged = showPlayingIcon.getValue() != lastShowPlayingIcon;
        boolean fontValueChanged = !fontValue.getValue().equals(lastFontValue);
        boolean styleChanged = !mode.getValue().equals(lastStyle);

        if (shadowChanged || showPlayingIconChanged || fontValueChanged || styleChanged) {
            // 更新缓存的设置值
            lastShadow = shadow.getValue();
            lastShowPlayingIcon = showPlayingIcon.getValue();
            lastFontValue = fontValue.getValue();
            lastStyle = mode.getValue();

            return true;
        }

        return false;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final PoseStack poseStack = event.poseStack();
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        if (poseStack == null || mc == null || mc.level == null || mc.player == null) return;

        // 更新位置
        updatePosition();

        // 如果没有媒体信息，不显示UI
        if (!SMTCUtil.hasMediaInfo()) {
            return;
        }

        // 获取媒体信息
        String title = SMTCUtil.getCurrentTitle();
        String artist = SMTCUtil.getCurrentArtist();
        String lyrics = LyricsHandler.basic;

        boolean isPlaying = SMTCUtil.isPlaying();

        // 检查是否需要重新计算缓存
        boolean settingsChanged = checkSettingsChanged();
        boolean contentChanged = !title.equals(lastTitle) || !artist.equals(lastArtist);

        if (settingsChanged || contentChanged) {
            lastTitle = title;
            lastArtist = artist;
        }

        // 根据样式渲染
        switch (mode.getValue()) {
            case "Classic" -> renderClassicStyle(poseStack, fontManager, getX(), getY(), title, artist, lyrics, isPlaying);
        }

        // 渲染拖动效果
        renderDragEffects(poseStack);
        updateHighlightAnimation();
    }

    // 缓动函数：easeOutQuart - 更平滑的减速效果
    private float easeOutQuart(float t) {
        return 1 - (float)Math.pow(1 - t, 4);
    }

    private void renderClassicStyle(PoseStack poseStack, FontManager fontManager, float x, float y, String title, String artist, String lyrics, boolean isPlaying) {
        // 计算文本
        String artistText = "艺术家: " + artist;
        String lyricsText = "歌词：" + lyrics;

        // 计算文本宽度
        int titleWidth = getTextWidth(fontManager, title, true);
        int artistWidth = getTextWidth(fontManager, artistText, false);
        int lyricsWidth = 0;

        if (ncmLyrics.getValue()) {
            lyricsWidth = getTextWidth(fontManager, lyricsText, true);
        }

        // 计算背景宽度 (取最长文本宽度 + 边距)
        int contentWidth;
        if (ncmLyrics.getValue()) {
            contentWidth = Math.max(titleWidth, Math.max(artistWidth, lyricsWidth));
        } else {
            contentWidth = Math.max(titleWidth, artistWidth);
        }

        int maxWidth = contentWidth + 15; // 左右各加5像素边距，再加5像素给播放图标

        // 如果显示播放图标，增加额外宽度
        if (showPlayingIcon.getValue()) {
            String statusText = isPlaying ? "▶" : "⏸";
            int statusWidth = getTextWidth(fontManager, statusText, false);
            maxWidth += statusWidth + 5; // 图标宽度 + 间距
        }

        // 根据是否显示歌词调整高度
        int height = ncmLyrics.getValue() ? 45 : 30;

        // 设置动画目标值
        targetWidth = maxWidth;
        targetHeight = height;

        // 更新组件大小
        this.width = maxWidth;
        this.height = height;

        // 初始化动画值（如果是第一次）
        if (animatedWidth == 0) {
            animatedWidth = targetWidth;
            animatedHeight = targetHeight;
            lastUpdateTime = System.currentTimeMillis();
        }

        // 更新动画
        long currentTime = System.currentTimeMillis();
        float deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // 计算动画进度
        float progress = Math.min(1.0f, deltaTime / ANIMATION_DURATION);
        progress = easeOutQuart(progress); // 应用缓动函数

        // 更新动画值
        if (Math.abs(targetWidth - animatedWidth) > 0.5f) {
            animatedWidth += (targetWidth - animatedWidth) * progress;
        } else {
            animatedWidth = targetWidth;
        }

        if (Math.abs(targetHeight - animatedHeight) > 0.5f) {
            animatedHeight += (targetHeight - animatedHeight) * progress;
        } else {
            animatedHeight = targetHeight;
        }

        // 绘制背景 (始终显示)
        RenderUtils.drawRoundedRect(poseStack, x, y, (int)animatedWidth, (int)animatedHeight, 0, CLASSIC_BG);
        RenderUtils.drawGradientRectL2R(poseStack, x, y, (int)animatedWidth, 1, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());

        // 绘制标题
        drawText(poseStack, fontManager, title, x + 5, y + 6, WHITE.getRGB(), true, false);

        // 绘制艺术家
        if (fontValue.is("Minecraft")) {
            poseStack.pushPose();
            poseStack.scale(0.8F, 0.8F, 0.8F);
            drawText(poseStack, fontManager, artistText, (x + 5) * (1F / 0.8F), (y + 18) * (1F / 0.8F), LIGHT_GRAY.getRGB(), false, false);
            poseStack.popPose();
        } else {
            drawText(poseStack, fontManager, artistText, (x + 5), (y + 18), LIGHT_GRAY.getRGB(), false, false);
        }

        // 只有在启用ncmLyrics时才绘制歌词
        if (ncmLyrics.getValue()) {
            drawText(poseStack, fontManager, lyricsText, x + 5, y + 31, LIGHT_GRAY.getRGB(), true, false);
        }

        // 绘制播放状态
        if (showPlayingIcon.getValue()) {
            String statusText = fontValue.is("Minecraft") ? (isPlaying ? "▶" : "⏸") : " ";
            Color statusColor = isPlaying ? Theme.INSTANCE.firstColor : LIGHT_GRAY;

            drawText(poseStack, fontManager, statusText, x + (int)animatedWidth - 10, y + 6, statusColor.getRGB(), false, true);
        }
    }

    // 辅助方法：获取文本宽度
    private int getTextWidth(FontManager fontManager, String text, boolean isTitle) {
        if (fontValue.is("Minecraft")) {
            return mc.font.width(text);
        } else {
            return (int) (isTitle ?
                    fontManager.zw20.getStringWidth(text) :
                    fontManager.zw18.getStringWidth(text));
        }
    }

    // 辅助方法：绘制文本
    private void drawText(PoseStack poseStack, FontManager fontManager, String text, float x, float y, int color, boolean isTitle, boolean isIcon) {
        if (fontValue.is("Minecraft")) {
            WrapperUtils.drawShadow(poseStack, text, x, y, color, shadow.getValue());
        } else {
            if (isIcon) {
                (isTitle ? fontManager.icon30 : fontManager.icon18).drawString(poseStack, text, x, y, color, shadow.getValue());
            } else {
                (isTitle ? fontManager.zw20 : fontManager.zw14).drawString(poseStack, text, x, y, color, shadow.getValue());
            }
        }
    }
}