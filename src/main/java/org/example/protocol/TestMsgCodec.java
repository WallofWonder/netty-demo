package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import org.example.massage.LoginRequestMessage;

public class TestMsgCodec {
    public static void main(String[] args) throws Exception {
        // 这样将 frameDecoder 抽取为一个实例进行复用存在隐患，
        // frameDecoder 处理带状态的信息，是线程不安全的，可能会把不同线程传来的消息进行拼接
        // 例子：worker1 和 worker2 同时发送 "1234"，frameDecoder 可能将 worker1 的 12 和 worker2 的 34 拼在一起
        LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0);
        // loggingHandler 处理的是无状态信息，可以抽取为一个实例进行复用
        // 在源码中 netty 也在可复用的 handler 上添加了 @Sharable 注解进行标记
        LoggingHandler loggingHandler = new LoggingHandler();

        EmbeddedChannel channel = new EmbeddedChannel(
                loggingHandler,
                frameDecoder,
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
