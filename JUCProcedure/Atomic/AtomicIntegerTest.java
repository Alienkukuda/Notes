package JUCProcedure.Atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 原子类自增
 * @author Captain
 * @date 2019/11/19
 **/
public class AtomicIntegerTest {

    private AtomicInteger i = new AtomicInteger(0);

    public AtomicInteger getValue() {
        return i;
    }

    public void add() {
        i.getAndIncrement();
    }

    public static void main(String[] args) {
        AtomicIntegerTest test = new AtomicIntegerTest();
        for (int i = 0; i < 5; i++) {
            test.add();
        }
        System.out.println(test.getValue());
    }
}
