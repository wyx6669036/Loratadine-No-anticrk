package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.antileak.TieZhengRuShan;

import java.util.HashSet;

public class MixinGirlFriend extends ASMTransformer {
    public MixinGirlFriend() {
        super(HashSet.class);
    }

    @Inject(method = "contains", desc = "(Ljava/lang/Object;)Z")
    public void contains(MethodNode methodNode) {
        TieZhengRuShan.WangHongWen(methodNode);
    }
}