package com.example.banktransfer.global.progress;

public interface ProgressRecorder {
    ProgressStatus getStatus(String progressKey);
    void record(String progressKey, ProgressStatus status, String reason);
    void delete(String progressKey);
}
