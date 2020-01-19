package JUCProcedure.Executor;

import java.util.concurrent.*;

/**
 * @author Captain
 * @date 2019/11/19
 **/
public class CallableAndFutureTest {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        CallTask task = new CallTask();

        Future<Integer> futureTask = null;
        Integer result = null;
        try{
            futureTask = executor.submit(task);
            result = futureTask.get(3000, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        Thread.sleep(1000);

        System.out.println("主线程在执行任务");

        if (null == result) {
            System.out.println("task运行失败");
        } else {
            System.out.println("task运行结果" + result);
        }

        System.out.println("所有任务执行完毕");
    }
}
class CallTask implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("子线程在进行计算");
        Thread.sleep(3000);
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += i;
        }
        return sum;
    }
}