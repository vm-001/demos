package top.leeys.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * 学生表
 * @author leeys.top@gmail.com
 *
 */
@Entity
@Table(name = "tb_student")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Student {
    
    @Id
    private String sid;         //学号
    private String password;    //密码, 因有找回功能所以没有用md5
    private String name;        //姓名
    @Column(name = "id_card")
    private String idCard;      //身份证
}
