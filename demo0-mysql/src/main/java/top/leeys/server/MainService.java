package top.leeys.server;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.leeys.config.ErrorCode;
import top.leeys.domain.Course;
import top.leeys.domain.Record;
import top.leeys.domain.Student;
import top.leeys.exception.BaseException;
import top.leeys.repository.CourseRepository;
import top.leeys.repository.RecordRepository;
import top.leeys.repository.StudentRepository;

@Service
public class MainService {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    DataSource dataSource;
    /*如果嫌原生过于刻板，可以使用JdbcTemplate*/
    @Autowired
    JdbcTemplate jdbcTemplate;

    public Student getUser(String sid, String password) {
        return studentRepository.findBySidAndPassword(sid, password);
    }

    public Student getUserBySid(String sid) {
        return studentRepository.findOne(sid);
    }

    public List<Course> getCourseList() {
        List<Course> courseList = courseRepository.findAll();
        return courseList;
    }


    /**
     * 开启事务
     */
    @Transactional
    public void selectCourse(String sid, String courseCode) {
        addRecord(new Record(null, sid, courseCode));
        updateCourse(courseCode);
    }

    /*向Record表插入一行记录*/
    public Record addRecord(Record record) {
        return recordRepository.save(record);
    }

    /*更新课程余量-1*/
    public void updateCourse(String courseCode) {
        int result =  courseRepository.updateCourse(courseCode);
        if (result != 1) {
            /**
             * 当更新行数不为1时，很大可能是由于客户端post一个不存在的课程代号
             */
            throw new BaseException(ErrorCode.PARAMS_ERROR, "提交错误请重试");
        }
    }


    /**
     *  =============================== TEST ====================================
     */
    @Transactional
    public int testUpdate(String courseCode) {
        return courseRepository.updateTest(courseCode);
    }

    /**
     * =================== 一些测试 =======================
     * 测试update时把课程+1, 防止违背课程余量为非负数的数据完整性
     */

    public String getRandomCourseCode() {
        String[] courseCodes = new String[]{
                "04XX02800", "04XX03200", "04XX03300", "04XX03400", "04XX03500", "04XX03600", "04XX03700", "04XX03900", "04XX04000"
        };
        return courseCodes[(int) (Math.random() * courseCodes.length)];
    }

    /**
     * 原生java写法
     */
    public void testNativeUpdate(String courseCode) {
        Connection conn = null;
        PreparedStatement ps = null;
        long start = System.currentTimeMillis();
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("update tb_course set surplus_num = surplus_num + 1 where code = ?");
            ps.setString(1, getRandomCourseCode());
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
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

    public List<Course> testNativeSelect() {
        List<Course> courseList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement("select * from tb_course");
            rs = ps.executeQuery();
            Course course;
            while (rs.next()) {
                course = new Course();
                course.setCode(rs.getString("code"));
                course.setCredit(rs.getInt("credit"));
                course.setName(rs.getString("name"));
                course.setTeacher(rs.getString("teacher"));
                course.setSurplusNum(rs.getInt("surplus_num"));
                course.setTotalNum(rs.getInt("total_num"));
                courseList.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
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
        return courseList;
    }

    public int testProcedure(String courseCode) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = dataSource.getConnection();
            cs = conn.prepareCall("{call test_update(?, ?)}");
            cs.setString(1, courseCode);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2);
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
        return 1;
    }

    /**
     * JdbcTemplate 的用法
     */
    public List<Course> testJdbcTemplate() {
        String sql = "elect * from tb_course";
        return jdbcTemplate.query(sql, new RowMapper<Course>() {
            @Override
            public Course mapRow(ResultSet rs, int i) throws SQLException {
                Course course = new Course();
                course.setCode(rs.getString("code"));
                course.setCredit(rs.getInt("credit"));
                course.setName(rs.getString("name"));
                course.setTeacher(rs.getString("teacher"));
                course.setSurplusNum(rs.getInt("surplus_num"));
                course.setTotalNum(rs.getInt("total_num"));
                return course;
            }
        });
    }
}
