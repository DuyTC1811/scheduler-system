package org.example.config;

import java.util.concurrent.*;

public class GlobalExecutorConfig {
    private static final ExecutorService executor;

    static {
        ThreadFactory customFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(String.format("[ THREAD-%d ]", thread.threadId()));
            thread.setDaemon(true);
            return thread;
        };

        executor = new ThreadPoolExecutor(
                5,                               // Core pool size: số lượng thread tối thiểu luôn được giữ.
                10,                                         // Max pool size: số lượng thread tối đa.
                60L,                                        // keep alive
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),     // Queue: nơi chứa task khi core thread đã bận.
                customFactory,
                new ThreadPoolExecutor.AbortPolicy()        // chính sách từ chối task khi quá tải
        );

        // ✅ shutdown hook để đảm bảo đóng executor khi JVM dừng
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down executor...");
            executor.shutdown();
        }));
    }

    private GlobalExecutorConfig() {
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
