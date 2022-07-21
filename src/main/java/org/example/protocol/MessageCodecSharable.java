package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.example.massage.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 自定义消息编解码器 <br/>
 * 继承自 {@link MessageToMessageCodec} 可以被 {@link Sharable} 注解。<br/>
 * 建议和 {@link LengthFieldBasedFrameDecoder} 一起使用，确保 {@link MessageToMessageCodec#decode} 接收的是完整的 msg。
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        // 1. 4字节的魔数
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        // 2. 1 字节的版本号
        buf.writeByte(1);
        // 3. 1 字节的序列化方式 jdk:0 , json:1
        buf.writeByte(0);
        // 4. 1 字节的指令类型
        buf.writeByte(msg.getMessageType());
        // 5. 4 个字节 (暂不考虑双工通信，随便写)
        buf.writeInt(msg.getSequenceId());
        // 填充字节，让前7步凑到16字节
        buf.writeByte(0xff);
        // 6. 获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 7. 正文长度
        buf.writeInt(bytes.length);
        // 8. 正文
        buf.writeBytes(bytes);

        out.add(buf);
    }

    /**
     * @param msg 这个msg是从 {@link LengthFieldBasedFrameDecoder} 传过来的，一定是完整的 msg，不用考虑粘包半包问题
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int magicNum = msg.readInt();
        byte version = msg.readByte();
        byte serializerType = msg.readByte();
        byte messageType = msg.readByte();
        int sequenceId = msg.readInt();
        msg.readByte(); // 无意义的填充字节
        int length = msg.readInt();
        byte[] bytes = new byte[length];
        msg.readBytes(bytes, 0, length);

        // jdk反序列化
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Message message = (Message) ois.readObject();
        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);
        out.add(message);
    }
}
