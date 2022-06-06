package org.example.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 2. 把 channel 注册到 selector
        // 事件发生后可以通过 SelectionKey 得到事件发生的信息
        // 这里 key 只关注 accept 事件
        SelectionKey sscKey = ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        log.debug("register key: {}", sscKey);

        ssc.bind(new InetSocketAddress(8888));
        while (true) {
            // 3. select 方法，没事件就阻塞，有事件就恢复
            // select 在事件没有得到处理时不会阻塞
            // 事件发生后要么处理要么取消，不能不理会
            selector.select();
            // 4. 处理事件
            Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
            while (itr.hasNext()) {
                SelectionKey key = itr.next();
                log.debug("key: {}", key);
                /*ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel sc = channel.accept();
                log.debug("sc: {}", sc);*/
                key.cancel();
            }
        }
    }
}
