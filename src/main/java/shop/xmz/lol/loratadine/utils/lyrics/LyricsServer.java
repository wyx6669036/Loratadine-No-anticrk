package shop.xmz.lol.loratadine.utils.lyrics;

import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author DSJ_
 * @description A server to receive lyrics from NCM
 */
public class LyricsServer {
    private static LyricsServer INSTANCE;
    private final HttpServer server;

    private LyricsServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 10123), 0);

        // 127.0.0.1.10123/ 可以启动本地服务器
        server.createContext("/", new LyricsHandler());

        // 127.0.0.1.10123/close 可以手动关闭本地服务器
        server.createContext("/close", exchange -> stop());
    }


    /**
     * 获取这个本地服务器的实例
     */
    @SneakyThrows
    public static LyricsServer getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new LyricsServer();
            System.out.println("[SMTC Lyrics Debug] " + "启动本地服务器成功，监听地址为 " + INSTANCE.server.getAddress().toString().substring(1));
            INSTANCE.server.start();
        }
        return INSTANCE;
    }

    /**
     * 关闭这个本地服务器
     */
    public void stop() {
        INSTANCE.server.stop(0);
        System.out.println("[SMTC Lyrics Debug] " + "关闭本地服务器");
        INSTANCE = null;
    }
}
