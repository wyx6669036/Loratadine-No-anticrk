package shop.xmz.lol.loratadine.ui.clickguis.compact.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * 最死妈的滚动条
 * @author DSJ_
 */
public class ModuleScroll {
    @Getter
    private float maxScroll = Float.MAX_VALUE;
    @Getter
    @Setter
    private float minScroll = 0;
    @Getter
    private float rawScroll = 0;
    @Getter
    private float scroll = 0;
    private float target = 0;
    private long lastTime;
    @Setter
    private float animationSpeed = 1;

    public ModuleScroll() {
        this.lastTime = System.currentTimeMillis();
    }

    public void onScroll(float delta) {
        // 更新滚动值
        rawScroll += delta;

        // 限制滚动范围
        rawScroll = Math.max(Math.min(minScroll, rawScroll), -maxScroll);

        // 设置目标位置为原始滚动位置
        target = rawScroll;

        // 更新上次调用时间
        lastTime = System.currentTimeMillis();
    }

    public void updateScrollAnimation() {
        float difference = target - scroll;

        if (Math.abs(difference) < 0.1f) {
            scroll = target;
            return;
        }

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000f; // 转换为秒
        lastTime = currentTime;

        float step = difference * Math.min(1f, deltaTime * animationSpeed * 10f);
        scroll += step;
    }

    public void setMaxScroll(float maxScroll) {
        this.maxScroll = maxScroll;

        this.rawScroll = Math.max(Math.min(minScroll, rawScroll), -maxScroll);
        this.target = rawScroll;
    }
}