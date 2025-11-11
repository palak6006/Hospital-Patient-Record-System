package com.hosp.records;

import java.io.Serializable;
import java.util.Comparator;
import java.util.PriorityQueue;

public class EmergencyRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class ERPatient {
        public final String patientId;
        public final int severity; // 1..10
        public final long timestamp;
        public ERPatient(String patientId, int severity) {
            this.patientId = patientId;
            this.severity = severity;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final PriorityQueue<ERPatient> queue = new PriorityQueue<>(
            Comparator.comparingInt((ERPatient e) -> -e.severity)
                      .thenComparingLong(e -> e.timestamp)
    );

    public synchronized void arrive(String patientId, int severity) {
        queue.add(new ERPatient(patientId, severity));
    }

    // remove and return next patient
    public synchronized ERPatient nextToProcess() {
        return queue.poll();
    }

    // peek without removing
    public synchronized ERPatient peekNext() {
        return queue.peek();
    }

    public synchronized int queueSize() { return queue.size(); }
}