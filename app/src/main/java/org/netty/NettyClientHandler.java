package org.netty;


import android.content.Intent;


import com.dj.androidpowercontrol.MainActivity;
import com.dj.androidpowercontrol.MyApplication;
import com.dj.androidpowercontrol.Util;

import org.netty.module.BaseMsg;
import org.netty.module.LoginMsg;
import org.netty.module.PingMsg;
import org.netty.module.PushMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * Handler
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsg> {

    private static final String TAG = "NettyClientHandler";

    //设置心跳时间  开始
    public static final int MIN_CLICK_DELAY_TIME = 1000 * 30;
    private long lastClickTime = 0;
    //设置心跳时间   结束

    //利用写空闲发送心跳检测消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                        lastClickTime = System.currentTimeMillis();
                        PingMsg pingMsg = new PingMsg();
                        ctx.writeAndFlush(pingMsg);
                        Util.showLog(TAG, "send ping to server----------");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //这里是断线要进行的操作
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Util.showLog(TAG,"重连了。---------");
        NettyClientBootstrap bootstrap = PushClient.getBootstrap();
        bootstrap.startNetty();
    }

    //这里是出现异常的话要进行的操作
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Util.showLog(TAG,"出现异常了。。。。。。。。。。。。。");
        cause.printStackTrace();
    }

    //这里是接受服务端发送过来的消息
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
        switch (baseMsg.getType()) {
            case LOGIN:
                //向服务器发起登录
                LoginMsg loginMsg = new LoginMsg();
                loginMsg.setUserName("robin");
                loginMsg.setPassword("yao");
                channelHandlerContext.writeAndFlush(loginMsg);
                break;
            case PING:
                Util.showLog(TAG,"receive ping from server----------");
                break;
            case PUSH:
                PushMsg pushMsg = (PushMsg) baseMsg;
//                Intent intent = new Intent(MyApplication.getAppContext(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("hi", pushMsg.getContent());
//                MyApplication.getAppContext().startActivity(intent);
                Util.showLog(TAG,"0000000000000000000000000" + pushMsg.getTitle() + " , " + pushMsg.getContent());
                if ("1".equals(pushMsg.getContent())) {
                    MainActivity.instance.mHandler.sendEmptyMessage(0x111);
                }
                if ("0".equals(pushMsg.getContent())) {
                    MainActivity.instance.mHandler.sendEmptyMessage(0x000);
                }
                break;
            default:
                Util.showLog(TAG,"default..");
                break;
        }
        ReferenceCountUtil.release(baseMsg);
    }


}
