package org.example.c2;

import java.nio.ByteBuffer;

import static org.example.c2.ByteBufferUtil.debugAll;

public class TestByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});
        buffer.flip();

        buffer.get(new byte[4]);
        debugAll(buffer);
        buffer.rewind(); // 读指针复位到0，从头开始读
        buffer.get(new byte[4]);
        debugAll(buffer);
        buffer.rewind();

        buffer.get(); // a
        buffer.get(); // b
        buffer.mark(); // mark c
        buffer.get(); // c
        buffer.get(); // d
        buffer.reset(); // reset to c
        System.out.println((char) buffer.get()); // print c

        System.out.println((char) buffer.get(2)); // c
        System.out.println((char) buffer.get(2)); // c
        System.out.println((char) buffer.get(2)); // c
    }
}
