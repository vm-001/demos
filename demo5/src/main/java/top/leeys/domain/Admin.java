package top.leeys.domain;

import java.util.Date;

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

@Entity
@Table(name = "tb_admin")
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class Admin {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String username;
    
    private String password;
    
    @Column(name = "super_admin")
    private int superAdmin;
    
    @Column(name = "created_time")
    private Date createdTime;
    
    @Column(name = "last_signin")
    private Date lastSignin;
    
    private String ip;
    
    private String token;
}
