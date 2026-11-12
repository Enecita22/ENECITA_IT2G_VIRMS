package com.scc.ui;

import com.scc.dao.ActivityLogDao;

import java.util.List;

public class ActivityLogViewer {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void displayLogs() {
        System.out.println("\n=== SYSTEM ACTIVITY & AUDIT LOGS ===");
        List<String> logs = logDao.getAllLogs();
        if (logs == null || logs.isEmpty()) {
            System.out.println("No activity logs recorded yet.");
        } else {
            for (String log : logs) {
                System.out.println(log);
            }
        }
        System.out.println("======================================");
    }
}