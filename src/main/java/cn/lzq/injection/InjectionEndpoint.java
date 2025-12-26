package cn.lzq.injection;

import cn.lzq.injection.leaked.mixin.MixinLoader;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.antileak.ZenlessZone0;

public class InjectionEndpoint {
    public static void load() {
        ZenlessZone0.a(null);
        init();
    }

    private static void init() {
        MixinLoader.init();
        Loratadine.isDllInject = true;
        Loratadine.INSTANCE.init();
    }
}
