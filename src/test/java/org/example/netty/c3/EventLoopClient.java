package org.example.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
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
        // main 会无阻塞向下执行
        // 如果没有 sync 方法，write 时可能连接尚未建立
        channelFuture.sync();
        Channel channel = channelFuture.channel();
        channel.writeAndFlush("hello world");
    }
}
