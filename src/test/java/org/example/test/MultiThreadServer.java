package org.example.test;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.example.c2.ByteBufferUtil.debugAll;

@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        // Boss 线程为主线程，专门处理 Accept 事件
        Thread.currentThread().setName("Boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector bossSel = Selector.open();
        ssc.register(bossSel, SelectionKey.OP_ACCEPT, null);
        ssc.bind(new InetSocketAddress(8888));
        // 创建固定数量的worker，这里先创建一个，用于处理 read 事件
        Worker worker = new Worker("worker-0");
        while (true) {
            bossSel.select();
            Iterator<SelectionKey> iter = bossSel.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("Connected...{}", sc.getRemoteAddress());
                    // 关联 Worker 和 SocketChannel
                    log.debug("Before register...{}", sc.getRemoteAddress());
                    worker.register(sc);
                    log.debug("After register...{}", sc.getRemoteAddress());
                }
            }
        }
    }

    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean started = false;

        // 任务队列
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque<>();

        public Worker(String name) {
            this.name = name;
        }

        /**
         * Initialize thread and selector
         *
         * @param sc SocketChannel from ACCEPT event
         */
        public void register(SocketChannel sc) throws IOException {
            if (!started) {
                thread = new Thread(this, name);
                selector = Selector.open();
                thread.start();
                started = true;
            }
            // 在 boss 线程中创建 register 任务但不执行
            queue.add(() -> {
                try {
                    sc.register(this.selector, SelectionKey.OP_READ); // 注册READ事件
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // 先wakeup一次，让worker线程从select()中解除阻塞继续执行
            selector.wakeup();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select(); // worker线程中调用
                    // 在 worker 线程中取出 register 任务执行，
                    Runnable task = queue.poll();
                    if (task != null) {
                        task.run();
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("Read...{}", channel.getRemoteAddress());
                            channel.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
