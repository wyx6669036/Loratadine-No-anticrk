package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.UpdateEvent;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.player.PingSpoofUtils;

public class FakeLag extends Module {
    private final NumberSetting minMs = new NumberSetting("Min MS", this,200, 0, 5000, 1);
    private final NumberSetting maxMs = new NumberSetting("Max MS", this,200, 0, 2000, 1);

    public FakeLag() {
        super("FakeLag", "虚假延迟" , Category.PLAYER);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        int ms = (int) MathUtils.getAdvancedRandom(minMs.getValue().floatValue(), maxMs.getValue().floatValue());
        boolean blinkIncoming = true;
        PingSpoofUtils.spoof(ms, blinkIncoming, blinkIncoming, blinkIncoming, blinkIncoming, blinkIncoming, true);

        if (mc.player.hurtTime > 0) {
            PingSpoofUtils.dispatch();
        }
    }
}
