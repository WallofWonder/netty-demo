package org.example.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class TestPipeline {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 通过 channel 拿到 pipeline,
                        // pipeline 预置两个handler：head 和 tail
                        ChannelPipeline pipeline = ch.pipeline();
                        // 添加处理器，名字 h1
                        // head <--> ... <--> {添加位置} <--> tail
                        pipeline// InboundHandlers ============== 接收到数据时触发
                                .addLast("h1", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("1");
                                        ByteBuf buf = (ByteBuf) msg;
                                        String name = buf.toString(StandardCharsets.UTF_8);
                                        // 将 name 传递到 下一个 InboundHandler（h2）
                                        super.channelRead(ctx, name);
                                    }
                                })
                                .addLast("h2", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("2");
                                        // h2得到的是h1处理后的msg（String name）
                                        // 初始化一个Student对象
                                        Student student = new Student(msg.toString());
                                        // 将 student 传递到 下一个 InboundHandler（h3）
                                        super.channelRead(ctx, student);
                                    }
                                })
                                .addLast("h3", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("3, 结果：{}, class:{}", msg, msg.getClass());
                                        // 发送一些数据，触发outbound handler
                                        // 触发顺序是反向pipeline： h4 <- h5 <- h6
                                        ch.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
                                    }
                                })
                                // OutboundHandlers ============== 发送数据时触发
                                .addLast("h4", new ChannelOutboundHandlerAdapter() {
                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.debug("4");
                                        super.write(ctx, msg, promise);
                                    }
                                })
                                .addLast("h5", new ChannelOutboundHandlerAdapter() {
                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.debug("5");
                                        super.write(ctx, msg, promise);
                                    }
                                })
                                .addLast("h6", new ChannelOutboundHandlerAdapter() {

                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.debug("6");
                                        super.write(ctx, msg, promise);
                                    }
                                });
                    }
                }).bind(8888);
    }

    @Data
    @AllArgsConstructor
    static class Student {
        private String name;
    }
}
