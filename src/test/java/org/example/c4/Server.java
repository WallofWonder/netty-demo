package org.example.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static org.example.c2.ByteBufferUtil.debugAll;

@Slf4j
public class Server {

    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact();
    }

    public static void main(String[] args) throws IOException {
        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 2. 把 channel 注册到 selector
        // 事件发生后可以通过 SelectionKey 得到事件发生的信息
        // 这里 key 只关注 accept 事件
        SelectionKey sscKey = ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        log.debug("Register key: {}", sscKey);

        ssc.bind(new InetSocketAddress(8888));
        while (true) {
            // 3. select 方法，没事件就阻塞，有事件就恢复
            // select 在事件没有得到处理时不会阻塞
            // 事件发生后要么处理要么取消，不能不理会
            selector.select();
            // 4. 创建 selectedKeys 集合，内部包含了触发了事件的 key，逐个进行处理
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.debug("Key: {}", key);
                // 区分事件类型
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    // 将 buffer 作为附件，绑定到 SelectionKey 上，作为附件
                    // 让一个 Channel 单独使用一个 buffer，便于实现扩容
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    sc.register(selector, SelectionKey.OP_READ, buffer);
                    log.debug("Connection established: {}", sc);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = sc.read(buffer);
                        if (read == -1) {
                            // 正常断开的情况，read 为 -1
                            // 客户端断开后，也会触发一次 read 事件
                            // key 就是 if (key.isAcceptable()) 里面 register 获得的 key
                            // 要把 key 从 selector 中注销，下次select()的时候会被删除
                            log.debug("Client closed");
                            key.cancel();
                            sc.close();
                        } else {
                            // 普通的 read 事件
                            split(buffer);
                            // 如果 split 后 buffer 没变化，说明消息长度超出buffer容量
                            // 申请一个更大容量的 buffer 作为新的附件
                            if (buffer.position() == buffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() << 1);
                                buffer.flip();
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        // 异常断开的情况，抛出异常
                        log.error(e.getMessage());
                        key.cancel();
                    }
                }
                // 因为 selectedKeys 不会主动将处理过事件的 key 移除
                // 所以处理完毕，必须将负责监听此事件的 key 移除
                // 否则下次出现事件的时候，遍历到这个旧 key 会出现问题
                iter.remove();
//                key.cancel();
            }
        }
    }
}
