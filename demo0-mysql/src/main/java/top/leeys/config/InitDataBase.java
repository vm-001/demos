package top.leeys.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.leeys.domain.Course;
import top.leeys.domain.Student;
import top.leeys.repository.CourseRepository;
import top.leeys.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 此类仅用于开发期间初始化数据
 * 当数据表记录为空时自动插入数据
 * @author leeys.top@gmail.com
 *
 */
@Component
public class InitDataBase {
    private StudentRepository studentRepository;
    private CourseRepository  courseRepository;
    
    @Autowired
    public void init(StudentRepository studentRepository, CourseRepository  courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        init();
    }
    
    

	public void init() {
	    System.out.println("====== 应用启动  =====");
        System.out.println("开始初始化数据库数据... ");
        if (studentRepository.findAll().size() == 0) {
            Student s1 = new Student("0104150000", "123456", "柴总", "445221");
            Student s2 = new Student("0104150001", "123456", "柯基", "445221");
            studentRepository.save(s1);
            studentRepository.save(s2);
            System.out.println("插入数据：" + s1);
            System.out.println("插入数据：" + s2);
        }
        if (courseRepository.findAll().size() == 0) {
            courseRepository.save(getCourseList());
            System.out.println("插入数据:" + getCourseList());
        }
	}

	
	private List<Course> getCourseList() {
	    List<Course> courseList = new ArrayList<>();
	    courseList.add(new Course("04XX03200", "走近高新技术", "李宁湘", 2, 150, 150));
	    courseList.add(new Course("04XX03300", "君子修养", "李宁湘", 2, 150, 150));
	    courseList.add(new Course("04XX03400", "创业方法与创新精神", "杜海东", 2, 150, 150));
	    courseList.add(new Course("04XX03500", "人文艺术与修养", "李双芹", 2, 150, 150));
	    courseList.add(new Course("04XX03600", "历史文化修养", "李任欣", 2, 150, 150));
	    courseList.add(new Course("04XX03700", "人际沟通与口头表达", "臧焱辛", 2, 150, 150));
	    courseList.add(new Course("04XX02800", "美术欣赏", "朱星雨", 2, 150, 150));
	    courseList.add(new Course("04XX03900", "信息检索与实践", "邵海义", 2, 150, 150));
	    courseList.add(new Course("04XX04000", "数学建模（基础班）", "康海刚", 2, 150, 150));
	   return courseList;
	}
}
