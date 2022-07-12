package org.example.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TestJDKFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        // 提交任务
        Future<Integer> future = service.submit(() -> {
            log.debug("计算...");
            Thread.sleep(1000);
            return 50;
        });

        // 主线程通过future获取结果（阻塞，等待线程结束）
        log.debug("等待结果...");
        log.debug("结果：" + future.get());
    }
}
