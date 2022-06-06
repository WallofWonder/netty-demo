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
        ssc.configureBlocking(false); // 关闭ServerSocketChannel的阻塞模式
        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8888));
        // 3.1 建立连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 3.2 建立连接, SocketChannel用于通信s
            SocketChannel sc = ssc.accept(); // 非阻塞，线程继续运行，若没有连接建立，sc为null
            if (sc != null) {
                log.debug("Connected...{}" ,sc);
                sc.configureBlocking(false); // 关闭SocketChannel的阻塞模式
                channels.add(sc);
            }
            for (SocketChannel channel : channels) {
                // 4. 接收客户端的数据
                int read = channel.read(buffer);// 非阻塞，没有读到数据则返回0
                if (read > 0) {
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("After reading...{}", channel);
                }
            }
        }
    }
}
