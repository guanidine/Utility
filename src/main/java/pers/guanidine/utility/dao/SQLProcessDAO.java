package pers.guanidine.utility.dao;

import org.apache.commons.lang3.StringUtils;
import pers.guanidine.utility.impls.CallBack;

import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * SQL增删改查。
 *
 * @author Guanidine Beryllium
 */
public class SQLProcessDAO {

    String driver;
    String url;
    String uname;
    String passwd;

    private static volatile SQLProcessDAO sqlProcessor;

    private SQLProcessDAO(String driver, String url, String uname, String passwd) {
        if (sqlProcessor != null) {
            throw new RuntimeException("就你会用反射啊（战术后仰）...");
        }
        this.driver = driver;
        this.url = url;
        this.uname = uname;
        this.passwd = passwd;
    }

    /**
     * 初始化SQL连接。
     *
     * @param driver SQL驱动
     * @param url    连接URL
     * @param uname  SQL用户名
     * @param passwd SQL密码
     * @return 数据库查询实例
     */
    public static SQLProcessDAO getInstance(String driver, String url, String uname, String passwd) {
        if (sqlProcessor == null) {
            synchronized (SQLProcessDAO.class) {
                if (sqlProcessor == null) {
                    sqlProcessor = new SQLProcessDAO(driver, url, uname, passwd);
                }
            }
        }
        return sqlProcessor;
    }

