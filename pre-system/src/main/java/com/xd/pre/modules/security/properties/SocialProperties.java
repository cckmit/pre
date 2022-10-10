package com.xd.pre.modules.security.properties;

/**
 * @Classname SocialProperties
 * @Author Created by Lihaodong (alias:小东啊) lihaodongmail@163.com
 * @Date 2019-08-06 14:50
 * @Version 1.0
 */
public abstract class SocialProperties {
    private String appId;
    private String appSecret;

    public SocialProperties() {
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return this.appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
