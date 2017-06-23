package top.leeys.test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import top.leeys.server.MainService;


public class PerformanceTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    DataSource dataSource;

    /*
        存储过程：
         CREATE PROCEDURE proc_select_course(in course_code varchar(30), in sid varchar(30), out result int)
         BEGIN
             DECLARE p_error INT DEFAULT 0;
             DECLARE CONTINUE HANDLER FOR SQLSTATE '22003' SET p_error = -1;
             DECLARE CONTINUE HANDLER FOR SQLSTATE '23000' set p_error = -2;
             START TRANSACTION;
             UPDATE tb_course SET surplus_num = surplus_num - 1 WHERE `code` = course_code; #surplus_num为负数抛22003
             INSERT INTO tb_record VALUES(null, course_code, sid);  #sid重复抛23000错误
             IF p_error != 0 THEN
                ROLLBACK;
             ELSE
                COMMIT;
             END IF;
             SET result = p_error;
         END;

        测试专用存储过程
        CREATE PROCEDURE proc_test_update(in course_code varchar(30),out result int)
        BEGIN
            DECLARE p_error INT DEFAULT 0;
            DECLARE CONTINUE HANDLER FOR SQLSTATE '22003' SET p_error = -1;
            START TRANSACTION;
            UPDATE tb_course SET surplus_num = surplus_num + 1 WHERE `code` = course_code; #surplus_num为负数抛22003
            IF p_error != 0 THEN
                ROLLBACK;
            ELSE
                COMMIT;
            END IF;
            SET result = p_error;
        END;
     */


    /**
     * 测试存储过程
     */
    @Test
    public void testProcedure() {
        String courseCode = "04XX04000";
        String sid = "0104150564";
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = dataSource.getConnection();
            cs = conn.prepareCall("{call proc_select_course(?, ?, ?)}");
            cs.setString(1, courseCode);
            cs.setString(2, sid);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            System.out.println("存储过程执行结果是：" + cs.getInt(3));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (cs != null) {
                    cs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 存储过程测试
     */
    @Test
    public void testProcedure2() {
        int threads = 30;   //1000线程
        final int time = 1000 * 10; //10秒
        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int updateNum = 0;
                    String courseCode = getRandomCourseCode();
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start <= time) {
                        Connection conn = null;
                        CallableStatement cs = null;
                        try {
                            conn = dataSource.getConnection();
                            cs = conn.prepareCall("{call proc_test_update(?, ?)}");
                            cs.setString(1, courseCode);
                            cs.registerOutParameter(2, Types.INTEGER);
                            cs.execute();
                            updateNum++;
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (cs != null) {
                                    cs.close();
                                }
                                if (conn != null) {
                                    conn.close();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println(Thread.currentThread() + "done," + courseCode + " call:" + updateNum);
                    exeNum += updateNum;
                }
            }).start();
        }

        try {
            Thread.sleep(time + 500);
            System.out.println("平均每秒执行数：" + exeNum / (time / 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟web环境线程的每一个请求对应一次数据库操作
     */
    @Test
    public void webEnvTest() {
        long start = System.currentTimeMillis();
        int time = 1000 * 10; //10秒
        int num = 0;
        while (System.currentTimeMillis() - start <= time) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                        Connection conn = null;
                        PreparedStatement ps = null;
                        try {
                            conn = dataSource.getConnection();

                            /*update*/
                            conn.setAutoCommit(false);
                            ps = conn.prepareStatement("update tb_course set surplus_num = surplus_num + 1 where code = ?");
                            ps.setString(1, getRandomCourseCode());
                            ps.executeUpdate();
                            conn.commit();

                            /*select*/
//                            ps = conn.prepareStatement("select * from tb_course");
//                            ps.executeQuery();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            try {
                                conn.rollback();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                        } finally {
                            try {
                                if (ps != null) {
                                    ps.close();
                                }
                                if (conn != null) {
                                    conn.close();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            }).start();
            num++;
        }
        System.out.println("平均每秒执行数：" + num / (time / 1000));
    }


    /**
     * 本地原生，测试极限性能
     * select 3W update 7800
     */
    @Test
    public void localNativeTest() {
        int threads = 30;   //线程
        final int time = 1000 * 10; //10秒
        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int updateNum = 0;
                    String courseCode = getRandomCourseCode();
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start <= time) {
                        Connection conn = null;
                        PreparedStatement ps = null;
                        try {
                            conn = dataSource.getConnection();
                            /*update*/
                            conn.setAutoCommit(false);
                            ps = conn.prepareStatement("update tb_course set surplus_num = surplus_num + 1 where code = ?");
                            ps.setString(1, courseCode);
                            ps.executeUpdate();
                            conn.commit();

                            /*select*/
//                            ps = conn.prepareStatement("select * from tb_course");
//                            ps.executeQuery();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            try {
                                conn.rollback();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                        } finally {
                            try {
                                if (ps != null) {
                                    ps.close();
                                }
                                if (conn != null) {
                                    conn.close();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        updateNum++;
                    }
                    System.out.println(Thread.currentThread() + "done," + courseCode + " update:" + updateNum);
                    exeNum += updateNum;
                }
            }).start();
        }
        try {
            Thread.sleep(time + 500);
            System.out.println("平均每秒执行数：" + exeNum / (time / 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    int exeNum = 0;
    @Test
    public void testUpdate() {
        int threads = 100;
        final int time = 1000 * 10; //10秒
        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int updateNum = 0;
                    String courseCode = getRandomCourseCode();
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start <= time) {
                        mainService.testUpdate(courseCode); updateNum++;
//                        mainService.getCourseList(); updateNum ++;
                    }
                    System.out.println(Thread.currentThread() + "done," + courseCode + " update:" + updateNum);
                    exeNum += updateNum;
                }
            }).start();
        }

        try {
            Thread.sleep(time + 500);
            System.out.println("平均每秒执行数：" + exeNum / (time / 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    String getRandomCourseCode() {
        String[] courseCodes = new String[]{
                "04XX02800", "04XX03200", "04XX03300", "04XX03400", "04XX03500", "04XX03600", "04XX03700", "04XX03900", "04XX04000"
        };
        return courseCodes[(int) (Math.random() * courseCodes.length)];
    }
}
