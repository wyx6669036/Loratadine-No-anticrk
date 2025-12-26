package shop.xmz.lol.loratadine.utils.render.blur;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

public class BlurRenderer {
    public static PostChain blurShader;

    // 加载模糊
    public static void loadBlurShader() {
        if (mc.player == null || mc.level == null) return;

        if (blurShader != null) {
            blurShader.close(); // 防止重复加载
        }

        try {
            blurShader = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), new ResourceLocation("minecraft:shaders/post/blur.json"));
            blurShader.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        } catch (Exception e) {
            e.printStackTrace();
            blurShader = null;
        }
    }

    // 取消加载模糊
    public static void unloadShader() {
        if (blurShader != null) {
            blurShader.close();
            blurShader = null;
        }
    }
}

