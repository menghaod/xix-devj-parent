package com.xix.sdk.monitor;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitorData {
    // 监控实体唯一身份标识
    private String uniKey;
    private long totalCount;
    private long successCount;
    private long failedCount;
    private LocalDateTime createdTime = LocalDateTime.now();
    private LocalDateTime updatedTime = LocalDateTime.now();

    public MonitorData() {
    }

    public MonitorData(String uniKey) {
        this.uniKey = uniKey;
    }
}
