package top.leeys.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 图书表
 * PS: lucene索引的字段不能为NULL, 这里简单地把字段值设为空字符串
 * 
 * @author leeys.top@gmail.com
 */
@Entity
@Table(name = "tb_book", indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_isbn", columnList = "isbn"),
        @Index(name = "idx_author", columnList = "author"),
        @Index(name = "idx_price", columnList = "price"),
        @Index(name = "idx_publisher", columnList = "publisher"),
        @Index(name = "idx_publish_date", columnList = "publish_date"),
})
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class Book {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "marc_no")
    private String marcNo = "";
    
    private String isbn = "";    //图书馆数据比较混乱，包括isbn10和isbn13
    
    private String name = "";
    
    @Column(name = "img_url")
    private String imgUrl = "";
    
    private String summary = "";  //简介
    
    private String author = "";
    
    @Column(name = "author_info")
    private String authorInfo = ""; 
    
    private float price;
    
    private float discount = 10; //折扣 默认原价
    
    private String publisher = "";
    
    private int inventory = 100; //库存
    
    @JsonFormat(pattern = "yyyy-MM", timezone="GMT+8")
    @Column(name = "publish_date")
    @Temporal(TemporalType.DATE)
    private Date publishDate;
}
