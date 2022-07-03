package org.example.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 带有 Future Promise 的都是和异步方法配套使用
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                .addLast(new StringEncoder(StandardCharsets.UTF_8)); // 将字符串编码为 ByteBuf
                    }
                })
                // 异步阻塞方法，main 发起调用，执行 connect 的是 Nio 线程
                .connect("localhost", 8888);
        // 方法一： main 会无阻塞向下执行，使用sync方法同步处理结果
        // 如果没有 sync 方法，write 时可能连接尚未建立
        /*channelFuture.sync(); // 阻塞，等待连接建立
        Channel channel = channelFuture.channel();
        channel.writeAndFlush("hello world");*/

        // 方法二：使用 addListener(回调对象) 异步处理结果
        // Nio 线程建立连接后调用
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            Channel channel = channelFuture1.channel();
            log.debug("{}", channel);
            channel.writeAndFlush("hello world");
        });
    }
}
