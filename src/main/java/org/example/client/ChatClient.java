package org.example.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.massage.LoginRequestMessage;
import org.example.massage.LoginResponseMessage;
import org.example.protocol.MessageCodecSharable;
import org.example.protocol.ProtocolFrameDecoder;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();

        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        // 用于等待登录结果的信号量
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN_SUCCESS = new AtomicBoolean(false);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
//                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 负责接收控制台输入，向服务器发送消息
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.print("用户名：\n> ");
                                String userName = scanner.nextLine();
                                System.out.print("密码：\n> ");
                                String password = scanner.nextLine();
                                LoginRequestMessage message = new LoginRequestMessage(userName, password);
                                ctx.writeAndFlush(message);
                                System.out.println("等待服务器校验登录信息...");
                                try {
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                // 登录失败，关闭 channel，使得 closeFuture 解除阻塞
                                if (!LOGIN_SUCCESS.get()) {
                                    ctx.channel().close();
                                    return;
                                }
                                while (true) {
                                    // 从功能菜单选择下一个操作
                                    System.out.println("==============Menu================");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");
                                    System.out.print("> ");
                                    String command = scanner.nextLine();
                                    String[] s = command.split(" ");
                                }
                            }, "system in").start();
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}", msg);
                            // 处理 server 返回的登录结果
                            if (msg instanceof LoginResponseMessage) {
                                LoginResponseMessage message = (LoginResponseMessage) msg;
                                LOGIN_SUCCESS.set(message.isSuccess());
                            }
                            // 唤醒 “system in” 线程
                            WAIT_FOR_LOGIN.countDown();
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8888).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
