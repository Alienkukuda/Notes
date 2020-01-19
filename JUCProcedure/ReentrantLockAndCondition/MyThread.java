package JUCProcedure.ReentrantLockAndCondition;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Captain
 * @date 2019/11/14
 **/
public class MyThread {

    final Lock lock = new ReentrantLock();

    Condition condition1 = lock.newCondition();
    Condition condition2 = lock.newCondition();

    int num = 0;
    int numPtr = 1;
    int letter = 0;



    public void printNum() throws InterruptedException {
        lock.lock();
        try {
            if (0 != num && 1 != num) {
                condition1.await();
            }
            System.out.print(numPtr++);
            if (num == 0) {
                num = 1;
            } else if (num == 1) {
                num = 2;
            }
            condition2.signal();
        } finally {
            lock.unlock();
        }
    }

    public void printLetter() throws InterruptedException {
        lock.lock();
        try {
            if (2 != num) {
                condition2.await();
            }
            if (letter == 26) {
                letter = 0;
            }
            System.out.print(getLetter(letter));
            letter++;
            if (num == 2) {
                num = 0;
            }
            condition1.signal();
        } finally {
            lock.unlock();
        }
    }

    private char getLetter(int add) {
        return (char)((int)'A' + add);
    }

}
