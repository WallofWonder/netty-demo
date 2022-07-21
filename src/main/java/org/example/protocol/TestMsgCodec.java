package org.example.protocol;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import org.example.massage.LoginRequestMessage;

public class TestMsgCodec {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                new MessageCodec()
        );
        LoginRequestMessage message = new LoginRequestMessage("Zhangsan", "123");
        // 编码后出站
        channel.writeOutbound(message);
    }
}
