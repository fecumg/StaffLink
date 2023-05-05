package fpt.edu.stafflink.debouncing;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer {
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<Integer, TimerTask> delayedMap = new ConcurrentHashMap<>();

    private static final int ONE_AND_ONLY_KEY = 1;

    private Callback callback;
    private int interval;

    public Debouncer() {
    }

    public void call(Callback callback, int interval) {
        this.callback = callback;
        this.interval = interval;

        TimerTask task = new TimerTask(ONE_AND_ONLY_KEY);

        TimerTask prev;
        do {
            prev = delayedMap.putIfAbsent(ONE_AND_ONLY_KEY, task);
            if (prev == null)
                sched.schedule(task, interval, TimeUnit.MILLISECONDS);
        } while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
    }

// The task that wakes up when the wait time elapses
    private class TimerTask implements Runnable {
        private final int key;
        private long dueTime;
        private final Object lock = new Object();

        public TimerTask(int key) {
            this.key = key;
            extend();
        }

        public boolean extend() {
            synchronized (lock) {
                if (dueTime < 0) {
//                    Task has been shutdown
                    return false;
                }
                dueTime = System.currentTimeMillis() + interval;
                return true;
            }
        }

        @Override
        public void run() {
            synchronized (lock) {
                long remaining = dueTime - System.currentTimeMillis();
                if (remaining > 0) { // Re-schedule task
                    sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
                } else { // Mark as terminated and invoke callback
                    dueTime = -1;
                    try {
                        delayedMap.remove(key);
                        callback.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
