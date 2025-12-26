package shop.xmz.lol.loratadine.antileak;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class TieZhengRuShan {
    public static Object girlFriend1 = new Object();
    public static Object girlFriend2 = new Object();
    public static Object girlFriend3 = new Object();
    public static Object girlFriend4 = new Object();
    public static Object girlFriend5 = new Object();
    public static Object girlFriend6 = new Object();
    public static Object girlFriend7 = new Object();
    public static Object girlFriend8 = new Object();
    public static Object guizuFriend = new Object();

    public static void GirlFriend() {
        girlFriend1 = girlFriend2;
        girlFriend2 = girlFriend3;
        girlFriend3 = girlFriend4;
        girlFriend4 = girlFriend5;
        girlFriend5 = girlFriend6;
        girlFriend6 = girlFriend7;
        girlFriend7 = girlFriend8;

        System.out.println("GirlFriend");
        Object girlFriend = new Object();
        System.out.println(girlFriend);
        System.out.println(girlFriend.hashCode());
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");

        guizuFriend = "LuoDaYou TeYiRenShi.";

        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
    }

    public static void JiangQing() {
        girlFriend1 = girlFriend2;
        girlFriend2 = girlFriend3;
        girlFriend3 = girlFriend4;
        girlFriend4 = girlFriend5;
        girlFriend5 = girlFriend6;
        girlFriend6 = girlFriend7;
        girlFriend7 = girlFriend8;

        System.out.println("GirlFriend");
        Object girlFriend = new Object();
        System.out.println(girlFriend);
        System.out.println(girlFriend.hashCode());
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");

        guizuFriend = "LuoDaYou TeYiRenShi.";

        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
    }

    public static void SiRenBang() {
        girlFriend1 = girlFriend2;
        girlFriend2 = girlFriend3;
        girlFriend3 = girlFriend4;
        girlFriend4 = girlFriend5;
        girlFriend5 = girlFriend6;
        girlFriend6 = girlFriend7;
        girlFriend7 = girlFriend8;

        System.out.println("GirlFriend");
        Object girlFriend = new Object();
        System.out.println(girlFriend);
        System.out.println(girlFriend.hashCode());
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");

        guizuFriend = "LuoDaYou TeYiRenShi.";

        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
    }

    public static void WangHongWen(MethodNode methodNode) {
        // 创建一个新的指令列表
        InsnList list = new InsnList();

        // 创建跳过自定义逻辑和返回 true 的标签
        LabelNode skipLabel = new LabelNode();
        LabelNode returnTrueLabel = new LabelNode();

        // 加载第一个参数，即 `Object o`
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));

        // 检查是否为 String 类型
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, "java/lang/String"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, skipLabel)); // 如果不是 String，跳过

        // 强制转换为 String
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/String"));

        // 加载 "cn.lzq.injection" 字符串常量并检查
        list.add(new LdcInsnNode("cn.lzq.injection"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFNE, returnTrueLabel)); // 如果包含 "cn.lzq.injection"，跳转返回 true

        // 加载 "shop.xmz.lol.loratadine" 字符串常量并检查
        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // 重新加载参数
        list.add(new LdcInsnNode("shop.xmz.lol.loratadine"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFNE, returnTrueLabel)); // 如果包含 "shop.xmz.lol.loratadine"，跳转返回 true

        // 加载 "net.minecraftforge.eventbus" 字符串常量并检查
        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // 重新加载参数
        list.add(new LdcInsnNode("net.minecraftforge.eventbus"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFNE, returnTrueLabel)); // 如果包含 "net.minecraftforge.eventbus"，跳转返回 true

        // 加载 "java.lang.management.ThreadMXBean" 字符串常量并检查
        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // 重新加载参数
        list.add(new LdcInsnNode("java.lang.management.ThreadMXBean"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFNE, returnTrueLabel)); // 如果包含 "java.lang.management.ThreadMXBean"，跳转返回 true

        // 加载 "java.lang.ProcessBuilder" 字符串常量并检查
        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // 重新加载参数
        list.add(new LdcInsnNode("java.lang.ProcessBuilder"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFNE, returnTrueLabel)); // 如果包含 "java.lang.ProcessBuilder"，跳转返回 true

        // 添加跳过自定义逻辑的标签
        list.add(skipLabel);

        // 在这里插入自定义逻辑到方法开头
        methodNode.instructions.insert(list);

        // 创建返回 true 的指令列表
        InsnList returnTrueList = new InsnList();
        returnTrueList.add(returnTrueLabel);
        returnTrueList.add(new InsnNode(Opcodes.ICONST_1)); // 加载常量 true (1)
        returnTrueList.add(new InsnNode(Opcodes.IRETURN));  // 返回 true

        // 在方法结尾插入返回 true 的逻辑
        methodNode.instructions.add(returnTrueList);
    }
}
