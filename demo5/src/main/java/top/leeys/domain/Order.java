package top.leeys.domain;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tb_order")
@Getter @Setter @ToString @AllArgsConstructor
@JsonIgnoreProperties({""})
public class Order {
    @Id
    private String uuid;
    
    @Column(name = "origin_price")
    private float originPrice;   //原价
    
    private float price;   //折后价格
    
    private int status;    //0等待支付, 1支付成功, 2支付超时
    
    private String phone;
    
    public static interface Status {
        int WAIT = 0;
        int SUCCESS = 1;
        int OVER_TIME = 2;
    }
    
    private String consignee;
    
    private String description;
    
    @ManyToOne
    private User user;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name = "created_time")
    private Date createdTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name = "pay_time")
    private Date payTime;   //支付时间
    
    
    private String address; //地址
    
    /**
     * 这两个注解可以解决双向关系的互相引用: @JsonManagedReference @JsonBackReference 
     * 但更简单的方式是在另外一边使用: @JsonIgnore
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy="order", fetch = FetchType.EAGER)
    private List<OrderItem> itemList;
    
    
    public Order() {
        this.uuid = UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    public Order(String uuid) {
        this.uuid = uuid;
    }
    
    /*计算订单原价*/
    public float countOriginPrice(List<OrderItem> itemList) {
        float p = 0;
        if (itemList != null) {
            for (OrderItem item : itemList) {
                p += item.getBook().getPrice() * item.getNum();
            }
        }
        return p;
    }
    
    /*计算打折后的价格*/
    public float countPrice(List<OrderItem> itemList) {
        float p = 0;
        if (itemList != null) {
            for (OrderItem item : itemList) {
                p += item.getPrice();
            }
        }
        return p;
    }
}
