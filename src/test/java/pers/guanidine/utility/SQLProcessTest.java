package pers.guanidine.utility;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pers.guanidine.utility.dao.SQLProcessDAO;
import pers.guanidine.utility.impls.CallBack;
import pers.guanidine.utility.thread.NamedThreadPool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

public class SQLProcessTest {
    String driver = "com.mysql.cj.jdbc.Driver";
    String url =
            "jdbc:mysql://81.70.96.193:3306/chatterusers?useSSL=false&characterEncoding=utf-8&serverTimezone=UTC";
    String uname = "root";
    String passwd = "asd123";

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
        SQLProcessDAO.getInstance(driver, url, uname, passwd).query("select * from user_info", callBack);
    }

    @Test
    public void testQuerySingleton() {
        SQLProcessDAO processor1 = SQLProcessDAO.getInstance(driver, url, uname, passwd);
        SQLProcessDAO processor2 = SQLProcessDAO.getInstance(driver, url, uname, passwd + "0");
        Assert.assertEquals(processor1.hashCode(), processor2.hashCode());
    }

    @Test(expectedExceptions = InvocationTargetException.class)
    public void testQuerySingletonException() throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Constructor<SQLProcessDAO> constructor = SQLProcessDAO.class.getDeclaredConstructor(String.class,
                String.class,
                String.class, String.class);
        constructor.setAccessible(true);
        constructor.newInstance(driver, url, uname, passwd);
        constructor.newInstance(driver, url, uname, passwd);
    }

    @Test
    public void testMultiThreadQuery() {
        SQLProcessDAO processor = SQLProcessDAO.getInstance(driver, url, uname, passwd);
        Semaphore mutex = new Semaphore(1);
        var ref = new Object() {
            boolean flag = true;
        };
        // 并不总是正确就对了（雾），循环一次正确率高一些
        for (int i = 0; i < 5; i++) {
            processor.insert("follow", new String[]{"3", "1"}, new String[0],
                    NamedThreadPool.getInstance().getSqlQueryExecutor());
            processor.delete("follow", "uid=3 and follow_id=1",
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
            processor.query("select * from follow where uid=3 and follow_id=1", callBack);
        }
        Assert.assertTrue(ref.flag);
    }
}
