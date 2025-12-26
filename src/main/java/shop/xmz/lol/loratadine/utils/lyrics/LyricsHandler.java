package shop.xmz.lol.loratadine.utils.lyrics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author DSJ_
 * @description A server to receive lyrics from NCM
 */
public class LyricsHandler implements HttpHandler {
    public static String basic; // 第一行歌词
    public static String extra; // 下一行歌词

    // 构造函数，初始化LyricsHandler对象
    public LyricsHandler() {}

    /**
     * 实现HttpHandler接口的handle方法
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();

        // 读取请求体并将其转换为字符串
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (uri.getPath().equals("/lyrics/lyrics")) {
            JsonObject json = new JsonParser().parse(body).getAsJsonObject();
            basic = json.get("basic").getAsString();
            extra = json.get("extra").getAsString();
        }

        String dsj = "200, ok";
        exchange.sendResponseHeaders(200, dsj.length());
        OutputStream out = exchange.getResponseBody();
        out.write(dsj.getBytes());
        out.flush();
        out.close();
    }
}
