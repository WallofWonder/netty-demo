package org.example.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static org.example.netty.c4.TestByteBuf.log;

public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(buf);

        // 零拷贝切片，切片过程中没有发生数据复制，切片和 buf 共享内存
        // 不能往切片增加数据，会抛出IndexOutOfBoundsException
        ByteBuf buf1 = buf.slice(0, 5);
        ByteBuf buf2 = buf.slice(5, 5);
        log(buf1);
        log(buf2);

        // 调用切片retain可以保留切片
        // 防止buf被释放后影响切片的使用
        buf1.retain();

        // buf 和 buf1 的元素同步修改
        buf1.setByte(0, 'b');
        log(buf1);
        log(buf);

        // 释放buf内存
        // 如果切片没有retain，再访问切片会报错
        buf.release();
        log(buf1);
        log(buf2);

        buf1.release();
        buf2.release();
    }
}
