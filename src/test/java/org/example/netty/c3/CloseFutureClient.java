package org.example.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new StringEncoder());
                    }
                })
                .connect("localhost", 8888);

        Channel channel = channelFuture.sync().channel();
        log.debug("{}", channel);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();
//                    log.debug("连接关闭后的操作"); // 错误用法，因为close是非阻塞且在nio线程执行
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        // 获取 CloseFuture 对象，支持同步或异步处理关闭
        ChannelFuture closeFuture = channel.closeFuture();
        // 方法一
        /*log.debug("waiting close...");
        // 阻塞，等待连接关闭
        closeFuture.sync();
        log.debug("连接关闭后的操作");*/
        // 方法二
        closeFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            // 在 nio 线程打印
            log.debug("连接关闭后的操作");
            group.shutdownGracefully();
        });
    }
}
