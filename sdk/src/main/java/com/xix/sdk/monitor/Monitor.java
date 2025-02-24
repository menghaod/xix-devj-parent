package com.xix.sdk.monitor;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Monitor<E extends MonitorData> {

    /**
     * 计数
     *
     * @param uniKey    监控唯一标识
     * @param isSuccess 本次操作是否成功
     */
    default void count(String uniKey, boolean isSuccess) {
        count(uniKey, isSuccess, null, null);
    }

    /**
     * 计数
     *
     * @param uniKey         监控唯一标识
     * @param isSuccess      本次操作是否成功
     * @param createIfAbsent 该uniKey首次监控则创建
     * @param extendEntity   对监控对象进行操作扩展
     */
    void count(String uniKey, boolean isSuccess, Function<String, E> createIfAbsent, Consumer<E> extendEntity);

    /**
     * 获取所以的监控数据
     */
    Map<String, E> getAllMonitorData();

    /**
     * 重置监控信息
     */
    void resetMonitorData();

    /**
     * 监控数据持久化
     */
    default void durable() {
        // do nothing
    }

}
