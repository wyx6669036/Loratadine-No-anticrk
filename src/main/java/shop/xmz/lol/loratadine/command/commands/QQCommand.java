package shop.xmz.lol.loratadine.command.commands;

import shop.xmz.lol.loratadine.command.Command;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;
import shop.xmz.lol.loratadine.utils.ClientUtils;

import shop.xmz.lol.loratadine.utils.Multithreading;
import shop.xmz.lol.loratadine.utils.misc.WebUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QQCommand extends Command {
    public QQCommand() {
        super("qq");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ClientUtils.log("Usage: .qq <qq number>");
            return;
        }

        Multithreading.run(() -> {
            NotificationManager.add(NotificationType.WARNING, "QQ查询", "获取信息...", 5);
            String result;
            result = WebUtils.get("https://zy.xywlapi.cc/qqapi?qq=" + args[0]);

            assert result != null;
            Matcher matcher = Pattern.compile("\"(qq\":\"\\d+)\",\"(phone\":\"\\d+)\",\"(.*)").matcher(result);

            if (matcher.find()) {
                NotificationManager.add(NotificationType.WARNING, "QQ查询", "请查看聊天栏", 5);
                ClientUtils.log("§6§oQQ: " + matcher.group(1).substring(5));
                ClientUtils.log("§6§o联系方式: " + matcher.group(2).substring(8));
                ClientUtils.log("§6§o归属地: " + matcher.group(3).substring(12, 20));
                ClientUtils.log("§a查询完成");
            } else {
                NotificationManager.add(NotificationType.WARNING, "QQ查询", "获取 QQ 信息失败!", 5);
            }
        });
    }
}