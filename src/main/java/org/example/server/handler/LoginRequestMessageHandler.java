package org.example.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.massage.LoginRequestMessage;
import org.example.massage.LoginResponseMessage;
import org.example.server.service.UserServiceFactory;
import org.example.server.session.SessionFactory;

/**
 * 登录请求处理器
 */
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        boolean success = UserServiceFactory.getUserService().login(username, password);
        if (success) {
            SessionFactory.getSession().bind(ctx.channel(), username);
        }
        LoginResponseMessage message = new LoginResponseMessage(success, success ? "登录成功" : "用户名或密码不正确");
        ctx.writeAndFlush(message);
    }
}
