package pers.guanidine.utility.thread;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 可命名线程工厂使用样例。
 *
 * @author Guanidine Beryllium
 */
public class NamedThreadPool {
    private final ThreadPoolExecutor sqlQueryExecutor;

    private final ThreadPoolExecutor htmlQueryExecutor;

    private final ThreadPoolExecutor normalExecutor;

    private static volatile NamedThreadPool threadPool;

    private NamedThreadPool() {
        if (threadPool != null) {
            throw new RuntimeException("就你会用反射啊（战术后仰）...");
        }
        sqlQueryExecutor = new ThreadPoolExecutor(
                1,
                3,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(3),
                new NamedThreadFactory("SQLQuery")
        );

        htmlQueryExecutor = new ThreadPoolExecutor(
                3,
                10,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(5),
                new NamedThreadFactory("HTMLQuery")
        );

        normalExecutor = new ThreadPoolExecutor(
                5,
                10,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(10),
                new NamedThreadFactory("Normal")
        );
    }

    public static NamedThreadPool getInstance() {
        if (threadPool == null) {
            synchronized (NamedThreadPool.class) {
                if (threadPool == null) {
                    threadPool = new NamedThreadPool();
                }
            }
        }
        return threadPool;
    }

    public ThreadPoolExecutor getSqlQueryExecutor() {
        return sqlQueryExecutor;
    }

    public ThreadPoolExecutor getHtmlQueryExecutor() {
        return htmlQueryExecutor;
    }

    public ThreadPoolExecutor getNormalExecutor() {
        return normalExecutor;
    }
}
