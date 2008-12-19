package is.util;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class ConcurrentUtil
{
    public static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static ThreadPoolExecutor createThreadPool()
    {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(NUM_PROCESSORS, NUM_PROCESSORS, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        return pool;
    }

    public static void shutdownPoolAndAwaitTermination(ThreadPoolExecutor pool)
    {
        try
        {
            pool.shutdown();
            pool.awaitTermination(1000, TimeUnit.SECONDS);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