    /**
     * SELECT操作。
     * <p>
     * 通过接口回调异步返回查询结果。
     *
     * @param sql               完整的查询语句
     * @param resultSetCallBack {@link CallBack <ResultSet>} 回调接口
     * @see CallBack
     */
    public void query(String sql, CallBack<ResultSet> resultSetCallBack) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, uname, passwd);
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            resultSetCallBack.onCall(rs);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            closeConnection(conn, ps);
        }
    }

    /**
     * INSERT操作。
     * <p>
     * 根据目标表格的字段序列顺序依次指定字段值。
     *
     * @param table  表名
     * @param values 值序列
     * @see SQLProcessDAO#insert(String, String[], String[])
     */
    public void insert(String table, String[] values) {
        insert(table, values, new String[0]);
    }

    /**
     * INSERT操作。
     * <p>
     * INSERT语句的基本用法是：{@code INSERT INTO <表名> (字段1, 字段2, ...) VALUES (值1, 值2, ...);}
     * <p>
     * 其中字段序列可省略，即调用{@link SQLProcessDAO#insert(String, String[])}，当且仅当值序列覆盖了目标表格的所有字段，且顺序也与之相一致。
     * <p>
     * 如果代码有多线程的需求，例如不允许在主线程下创建数据库连接，那么可以传入一个线程池接口{@link Executor}来分配数据库使用的线程，即调用{@link SQLProcessDAO#insert(String,
     * String[], String[], Executor)}。
     *
     * @param table   表名
     * @param values  值序列
     * @param apiKeys 字段序列
     */
    public void insert(String table, String[] values, String[] apiKeys) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, uname, passwd);
            StringBuilder insertSql = new StringBuilder();
            if (apiKeys.length == 0) {
                insertSql.append("insert into ").append(table).append(" values (")
                        .append(StringUtils.join(Arrays.asList(values), ","))
                        .append(")");
            } else {
                insertSql.append("insert into ").append(table).append(" (")
                        .append(StringUtils.join(Arrays.asList(apiKeys), ","))
                        .append(") values (")
                        .append(StringUtils.join(Arrays.asList(values), ","))
                        .append(")");
            }
            System.out.println(insertSql);
            ps = conn.prepareStatement(insertSql.toString());
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, ps);
        }
    }

    /**
     * INSERT操作。
     * <p>
     * 在指定线程池中调度一个线程来执行INSERT操作。
     *
     * @param table    表名
     * @param values   值序列
     * @param apiKeys  字段序列
     * @param executor 线程池
     * @see SQLProcessDAO#insert(String, String[], String[])
     */
    public void insert(String table, String[] values, String[] apiKeys, Executor executor) {
        executor.execute(() -> insert(table, values, apiKeys));
    }

    /**
     * UPDATE操作。
     * <p>
     * <a color='red'>注意：</a>这种写法将更新表内所有元组的相应字段值！
     *
     * @param table  表名
     * @param update 字段更新序列，即{@code SET}和{@code WHERE}之间的内容
     * @see SQLProcessDAO#update(String, String, String)
     */
    public void update(String table, String update) {
        update(table, update, null);
    }

    /**
     * UPDATE操作。
     * <p>
     * UPDATE语句的基本用法是：{@code UPDATE <表名> SET 字段1=值1, 字段2=值2, ... WHERE ...;}
     * <p>
     * 如果您已经确定，这次对数据库的更新的确是<b>覆盖表格所有元组</b>的，则可以调用{@link SQLProcessDAO#update(String, String)}。
     * <p>
     * 如果代码有多线程的需求，例如不允许在主线程下创建数据库连接，那么可以传入一个线程池接口{@link Executor}来分配数据库使用的线程，即调用{@link SQLProcessDAO#update(String,
     * String, String, Executor)}。
     *
     * @param table      表名
     * @param update     字段更新序列，即{@code SET}和{@code WHERE}之间的内容
     * @param updateCase 字段更新条件，即{@code WHERE}后面的全部内容
     */
    public void update(String table, String update, String updateCase) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, uname, passwd);
            StringBuilder updateSql = new StringBuilder();
            if (updateCase == null) {
                updateSql.append("update ").append(table).append(" set ").append(update);
            } else {
                updateSql.append("update ").append(table).append(" set ")
                        .append(update).append(" where ").append(updateCase);
            }
            System.out.println(updateSql);
            ps = conn.prepareStatement(updateSql.toString());
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, ps);
        }
    }

    /**
     * UPDATE操作。
     * <p>
     * 在指定线程池中调度一个线程来执行UPDATE操作。
     *
     * @param table      表名
     * @param update     字段更新序列，即{@code SET}和{@code WHERE}之间的内容
     * @param updateCase 字段更新条件，即{@code WHERE}后面的全部内容
     * @param executor   线程池
     * @see SQLProcessDAO#update(String, String, String)
     */
    public void update(String table, String update, String updateCase, Executor executor) {
        executor.execute(() -> update(table, update, updateCase));
    }

    /**
     * DELETE操作。
     * <p>
     * <a color='red'>注意：</a>这种写法将删除表内所有数据！
     *
     * @param table 表名
     * @see SQLProcessDAO#delete(String, String)
     */
    public void delete(String table) {
        delete(table, null);
    }

    /**
     * DELETE操作。
     * <p>
     * DELETE语句的基本用法是：{@code DELETE FROM <表名> WHERE ...;}
     * <p>
     * 如果您已经确定，的确有必要<b>清空表内所有数据</b>，则可以调用{@link SQLProcessDAO#delete(String)}。
     * <p>
     * 如果代码有多线程的需求，例如不允许在主线程下创建数据库连接，那么可以传入一个线程池接口{@link Executor}来分配数据库使用的线程，即调用{@link SQLProcessDAO#delete(String,
     * String, Executor)}。
     *
     * @param table      表名
     * @param deleteCase 字段删除条件，即{@code WHERE}后面的全部内容
     */
    public void delete(String table, String deleteCase) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, uname, passwd);
            StringBuilder deleteSql = new StringBuilder();
            if (deleteCase == null) {
                deleteSql.append("delete from ").append(table);
            } else {
                deleteSql.append("delete from ").append(table)
                        .append(" where ").append(deleteCase);
            }
            System.out.println(deleteSql);
            ps = conn.prepareStatement(deleteSql.toString());
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, ps);
        }
    }

    /**
     * DELETE操作。
     * <p>
     * 在指定线程池中调度一个线程来执行DELETE操作。
     *
     * @param table      表名
     * @param deleteCase 字段删除条件，即{@code WHERE}后面的全部内容
     * @param executor   线程池
     * @see SQLProcessDAO#delete(String, String)
     */
    public void delete(String table, String deleteCase, Executor executor) {
        executor.execute(() -> delete(table, deleteCase));
    }

    /**
     * 关闭数据库连接。
     */
    private void closeConnection(Connection conn, PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
