package pers.guanidine.utility.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可命名的线程工厂。
 *
 * @author Guanidine Beryllium
 */
public class NamedThreadFactory implements ThreadFactory {
    protected static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
    protected final AtomicInteger mThreadNum = new AtomicInteger(1);
    protected final String mPrefix;
    protected final boolean isDaemon;
    protected final ThreadGroup mGroup;

    public NamedThreadFactory() {
        this("pool-" + POOL_SEQ.getAndIncrement(), false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean isDaemon) {
        mPrefix = prefix + "-thread-";
        this.isDaemon = isDaemon;
        SecurityManager s = System.getSecurityManager();
        mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String name = mPrefix + mThreadNum.getAndIncrement();
        Thread thr = new Thread(mGroup, runnable, name, 0);
        thr.setDaemon(isDaemon);
        return thr;
    }

    public ThreadGroup getThreadGroup() {
        return mGroup;
    }
}
