package JUCProcedure.ReentrantLockAndCondition;

/**
 * 写线程
 * @author Captain
 * @date 2019/11/14
 **/
public class BoundedBufferWrite implements Runnable {

    private BoundedBuffer buffer;

    public BoundedBufferWrite(BoundedBuffer boundedBuffer) {
        this.buffer = boundedBuffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                buffer.put("captain");
                System.out.println(Thread.currentThread().getName()+":写入成功");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
