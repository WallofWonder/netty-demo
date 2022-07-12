package org.example.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
                                        super.channelRead(ctx, msg);
                                    }
                                })
                                .addLast("h2", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("2");
                                        super.channelRead(ctx, msg);
                                    }
                                })
                                .addLast("h3", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("3");
                                        // 发送一些数据，触发outbound handler
                                        // 触发顺序是反向pipeline： h4 <- h5 <- h6
                                        // 不能使用ChannelHandlerContext的writeAndFlush方法
                                        // ctx 会从当前handler倒着往回传递
                                        // ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
                                        // 正确做法是使用channel的writeAndFlush方法
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
