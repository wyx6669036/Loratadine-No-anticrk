package shop.xmz.lol.loratadine.modules.impl.player;

import net.minecraft.world.item.BlockItem;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import cn.lzq.injection.leaked.invoked.TickEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

public class FastPlace extends Module {
    private final BooleanSetting onlyNoStateScaffold = new BooleanSetting("OnlyNoStateScaffold",this,false);
    private int originalRightClickDelay;

    public FastPlace() {
        super("FastPlace", "快速放置" ,Category.PLAYER);
        this.setEnabled(true);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.level == null) return;

        originalRightClickDelay = WrapperUtils.getRightClickDelay();
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.level == null) return;

        WrapperUtils.setRightClickDelay(originalRightClickDelay);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null || !isEnabled()) return;

        if (onlyNoStateScaffold.getValue() && Loratadine.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) return;

        if (mc.player.getMainHandItem().getItem() instanceof BlockItem || mc.player.getOffhandItem().getItem() instanceof BlockItem) {
            if (WrapperUtils.getRightClickDelay() != 0) WrapperUtils.setRightClickDelay(0);
        }
    }
}
