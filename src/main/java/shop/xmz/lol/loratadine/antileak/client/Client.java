package shop.xmz.lol.loratadine.antileak.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.jetbrains.annotations.NotNull;

public class Client {
    public static Channel channel;
    public static Bootstrap bootstrap;
    public static EventLoopGroup group;

    public static void start() throws Throwable {
        group = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(@NotNull SocketChannel channel) {
                     ChannelPipeline pipeline = channel.pipeline();
                     pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                     pipeline.addLast(new Handler());
                 }
             });

            // 尝试连接服务器
//            ChannelFuture f = bootstrap.connect("195.245.242.204", 8151).sync();
//            channel = f.channel();

            // 添加关闭钩子以释放资源
            Runtime.getRuntime().addShutdownHook(new Thread(group::shutdownGracefully));
        } catch (Throwable t) {
            group.shutdownGracefully();
            throw t;
        }
    }
}