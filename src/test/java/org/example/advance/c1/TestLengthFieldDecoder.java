package org.example.advance.c1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                // 最大帧长1024B
                // 长度头在开头4B
                // 实际内容在长度域往后1B，也就是版本头后
                // 去掉长度头的4B，保留版本头+实际内容
                new LengthFieldBasedFrameDecoder(1024, 0, 4, 1, 4),
                new LoggingHandler(LogLevel.DEBUG)
        );

        // 实际内容长度(4字节 int 表示) + 版本头(1字节表示) +实际内容
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer, "Hello, world");
        send(buffer, "Hi!");
        channel.writeInbound(buffer);
    }

    private static void send(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes(); // 实际内容
        int length = bytes.length;  // 实际内容长度
        buffer.writeInt(length);    // 添加长度header
        buffer.writeByte(1);  // 添加版本header
        buffer.writeBytes(bytes);   // 添加实际内容
    }
}
