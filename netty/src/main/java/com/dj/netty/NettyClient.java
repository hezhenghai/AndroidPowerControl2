package com.dj.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by Administrator
 * on 2016/8/10.
 */
public class NettyClient {

    public void connect(String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);//channel 通道
        bootstrap.option(ChannelOption.TCP_NODELAY, true);//option 选项
        bootstrap.handler(new ChildChannelHandler());//handler 处理

        //发起异步连接
        ChannelFuture future = bootstrap.connect(host, port);

        //等待客户端连接关闭
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
