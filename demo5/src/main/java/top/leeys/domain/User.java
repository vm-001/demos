package top.leeys.domain;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tb_user")
@Getter @Setter @ToString @AllArgsConstructor
@JsonIgnoreProperties({"password", "email", "ip"})
public class User {
    @Id
    private String uuid;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    private float balance;
    
    private String ip;
    
    private String tags;
    
    private String username;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name = "created_time")
    private Date createdTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name = "last_signin")
    private Date lastSignin;
    
    private String cart;  //redis中购物车的key值
    
    public User() {
        this.uuid = UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    public User(String uuid) {
        this.uuid = uuid;
    }
    
    
}
