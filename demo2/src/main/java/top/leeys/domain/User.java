package top.leeys.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vdurmont.emoji.EmojiParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户表
 * 只考虑微博用户, 所以只用一张表
 * 
 * @author leeys.top@gmail.com
 */
@Entity
@Table(name = "tb_user", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_uid", columnList = "uid"),
        @Index(name = "idx_createdTime", columnList = "created_time")
})
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
@JsonIgnoreProperties({"accessToken", "extra", "token", "createdTime"})
public class User {
    @Id
    private String uuid;
    
    private String username;   //微博用户名
    
    private String uid;        //微博uid
    
    @Column(name = "avatar_url")
    private String avatarUrl;    //50*50
    
    private String description;  //微博简介
    
    @Column(name = "access_token")
    private String accessToken;  //新浪access_token
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name = "created_time")
    private Date createdTime;
    
    @Column(length=3000)
    private String extra;  //存储调用微博接口返回用户信息的json
    
    private String token;  //客户端鉴权
    
    public User(String uuid) {
        this.uuid = uuid;
    }
    
    public static void main(String[] args) {
        String input = "A string 😄with a \uD83D\uDC66\uD83C\uDFFFfew 😉emojis!";
        System.out.println(EmojiParser.removeAllEmojis(input));
        System.out.println(EmojiParser.parseToUnicode(input));
    }
}
