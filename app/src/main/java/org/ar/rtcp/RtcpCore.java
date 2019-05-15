package org.ar.rtcp;

import org.ar.rtcp_kit.ARRtcpKit;

/**
 * Created by Skyline on 2017/6/14.
 */

public class RtcpCore {

    private static RtcpCore mInstance;

    public static RtcpCore Inst() {
        if(null == mInstance) {
            mInstance = new RtcpCore();
        }
        return mInstance;
    }



    private ARRtcpKit mRtcpKit;

    public ARRtcpKit getmRtcpKit() {
        //写成单例模式 全局持有一个rtcp对象
        if(null == mRtcpKit) {
            mRtcpKit = new ARRtcpKit("wawa","wawa");
        }

        return mRtcpKit;
    }
}
