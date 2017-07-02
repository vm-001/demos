package top.leeys.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 游戏记录表
 * 
 * @author leeys.top@gmail.com
 */
@Entity
@Table(name = "tb_record")
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class Record {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    private User user;
    
    private Integer score;
    
    private Integer type;  //1单人 2双人
    
    @Column(name = "created_time")
    private Date createTime;
}
