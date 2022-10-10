package com.xd.pre.common.sign.dll;

public class JD {
    static {
        System.loadLibrary("jd");
    }
    public native String jd_sign(String functionId,String uuid,String body,String st,String clientVersion);
}
