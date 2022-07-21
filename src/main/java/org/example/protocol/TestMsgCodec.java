package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import org.example.massage.LoginRequestMessage;

public class TestMsgCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new MessageCodec()
        );
        LoginRequestMessage message = new LoginRequestMessage("Zhangsan", "123");
        // 编码后出站
        channel.writeOutbound(message);

        // decode 入站后解码
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);

        ByteBuf s1 = buf.slice(0, 100);
        ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100);
        // 先转100B，再传剩下的
        s1.retain(); // 引用计数+1，防止writeInbound后释放内存
        // 若传了不完整的数据，LengthFieldBasedFrameDecoder会停止将数据继续沿着pipeline传递
        channel.writeInbound(s1);
        // 等待数据接收完，会将数据拼接完整再传给下一个handler
        channel.writeInbound(s2);
    }
}
