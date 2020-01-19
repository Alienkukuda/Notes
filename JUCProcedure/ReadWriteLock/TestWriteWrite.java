package JUCProcedure.ReadWriteLock;

/**
 * @author Captain
 * @date 2019/11/20
 **/
public class TestWriteWrite {
    public static void main(String[] args) {
        MyReadWriteLock myReadWriteLock = new MyReadWriteLock();
        new Thread(new Runnable() {
            @Override
            public void run() {
                myReadWriteLock.put("b","fantj_b");
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                myReadWriteLock.put("b","fantj_b");
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                myReadWriteLock.put("b","fantj_b");
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                myReadWriteLock.put("b","fantj_b");
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                myReadWriteLock.put("b","fantj_b");
            }
        }).start();

    }
}
