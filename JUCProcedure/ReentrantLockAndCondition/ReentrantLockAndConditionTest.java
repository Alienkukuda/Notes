package JUCProcedure.ReentrantLockAndCondition;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock结合Condition实现输出12A34B...
 * @author Captain
 * @date 2019/11/13
 **/
public class ReentrantLockAndConditionTest {
    public static void main(String[] args) {
        MyThread myThread = new MyThread();
        int count = 0;
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();

        ExecutorService pool = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        pool.execute(new Runnable() {
            int count = 0;
            @Override
            public void run() {
                try {
                    while (count++ < 1000) {
                        myThread.printNum();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        pool.execute(new Runnable() {
            int count = 0;
            @Override
            public void run() {
                try {
                    while (count++ < 1000) {
                        myThread.printLetter();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
