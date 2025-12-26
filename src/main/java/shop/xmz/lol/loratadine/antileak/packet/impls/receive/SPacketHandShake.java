package shop.xmz.lol.loratadine.antileak.packet.impls.receive;

import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.antileak.Fucker;
import shop.xmz.lol.loratadine.antileak.packet.Packet;
import shop.xmz.lol.loratadine.antileak.user.UserType;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import io.netty.channel.Channel;
import shop.xmz.lol.loratadine.utils.WindowsUtil;
import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;

public class SPacketHandShake extends Packet {
    public SPacketHandShake(Channel channel) {
        super(channel);
    }

    @Override
    public void read(PacketWrapper wrapper) throws Throwable {
        final int state = wrapper.readInt();

//        switch (state) {
//            case -1: // 传入参数错误
//                WindowsUtil.error("传入参数错误!");
//                Fucker.login = false;
//                Loratadine.INSTANCE.getModuleManager().modules.clear();
//                Loratadine.INSTANCE.setCommandManager(null);
//                Loratadine.INSTANCE.setMinecraft(null);
//                Loratadine.INSTANCE.setModuleManager(null);
//                Loratadine.INSTANCE.setEventManager(null);
//                UnsafeUtils.freeMemory();
//                System.exit(0);
//            break;
//
//            case 1: // 登录成功
//                Fucker.userData = wrapper.readUser();
//
//                final String name = Fucker.userData.username;
//                final boolean isBeta = Fucker.userData.isBeta;
//                final UserType userType = UserType.getFromName(Fucker.userData.userLimits.tag);
//
//                Fucker.login = true;
//                Fucker.userType = userType;
//                Fucker.name = name;
//                Fucker.isBeta = isBeta;
//
//                if (!Fucker.firstLogin) return;
//                Fucker.firstLogin = false; // 破处
//
//                Loratadine.INSTANCE.init();
//                if (isBeta) {
//                    WindowsUtil.success("验证成功! 尊敬的Beta用户 -> " + name);
//                } else {
//                    WindowsUtil.success("验证成功! 用户 -> " + name);
//                }
//                break;
//
//            case 2: // 账密错误
//                WindowsUtil.error("账密错误!");
//                Fucker.login = false;
//                Loratadine.INSTANCE.getModuleManager().modules.clear();
//                Loratadine.INSTANCE.setCommandManager(null);
//                Loratadine.INSTANCE.setMinecraft(null);
//                Loratadine.INSTANCE.setModuleManager(null);
//                Loratadine.INSTANCE.setEventManager(null);
//                UnsafeUtils.freeMemory();
//                System.exit(0);
//                break;
//
//            case 3: // UUID错误
//                WindowsUtil.error("UUID错误-验证失败!");
//                Fucker.login = false;
//                Loratadine.INSTANCE.getModuleManager().modules.clear();
//                Loratadine.INSTANCE.setCommandManager(null);
//                Loratadine.INSTANCE.setMinecraft(null);
//                Loratadine.INSTANCE.setModuleManager(null);
//                Loratadine.INSTANCE.setEventManager(null);
//                UnsafeUtils.freeMemory();
//                System.exit(0);
//                break;
//
//            case 4: // 时间不足
//                WindowsUtil.error("时间不足!");
//                Fucker.login = false;
//                Loratadine.INSTANCE.getModuleManager().modules.clear();
//                Loratadine.INSTANCE.setCommandManager(null);
//                Loratadine.INSTANCE.setMinecraft(null);
//                Loratadine.INSTANCE.setModuleManager(null);
//                Loratadine.INSTANCE.setEventManager(null);
//                UnsafeUtils.freeMemory();
//                System.exit(0);
//                break;
//
//            case 6: // 未允许的版本号
//                WindowsUtil.error("未允许的版本号!");
//                Fucker.login = false;
//                Loratadine.INSTANCE.getModuleManager().modules.clear();
//                Loratadine.INSTANCE.setCommandManager(null);
//                Loratadine.INSTANCE.setMinecraft(null);
//                Loratadine.INSTANCE.setModuleManager(null);
//                Loratadine.INSTANCE.setEventManager(null);
//                UnsafeUtils.freeMemory();
//                System.exit(0);
//                break;
//        }
        Fucker.userData = wrapper.readUser();

                final String name = "wyx6669036";
                final boolean isBeta = true;
                final UserType userType = UserType.getFromName(Fucker.userData.userLimits.tag);

                Fucker.login = true;
                Fucker.userType = userType;
                Fucker.name = name;
                Fucker.isBeta = isBeta;

                if (!Fucker.firstLogin) return;
                Fucker.firstLogin = false; // 破处

                Loratadine.INSTANCE.init();
                if (isBeta) {
                    WindowsUtil.success("验证成功! 尊敬的Beta用户 -> " + name);
                } else {
                    WindowsUtil.success("验证成功! 用户 -> " + name);
                }
    }

    @Override
    public void write(PacketWrapper wrapper) throws Throwable {
    }

    @Override
    public int packetId() {
        return 0;
    }
}
