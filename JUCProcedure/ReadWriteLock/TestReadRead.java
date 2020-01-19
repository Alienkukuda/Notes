package JUCProcedure.ReadWriteLock;

/**
 * @author Captain
 * @date 2019/11/20
 **/
public class TestReadRead {
    public static void main(String[] args) {

        MyReadWriteLock myReadWriteLock = new MyReadWriteLock();
        myReadWriteLock.put("a","fantj_a");
        //读读不互斥（共享）
        //读写互斥
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(myReadWriteLock.get("a"));
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(myReadWriteLock.get("a"));
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(myReadWriteLock.get("a"));
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(myReadWriteLock.get("a"));
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(myReadWriteLock.get("a"));
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(myReadWriteLock.get("a"));
            }
        }).start();

    }
}
