package top.leeys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import top.leeys.domain.Course;


public interface CourseRepository extends JpaRepository<Course, String>{
    
    @Modifying
    @Query("update Course set surplusNum = surplusNum - 1 where code = ?1")
    int updateCourse(String courseCode);
    
    /*用于测试并发update, 课程数量+1*/
    @Modifying
    @Query("update Course set surplusNum = surplusNum + 1 where code = ?1")
    int updateTest(String courseCode);

}
