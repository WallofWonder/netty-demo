package org.example.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static org.example.c2.ByteBufferUtil.debugRead;

@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        // =============== 理解阻塞模式 =============
        // 0. 用于缓存客户端数据
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 1. 创建服务器
        ServerSocketChannel ssc  = ServerSocketChannel.open();
        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8888));
        // 3.1 建立连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 3.2 建立连接, SocketChannel用于通信s
            log.debug("Connecting...");
            SocketChannel sc = ssc.accept(); // 阻塞方法
            log.debug("Connected...{}" ,sc);
            channels.add(sc);
            // 4. 接收客户端的数据
            for (SocketChannel channel : channels) {
                log.debug("Before reading...{}", channel);
                channel.read(buffer); // 阻塞方法
                buffer.flip();
                debugRead(buffer);
                buffer.clear();
                log.debug("After reading...{}", channel);
            }
        }
    }
}
