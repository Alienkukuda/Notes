package JUCProcedure.ReentrantLockAndCondition;

/**
 * 用于测试BoundedBuffer、BoundedBufferWrite和BounedBufferRead类
 *
 * @author Captain
 * @date 2019/11/14
 **/
public class BoundedBufferTest {

    private static int writeNum = 3;

    private static int readNum = 3;

    public static void main(String[] args) {

        //所有的写线程和读线程共同操作的缓冲队列
        BoundedBuffer buffer = new BoundedBuffer();

        //启动写线程
        for (int i = 0; i < writeNum; i++) {

            new Thread(new BoundedBufferWrite(buffer), "写入线程-" + i).start();

            System.out.println("启动写线程—" + i);
        }
        //启动读线程
        for (int j = 0; j < readNum; j++) {

            new Thread(new BounedBufferRead(buffer), "读取线程-" + j).start();

            System.out.println("启动读线程-" + j);

        }
    }
}
