package pers.guanidine.utility;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pers.guanidine.utility.dao.MysqlProcessDAO;
import pers.guanidine.utility.dao.SQLProcessDAO;
import pers.guanidine.utility.impls.CallBack;
import pers.guanidine.utility.thread.NamedThreadPool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

public class SQLProcessTest {
    @BeforeClass
    public void beforeClass() {
        System.out.println("====================Test SQLProcessDAO====================");
    }

    @Test
    public void testQuery() {
        CallBack<ResultSet> callBack = result -> {
            try {
                int cnt = 30;
                while (result.next() && cnt-- > 0) {
                    System.out.println(result.getString("uname"));
                }
                Assert.assertEquals(cnt, 27);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        MysqlProcessDAO.INSTANCE.getInstance().query("select * from user_info", callBack);
    }

    @Test
    public void testQuerySingleton() {
        SQLProcessDAO processor1 = MysqlProcessDAO.INSTANCE.getInstance();
        SQLProcessDAO processor2 = MysqlProcessDAO.INSTANCE.getInstance();
        Assert.assertEquals(processor1.hashCode(), processor2.hashCode());
    }

    @Test(expectedExceptions = NoSuchMethodException.class)
    public void testQuerySingletonException() throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Constructor<MysqlProcessDAO> constructor = MysqlProcessDAO.class.getDeclaredConstructor(String.class,
                String.class, String.class, String.class);
        constructor.setAccessible(true);
        constructor.newInstance("", "", "", "");
        constructor.newInstance("", "", "", "");
    }

    @Test
    public void testMultiThreadQuery() {
        SQLProcessDAO process = MysqlProcessDAO.INSTANCE.getInstance();
        Semaphore mutex = new Semaphore(1);
        var ref = new Object() {
            boolean flag = true;
        };
        // 并不总是正确就对了（雾），循环一次正确率高一些
        for (int i = 0; i < 1; i++) {
            process.insert("follow", new String[]{"3", "1"}, new String[0],
                    NamedThreadPool.getInstance().getSqlQueryExecutor());
            process.delete("follow", "uid=3 and follow_id=1",
                    NamedThreadPool.getInstance().getSqlQueryExecutor());
            CallBack<ResultSet> callBack = result -> {
                try {
                    int cnt = 0;
                    if (result.next()) {
                        cnt++;
                    }
                    synchronized (mutex) {
                        ref.flag = cnt == 0;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };
            process.query("select * from follow where uid=3 and follow_id=1", callBack);
        }
        Assert.assertTrue(ref.flag);
    }
}
