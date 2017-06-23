package top.leeys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import top.leeys.domain.Student;


public interface StudentRepository extends JpaRepository<Student, String>{
    
    Student findBySidAndPassword(String sid, String password);
}
