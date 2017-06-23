package top.leeys.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * 课程表
 * @author leeys.top@gmail.com
 *
 */
@Entity
@Table(name = "tb_course")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Course {
    @Id
    private String code;  //代号
    private String name;  //名称
    private String teacher;
    private Integer credit;  //学分
    @Column(name = "total_num")
    private Integer totalNum; //课程总数
    @Column(name = "surplus_num", columnDefinition = "int unsigned")
    private Integer surplusNum; //剩余数
}
