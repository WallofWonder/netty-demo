package org.example.netty.c3;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup(2); // IO任务、普通任务、定时任务
//        DefaultEventLoop defaultEventLoop = new DefaultEventLoop(); // 普通任务、定时任务
        // 获取下一个循环对象（循环遍历）
        for (int i = 0; i < 4; i++) {
            System.out.println(group.next());
        }

        // 异步执行普通任务
        /*group.next().submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("普通任务");
        });

        log.debug("main");*/

        // 异步执行定时任务，2秒后开始，每1秒打印一次
        group.next().scheduleAtFixedRate(() -> {
            log.debug("定时任务");
        }, 2, 1, TimeUnit.SECONDS);
    }
}
