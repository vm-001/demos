package top.leeys.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 当 @Id 注解在字段时, Hibernate会通过字段获取该值，而不是getter方法.
 * 对于有自定义逻辑的字段需要使用 @Access(AccessType.PROPERTY) 表示该字段的值由它的getter方法获取
 * 参考链接: https://stackoverflow.com/questions/13874528/what-is-the-purpose-of-accesstype-field-accesstype-property-and-access
 * 
 * @author leeys.top@gmail.com
 */
@Entity
@Table(name = "tb_order_item")
@Getter @Setter @ToString @NoArgsConstructor
public class OrderItem {
   
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @JsonIgnore
    @ManyToOne
    private Order order;
    
    @OneToOne
    private Book book;
    
    private int num;
    
    @Access(AccessType.PROPERTY)
    private float price;
    
    public OrderItem(Order order, Book book, int num) {
        this.order = order;
        this.book = book;
        this.num = num;
    }
    
    /**
     * 作用: 保存到数据库时自动计算价格
     * 1)当保存时改方法会被调用, 如果price为0,则会计算价格
     * 2)当从数据库读取时, price不会等于0(图书价格为0除外), 对外序列成json时不会重新计算,节省计算能力
     */
    public float getPrice() {
        if (price == 0)
            price = book.getPrice() * book.getDiscount() / 10 * getNum();
        return price;
    }
}