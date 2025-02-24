package com.xix.sdk.monitor;

import java.util.Map;

public interface MonitorDurable<E extends MonitorData> {

    /**
     * 将监控数据持久化到磁盘
     */
    void durable(Map<String, E> monitorData);


    class DefaultDurable<E extends MonitorData> implements MonitorDurable<E> {
        @Override
        public void durable(Map<String, E> monitorData) {
            // do nothing
        }
    }
}
