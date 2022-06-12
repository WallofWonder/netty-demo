package org.example.netty.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {
    public static void main(String[] args) {
        new ServerBootstrap() // 启动器，负责组装netty组件，启动服务器
                .group(new NioEventLoopGroup()) // select循环组.每个循环包含Thread和Selector，负责处理特定事件
                .channel(NioServerSocketChannel.class) // 选择ServerChannel的实现NioServerSocketChannel
                // boos 负责连接，child(worker) 负责处理读写，handler就是处理器
                .childHandler(new ChannelInitializer<NioSocketChannel>() { // 通道初始化器，本身也是一个handler，负责添加别的handler
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                // 添加 handler
                                .addLast(new StringDecoder()) // 将 ByteBuf 转为字符串
                                .addLast(new ChannelInboundHandlerAdapter() { // 自定义handler
                                    /**
                                     * 读事件
                                     * @param ctx handler上下文环境
                                     * @param msg StringDecoder 转换的字符串
                                     */
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        System.out.println(msg);
                                    }
                                });
                    }
                })
                .bind(8888); // 服务器监听端口
    }
}
