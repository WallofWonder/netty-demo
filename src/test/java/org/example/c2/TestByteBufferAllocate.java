package org.example.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBufferAllocate {
    public static void main(String[] args) {
        // class java.nio.HeapByteBuffer    堆内存，读写效率低，收到GC影响
        System.out.println(ByteBuffer.allocate(16).getClass());
        // class java.nio.DirectByteBuffer  直接内存，读写效率高（少一次拷贝），不受GC影响
        System.out.println(ByteBuffer.allocateDirect(16).getClass());
        ByteBuffer buffer = ByteBuffer.allocate(16);
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            int readBytes = channel.read(buffer);
            log.debug("readBytes: {}", readBytes);
        } catch (IOException ignore) {
        }
        // 追加写入，否则是覆盖
        try (FileChannel channel = new FileOutputStream("data.txt", true).getChannel()) {
            buffer.flip();
            int writeBytes = channel.write(buffer);
            log.debug("writeBytes: {}", writeBytes);
            buffer.clear();
        } catch (IOException ignore) {
        }
    }
}
