package org.netty;


import android.bluetooth.BluetoothAdapter;

import com.dj.androidpowercontrol.entity.MyConfig;
import com.dj.androidpowercontrol.util.Util;

import org.netty.module.Constants;
import org.netty.module.LoginMsg;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;

/**
 * bootstrap
 */
public class NettyClientBootstrap {

    private static final String TAG = "TAG";

    /**
     * uuid （蓝牙编号）
     */
    private String uuid;
    private String clientID;
    private int port = MyConfig.PORT;
    private String host = MyConfig.IP;
    public SocketChannel socketChannel;
    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(20);

    public void startNetty() throws InterruptedException {
        if (socketChannel != null && socketChannel.isOpen()) {
            Util.showLog(TAG, "已经连接");
        } else {
            //获取id
            myUUID();
//            Constants.setClientId("001");
            clientID = uuid.replace(":", "");
            Util.showLog(TAG, clientID + "---------" + uuid);
            Constants.setClientId(clientID);
            Util.showLog(TAG, "长链接开始");
            if (start()) {
                LoginMsg loginMsg = new LoginMsg();
                loginMsg.setPassword("yao");
                loginMsg.setUserName("robin");
                socketChannel.writeAndFlush(loginMsg);
                Util.showLog(TAG, "长链接成功");
            } else {
                Util.showLog(TAG, "长链接失败...");
            }
        }
    }

    private Boolean start() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(20, 10, 0));
                socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(new NettyClientHandler());
            }
        });
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                Util.showLog(TAG, "connect server  成功---------");
                return true;
            } else {
                Util.showLog(TAG, "connect server  失败---------");
                startNetty();
                return false;
            }
        } catch (Exception e) {
            Util.showLog(TAG, "无法连接----------------");
            return false;
        }
    }

    public void closeChannel() {
        if (socketChannel != null) {
            socketChannel.close();
        }
    }

    public boolean isOpen() {
        if (socketChannel != null) {
            Util.showLog(TAG, String.valueOf(socketChannel.isOpen()));
            return socketChannel.isOpen();
        }
        return false;
    }

    /**
     * 通过蓝牙地址，确定设备id（将蓝牙id作为设备唯一识别号）
     */
    private void myUUID() {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        uuid = mAdapter.getAddress();
    }
}