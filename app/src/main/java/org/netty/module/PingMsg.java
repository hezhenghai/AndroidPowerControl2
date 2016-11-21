package org.netty.module;

/**
 * 心跳检测消息类型
 *
 */
public class PingMsg extends BaseMsg {
    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}
