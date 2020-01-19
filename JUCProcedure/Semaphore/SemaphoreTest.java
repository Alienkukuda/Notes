package JUCProcedure.Semaphore;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

/**
 * @author Captain
 * @date 2019/11/26
 **/
public class SemaphoreTest {
    public static void main(String[] args) {
        //三个通道
        Semaphore semaphore = new Semaphore(3);

        for (int i = 1; i < 5; i++) {
            new SecurityCheckThread(i, semaphore).start();
        }
    }

    private static class SecurityCheckThread extends Thread {
        private int seq;
        private Semaphore semaphore;

        public SecurityCheckThread(int seq, Semaphore semaphore) {
            this.seq = seq;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                System.out.println("NO." + seq + "乘客，正在检查中");

                if (seq % 2 == 0) {
                    Thread.sleep(1000);
                    System.out.println("NO." + seq + "乘客，身份可疑，逮捕！");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
                System.out.println("NO." + seq + "乘客，检查完成。");
            }
        }
    }
}
