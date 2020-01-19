package JUCProcedure.CyclicBarrier;

import org.omg.CORBA.TIMEOUT;

import java.util.concurrent.*;

/**
 * @author Captain
 * @date 2019/11/21
 **/
public class CyclicBarrierTest {
    public static void main(String[] args) {
        final int totalThread = 4;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalThread; i++) {
            executorService.execute(() -> {
                System.out.println("到达屏障..");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("屏障之后开始执行..");
            });
        }
        executorService.shutdown();
    }
}
