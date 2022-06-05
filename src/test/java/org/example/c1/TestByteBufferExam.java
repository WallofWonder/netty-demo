package org.example.c1;

import java.nio.ByteBuffer;

import static org.example.c1.ByteBufferUtil.debugAll;

public class TestByteBufferExam {
    public static void main(String[] args) {
        // 模拟分两次接收数据
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello, world\nI'm ZhangSan\nHo".getBytes());
        split(source);
        source.put("w are you?\n".getBytes());
        split(source);
    }

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
}
