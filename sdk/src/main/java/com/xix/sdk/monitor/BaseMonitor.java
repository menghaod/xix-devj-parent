package com.xix.sdk.monitor;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class BaseMonitor<E extends MonitorData> implements Monitor<E> {

    private Map<String, E> monitorMap = new ConcurrentHashMap<>();
    private MonitorDurable<E> monitorDurable;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private boolean isStarted = false;

    public BaseMonitor() {
    }

    public BaseMonitor(MonitorDurable<E> monitorDurable) {
        this.monitorDurable = monitorDurable;
    }

    @Override
    public void count(String uniKey, boolean isSuccess, Function<String, E> createIfAbsent, Consumer<E> extendEntity) {
        E monitorData = getEntity(uniKey, createIfAbsent);
        synchronized (monitorData) {
            modifyData(monitorData, isSuccess);
            if (extendEntity != null) {
                extendEntity.accept(monitorData);
            }
        }
    }

    @Override
    public Map<String, E> getAllMonitorData() {
        return monitorMap;
    }

    @Override
    public void resetMonitorData() {
        monitorMap = new ConcurrentHashMap<>();
    }

    @Override
    public void durable() {
        if (CollectionUtils.isEmpty(monitorMap)) {
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            monitorDurable.durable(monitorMap);
            log.info("CountScheduler durable data count is {}, time spent {}ms", monitorMap.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("An error occurred in countScheduler durable, saveInfo: {}, errorInfo: ", JSONObject.toJSONString(monitorMap.values()), e);
        }
    }

    private E getEntity(String uniKey, Function<String, E> createIfAbsent) {
        return createIfAbsent != null ?
                monitorMap.computeIfAbsent(uniKey, createIfAbsent) :
                Optional.ofNullable(monitorMap.get(uniKey))
                        .orElseThrow(() -> new RuntimeException(String.format("the entity is null for key:%s, you can try input `Function<String, E> createIfAbsent` to create entity if absent", uniKey)));
    }

    protected void modifyData(E countEntity, boolean isSuccess) {
        countEntity.setTotalCount(countEntity.getTotalCount() + 1);
        if (isSuccess) {
            countEntity.setSuccessCount(countEntity.getSuccessCount() + 1);
        } else {
            countEntity.setFailedCount(countEntity.getFailedCount() + 1);
        }
        countEntity.setUpdatedTime(LocalDateTime.now());
    }

    public BaseMonitor<E> startSchedule() {
        return startSchedule(new ScheduleInfo());
    }

    /**
     * 只允许启动一次
     */
    public BaseMonitor<E> startSchedule(ScheduleInfo scheduleInfo) {
        if (isStarted) {
            log.warn("the countScheduler have been started");
            return this;
        }
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(scheduleInfo.corePoolSize, runnable -> {
            String threadName = scheduleInfo.threadName + "-thread-" + scheduleInfo.threadNumber.getAndIncrement();
            return new Thread(runnable, threadName);
        });
        // 每隔schedulePeriodSeconds秒执行一次
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(this::durable, scheduleInfo.schedulePeriodSeconds, scheduleInfo.schedulePeriodSeconds, scheduleInfo.timeUnit);
        isStarted = true;
        return this;
    }

    public static class ScheduleInfo {
        // 只有一个任务，故一个线程即可，不对外暴露set()
        private final int corePoolSize = 1;
        private int schedulePeriodSeconds = 30;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private String threadName = "monitorSchedule";
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public ScheduleInfo setSchedulePeriodSeconds(int schedulePeriodSeconds) {
            this.schedulePeriodSeconds = schedulePeriodSeconds;
            return this;
        }

        public ScheduleInfo setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public ScheduleInfo setThreadName(String threadName) {
            this.threadName = threadName;
            return this;
        }
    }
}
