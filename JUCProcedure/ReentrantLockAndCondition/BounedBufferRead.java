package JUCProcedure.ReentrantLockAndCondition;

/**
 * 读线程
 * @author Captain
 * @date 2019/11/14
 **/
public class BounedBufferRead implements Runnable {
    private BoundedBuffer buffer;

    public BounedBufferRead(BoundedBuffer boundedBuffer) {
        this.buffer = boundedBuffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                buffer.take();
                System.out.println(Thread.currentThread().getName()+":读取成功");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
