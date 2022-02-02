package pers.guanidine.utility.dao;

/**
 * SQL增删改查样例。
 * <p>
 * 以MySQL为例。对每一个数据库建立一个单例以供连接。
 *
 * @author Guanidine Beryllium
 */
public enum MysqlProcessDAO {
    /** 单例 */
    INSTANCE(
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://81.70.96.193:3306/chatterusers?useSSL=false&characterEncoding=utf-8&serverTimezone=UTC",
            "root",
            "asd123"
    );
    private final SQLProcessDAO sqlProcess;

    MysqlProcessDAO(String driver, String url, String uname, String passwd) {
        sqlProcess = new SQLProcessDAO(driver, url, uname, passwd);
    }

    /**
     * 初始化SQL连接。
     *
     * @return 数据库查询实例
     */
    public SQLProcessDAO getInstance() {
        return sqlProcess;
    }

}
