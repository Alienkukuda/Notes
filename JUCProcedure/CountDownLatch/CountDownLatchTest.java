package JUCProcedure.CountDownLatch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author Captain
 * @date 2019/11/20
 **/
public class CountDownLatchTest {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        Service service = new Service(latch);
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();

        ExecutorService pool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 5; i++) {
            pool.submit(service);
        }

        System.out.println("main thread await. ");
        latch.await();
        System.out.println("main thread finishes await. ");

        pool.shutdown();
    }
}

class Service implements Runnable {

    private CountDownLatch latch;

    public Service(CountDownLatch countDownLatch) {
        this.latch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " execute task. ");
            sleep(2);
            System.out.println(Thread.currentThread().getName() + " finished task. ");
        } finally {
            latch.countDown();
        }
    }
    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
