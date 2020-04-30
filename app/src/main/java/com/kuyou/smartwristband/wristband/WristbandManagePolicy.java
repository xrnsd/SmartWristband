package com.kuyou.smartwristband.wristband;

/**
 * action: WristbandManage 策略的定义[基本]
 * <p>
 * author: wuguoxian <br/>
 * date: 20200403 <br/>
 * remark1:<p> 频率 : 一次为多少毫秒 </p>
 */
public interface WristbandManagePolicy {
    /**
     * action: 手环数据同步状态确认的频率<br/>
     */
    public static final int DELAY_DATA_SYNC_CHECK = 1200;
    /**
     * action: 数据同步和上传的心跳频率<br/>
     * remark1: 数据同步和上传由于失败等等原因,服务器收到数据的频率永远不等于这个值
     * remark2: 频繁连接失败时上传频率基本就是连接同步成功的频率
     */
    public static final int DELAY_HEARTBEAT_DATA = 1000 * 4;
    /**
     * action: 数据同步和上传的心跳频率<br/>
     * remark: APP处于后台
     */
    public static final int DELAY_HEARTBEAT_DATA_BACKGROUND = 1000 * 6 * 1;
    /**
     * action: 流程心跳频率 <br/>
     * remark: 设备状态维持更新频率
     */
    public static final int DELAY_HEARTBEAT_WRISTBAND_STATUS = 1000 * 2;
    /**
     * action: 流程心跳频率 <br/>
     * remark: APP处于后台时的设备状态维持更新频率
     */
    public static final int DELAY_HEARTBEAT_WRISTBAND_STATUS_BACKGROUND = DELAY_HEARTBEAT_WRISTBAND_STATUS * 4;
    /**
     * action: 流程心跳频率 <br/>
     * remark: 未绑定手环时的设备状态维持更新频率
     */
    public static final int DELAY_HEARTBEAT_WRISTBAND_STATUS_UNBOUND = 1000 * 60 * 2;

    /**
     * action: 手环重新连接超时的时长<br/>
     **/
    public static final int RECONNECT_TIME_OUT = 1000 * 15;

    /**
     * action: 手环重新连接失败最大连续失败次数<br/>
     * remark:超过后放弃重连，自动解绑
     **/
    public static final int TIMES_RECONNECT_MAX_CONSECUTIVE_FAIL = 30;
    /**
     * action: 手环重新连接失败快速重连次数<br/>
     * remark:必须小于 TIMES_RECONNECT_MAX_CONSECUTIVE_FAIL
     **/
    public static final int FAST_RECONNECT_TIMES_MAX = 5;
    /**
     * action: 手环数据同步状态确认次数<br/>
     */
    public static final int TIMES_DATA_SYNC_CHECK = 4;
    /**
     * action: 是否允许失败重新上传<br/>
     */
    public static final boolean IS_ENABLE_FAILED_REUPLOAD = true;
    /**
     * action: 上传失败数据最大记录量<br/>
     */
    public static final int NUMBER_OF_UPLOAD_FAIL_DATA_MAX = 512;
    /**
     * action: 上传失败次数达到就允许缓存到本地<br/>
     */
    public static final int AUTO_SAVE_UPLOAD_FAIL_DATA_FREQ = 15;
}
