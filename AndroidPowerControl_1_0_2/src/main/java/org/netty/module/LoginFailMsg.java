package org.netty.module;

/**
 * 消息类型-登录失败
 *
 */
public class LoginFailMsg extends BaseMsg {

    private String failInfo;

    public LoginFailMsg() {
        super();
        setType(MsgType.LOGIN_FAIL);
    }

    public String getFailInfo() {
        return failInfo;
    }

    public void setFailInfo(String failInfo) {
        this.failInfo = failInfo;
    }
}