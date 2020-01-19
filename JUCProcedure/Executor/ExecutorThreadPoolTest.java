package JUCProcedure.Executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author Captain
 * @date 2019/11/18
 **/
public class ExecutorThreadPoolTest {
    public static void main(String[] args) {

//        ExecutorService executorService = Executors.newSingleThreadExecutor();

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();

        //推荐这种方式，而不采用Executors的静态方法
        ExecutorService pool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                 new LinkedBlockingDeque<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        pool.execute(()-> System.out.println(Thread.currentThread().getName()));
        pool.shutdown();
    }
}
