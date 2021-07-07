package com.hd.limiter;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 固定窗口限流器
 *
 * @Description: 控制资源每秒被访问次数的固定窗口实现
 * @Author: Hoda
 * @Date: Create in 2021/7/7
 * @Modifier:
 * @ModifiedDate:
 */
public class FixWindowLimiter {

    private Semaphore semaphore;

    public FixWindowLimiter(int limit) {
        this.semaphore = new Semaphore(limit);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            this.semaphore.release(limit - semaphore.availablePermits());
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void await() {
        semaphore.acquireUninterruptibly();
    }

    public static void main(String[] args) {
        AtomicInteger at = new AtomicInteger(0);
        FixWindowLimiter limiter = new FixWindowLimiter(100);

        new Thread(() -> {
            while (true) {
                // 每秒输出一次AtomicInteger的最新值
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(at.get());
            }
        }).start();

        // 启动100个线程对AtomicInteger进行累加，为了方便就没有使用线程池
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                while (true) {
                    // 每次累加操作前先调用await方法，超过设定的ops时会阻塞线程
                    limiter.await();
                    at.incrementAndGet();
                }
            }).start();
        }
    }
}
