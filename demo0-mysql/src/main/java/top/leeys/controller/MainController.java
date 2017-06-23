package top.leeys.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.Login;
import top.leeys.config.AppConfig;
import top.leeys.config.ErrorCode;
import top.leeys.domain.Course;
import top.leeys.domain.Student;
import top.leeys.exception.BaseException;
import top.leeys.pojo.Message;
import top.leeys.server.MainService;

@Controller
@Slf4j
public class MainController {
    @Autowired
    private MainService mainService;

    @GetMapping("/")
    public String index() {
        return "forward:login.html";
    }
    
    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<Message> login(
            @RequestParam("sid") String sid,
            @RequestParam("password") String password,
            HttpSession session) {
        if (sid == null || password == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Student student = mainService.getUserBySid(sid);
        Message message = new Message();
        if (student == null) {
            message.setMsg(ErrorCode.SID_NOT_EXIST, "学号不存在");
        } else if (!student.getPassword().equals(password)) {
            message.setMsg(ErrorCode.PASSWORD_ERROR, "密码错误");
        } else {
            message.setMsg(0, "success");
            session.setAttribute("username", sid);
        }
        log.info("用户登录:{} {}", sid, password) ;
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
    
    @Login //需要在WebConfig中添加拦截器
    @GetMapping("/course")
    public String course(Model model) {
        List<Course> courseList = mainService.getCourseList();
        model.addAttribute("courseList", courseList);
        return "course";
    }
    
    
    
    /**
     * 学校15年的正方教务系统没有修改密码功能，只有找回，因为有找回功能，所以数据库的用了明文保存asdfasdfassadfasdf
     */
    @ResponseBody
    @PostMapping("/forget")
    public ResponseEntity<Message> forget(
            @RequestParam("sid")String sid,
            @RequestParam("name")String name,
            @RequestParam("idcard")String idCard) {
        if (sid == null || name == null || idCard == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Message message = new Message();
        Student student = mainService.getUserBySid(sid);
        if (student == null) {
            message.setMsg(ErrorCode.SID_NOT_EXIST, "学号不存在");
        } else if (!student.getName().equals(name)) {
            message.setMsg(ErrorCode.NAME_ERROR, "姓名错误");
        } else if (!student.getIdCard().equals(idCard)){
            message.setMsg(ErrorCode.IDCARD_ERROR, "身份证号码错误");
        } else {
            message.setMsg(0, "success", student.getPassword());
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
    
    
    @ResponseBody
    @PostMapping("/submit")
    public ResponseEntity<Message> submit(
            @RequestParam("course_code")String courseCode,
            @SessionAttribute(name = "username", required = false)String sid) {

        if (courseCode == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Message message = new Message();
        System.out.println("sid:" + sid);
        if (sid == null) {
            message.setMsg(ErrorCode.SESSION_EXPIRED, "您的登录状态已经超时，请重新登录");
        } else if (AppConfig.DEVELOPER && sid.equals("0104150000")) {
            message.setMsg(0, "选课成功！\\n由于您使用的是测试账号，不会插入到数据库中！");
        }else {
            try {
                mainService.selectCourse(sid, courseCode);
                message.setMsg(0, "success");
            } catch(DataIntegrityViolationException e) {
                /**
                 * DataIntegrityViolationException: Spring封装的与具体实现无关的高度抽象异常
                 * 由于 JPA 的具体实现是 hibernate, 其也对 MySQL 的异常进行封装
                 * DataException                --> 由于课程剩余数量设置了 unsigned, 当课程减到负数时，会抛出该异常
                 * ConstraintViolationException --> 约束异常, 在这里为违背了 Record 表 sid 字段的唯一索引
                 */
                if (e.contains(DataException.class)) {
                    message.setMsg(ErrorCode.COURSE_IS_ZERO, "阁下手速慢了，已经被抢光了");
                } else if (e.contains(ConstraintViolationException.class)) {
                    message.setMsg(0, "您已经选过课了");
                } else {
                    message.setMsg(ErrorCode.UNKNOWN_ERROR, "服务器未知异常，请稍后重试");
                    log.error("服务器未知异常", e);
                }
            }
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
    /*
     ========================================== TEST ============================================
     */

    //Spring + JPA  1800
    @ResponseBody
    @GetMapping("/update")
    public ResponseEntity<Message> testUpdate() {
        String courseCode = mainService.getRandomCourseCode();
        mainService.testUpdate(courseCode);
        return new ResponseEntity<>(new Message(0, "success"), HttpStatus.OK);
    }

    //Spring + JPA 1700-1900
    @ResponseBody
    @GetMapping("/select")
    public ResponseEntity<Message> testSelect() {
        mainService.getCourseList();
        return new ResponseEntity<>(new Message(0, "success"), HttpStatus.OK);
    }

    //Spring + Native 2200
    @ResponseBody
    @GetMapping("/native/update")
    public ResponseEntity<Message> nativeUpdate() {
        String courseCode = mainService.getRandomCourseCode();
        mainService.testNativeUpdate(courseCode);
        return new ResponseEntity<>(new Message(0, "success"), HttpStatus.OK);
    }

    //Spring + Native 2900
    @ResponseBody
    @GetMapping("/native/select")
    public ResponseEntity<Message> nativeSelect() {
        List<Course> courseList = mainService.testNativeSelect();
        return new ResponseEntity<>(new Message(0, "success", courseList), HttpStatus.OK);
    }
}
