package org.example.nio.c2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.example.nio.c2.ByteBufferUtil.debugAll;

public class TestByteBufferString {
    public static void main(String[] args) {
        // ============ 1. String -> ByteBuffer ===================
        // 直接使用put
        ByteBuffer buffer1 = ByteBuffer.allocate(10);
        buffer1.put("hello".getBytes());
        debugAll(buffer1);
        // Charset 会自动切换到读模式
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        debugAll(buffer2);
        // wrap 会自动切换到读模式
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        debugAll(buffer3);

        // ============ 2. ByteBuffer -> String ===================
        // tip: 第一种直接使用put的方法得到的buffer要先切换到读模式
        buffer1.flip();
        String str1 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println(str1);

        String str2 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println(str2);
    }
}
