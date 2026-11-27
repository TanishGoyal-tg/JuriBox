package juribox.service;

import juribox.model.*;

import java.util.*;

/*
  Module: Concurrent Simulation & Inter-Thread Synchronization
  Member: Lakshit
  Date: 19 November 2026 - 20 November 2026
  Topic: Multithreading (Thread, join()), Inter-Thread Communication (wait() / notifyAll()), Guarded Blocks
 */
public class CourtSimulation {
    private static final String R = "\u001B[0m", B = "\u001B[1m", C = "\u001B[94m", G = "\u001B[92m", Y = "\u001B[93m";
    private static final String[] THREAD_COLORS = {"\u001B[93m", "\u001B[96m", "\u001B[92m", "\u001B[95m", "\u001B[94m"};

    public void simulateDaySessions(String date,
                                   List<Courtroom> courtrooms,
                                   List<Schedule> schedules,
                                   Map<String, Case> caseMap,
                                   Map<String, Judge> judgeMap,
                                   int slotDelayMs) {
        String bar = C + "+================================================================================+" + R;
        System.out.println("\n" + bar);
        System.out.printf(C + "| " + B + "\u001B[97m%-78s" + R + C + " |\n", "LIVE CONCURRENT COURT SESSION SIMULATION: " + date);
        System.out.printf(C + "| \u001B[90m%-78s" + R + C + " |\n", "Each Courtroom is running concurrently on its own independent Java Thread");
        System.out.println(bar + "\n");

        Map<String, List<Schedule>> roomSchedulesMap = new HashMap<>();
        for (Courtroom cr : courtrooms) {
            roomSchedulesMap.put(cr.getCourtroomId(), new ArrayList<>());
        }
        for (Schedule s : schedules) {
            if (s.getDate().equalsIgnoreCase(date.trim())) {
                List<Schedule> list = roomSchedulesMap.get(s.getCourtroomId());
                if (list != null) list.add(s);
            }
        }
        
        List<Thread> activeThreads = new ArrayList<>();

        // [Lakshit - 19 Nov 2026] Thread Spawning: Launch concurrent worker threads per operational courtroom
        int idx = 0;
        for (Courtroom cr : courtrooms) {
            List<Schedule> roomSchedules = roomSchedulesMap.get(cr.getCourtroomId());
            String tc = THREAD_COLORS[idx++ % THREAD_COLORS.length];
            CourtroomWorker worker = new CourtroomWorker(cr, date, roomSchedules, caseMap, judgeMap, slotDelayMs, tc);
            Thread roomThread = new Thread(worker, "Thread-" + cr.getCourtroomId());
            activeThreads.add(roomThread);
            roomThread.start();
        }

        // [Lakshit - 19 Nov 2026] Barrier synchronization: join() all courtroom threads to await day closure
        for (Thread t : activeThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("\u001B[91m[SIMULATION INTERRUPTED]" + R);
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("\n" + bar);
        System.out.printf(C + "| " + G + B + "%-78s" + R + C + " |\n", "ALL COURTROOM THREADS JOINED. SESSIONS CONCLUDED FOR: " + date);
        System.out.println(bar + "\n");
    }

    public static class CourtroomWorker implements Runnable {
        private final Courtroom courtroom;
        private final String date;
        private final CaseDocketQueue<Schedule> docketQueue;
        private final int totalHearings;
        private final Map<String, Case> caseMap;
        private final Map<String, Judge> judgeMap;
        private final int speedDelayMs;
        private final String tc;

        public CourtroomWorker(Courtroom courtroom, String date, List<Schedule> roomSchedules,
                               Map<String, Case> caseMap, Map<String, Judge> judgeMap, int speedDelayMs) {
            this(courtroom, date, roomSchedules, caseMap, judgeMap, speedDelayMs, "\u001B[96m");
        }

        public CourtroomWorker(Courtroom courtroom, String date, List<Schedule> roomSchedules,
                               Map<String, Case> caseMap, Map<String, Judge> judgeMap, int speedDelayMs, String tc) {
            this.courtroom = courtroom;
            this.date = date;
            this.caseMap = caseMap;
            this.judgeMap = judgeMap;
            this.speedDelayMs = speedDelayMs;
            this.tc = tc;
            this.totalHearings = roomSchedules.size();

            this.docketQueue = new CaseDocketQueue<>(Math.max(1, roomSchedules.size()));
            List<Schedule> sorted = new ArrayList<>(roomSchedules);
            sorted.sort(Comparator.comparingInt(Schedule::getStartSlot));
            for (Schedule s : sorted) {
                try {
                    this.docketQueue.produce(s);
                } catch (InterruptedException ignored) {}
            }
        }

        @Override
        public void run() {
            String tName = Thread.currentThread().getName();
            log(String.format("%s>>> [%s]%s %s: Session opened for %s (%d hearing(s) queued)",
                    B + tc, tName, R, courtroom.getRoomName(), date, totalHearings));

            if (totalHearings == 0) {
                log(String.format("    %s[%s]%s \u001B[90m%s: No hearings today. Room remains on standby.%s", tc, tName, R, courtroom.getRoomName(), R));
                return;
            }

            while (!docketQueue.isEmpty()) {
                Schedule s;
                try {
                    s = docketQueue.consume();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                Case c = caseMap.get(s.getCaseId());
                Judge j = judgeMap.get(s.getJudgeId());
                String title = (c != null) ? c.getTitle() : "Unknown";
                String judgeName = (j != null) ? j.getName() : s.getJudgeId();

                log(String.format("    %s[%s]%s [SLOT %d-%d] %sSTARTING:%s Case %s (\"%s\") | Presiding: %s",
                        B + tc, courtroom.getCourtroomId(), R, s.getStartSlot(), s.getEndSlot(), Y, R, s.getCaseId(), title, judgeName));

                try {
                    Thread.sleep((long) speedDelayMs * s.getDurationSlots());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                log(String.format("    %s[%s]%s [SLOT %d-%d] %sCONCLUDED:%s Case %s. Recess.",
                        B + tc, courtroom.getCourtroomId(), R, s.getStartSlot(), s.getEndSlot(), G, R, s.getCaseId()));
            }

            log(String.format("%s<<< %s:%s All daily court sessions completed. Chamber adjourned.", B + tc, courtroom.getRoomName(), R));
        }

        private void log(String msg) {
            synchronized (System.out) {
                System.out.println(msg);
            }
        }
    }

    public static class CaseDocketQueue<T> {
        private final Queue<T> queue = new LinkedList<>();
        private final int capacity;

        public CaseDocketQueue(int capacity) {
            this.capacity = Math.max(1, capacity);
        }

        public synchronized void produce(T item) throws InterruptedException {
            while (queue.size() >= capacity) {
                wait();
            }
            queue.add(item);
            notifyAll();
        }

        public synchronized T consume() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            T item = queue.poll();
            notifyAll();
            return item;
        }

        public synchronized boolean isEmpty() {
            return queue.isEmpty();
        }
    }
}

