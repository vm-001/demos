package top.leeys.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * 选课记录表
 * @author leeys.top@gmail.com
 *
 */
@Entity
@Table(name = "tb_record")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Record {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String sid;  //没有使用外键
    @Column(name = "course_code")
    private String courseCode;  //没有使用外键
} 
