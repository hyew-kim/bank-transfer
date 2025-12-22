package com.example.banktransfer.global.progress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProgressRecorder implements ProgressRecorder {
    private final Map<String, ProgressStatus> statusMap = new ConcurrentHashMap<>();
    private final Map<String, String> reasonMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryStart(String progressKey) {
        return statusMap.putIfAbsent(progressKey, ProgressStatus.PROCESSING) == null;
    }

    @Override
    public ProgressStatus getStatus(String progressKey) {
        return statusMap.get(progressKey);
    }

    @Override
    public void record(String progressKey, ProgressStatus status, String reason) {
        statusMap.put(progressKey, status);
        if (reason == null) {
            reasonMap.remove(progressKey);
        } else {
            reasonMap.put(progressKey, reason);
        }
    }

    @Override
    public void delete(String progressKey) {
        statusMap.remove(progressKey);
        reasonMap.remove(progressKey);
    }
}
