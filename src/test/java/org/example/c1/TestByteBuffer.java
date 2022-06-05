package org.example.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        // FileChannel
        // 1. IO流
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                // 从channel读取数据，向buffer写
                int len = channel.read(buffer);
                log.debug("num of byte read {}", len);
                if (len == -1) {
                    break;
                }
                // 打印buffer内容
                buffer.flip(); // 切换至读模式
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    log.debug("byte read {}", (char) b);
                }
                buffer.clear(); // 切换至写模式
            }
        } catch (IOException ignored) {
        }

    }
}
